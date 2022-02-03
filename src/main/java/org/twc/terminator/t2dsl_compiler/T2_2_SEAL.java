package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_SEAL extends T2_Compiler {

  public T2_2_SEAL(SymbolTable st, String config_file_path, int word_sz) {
    super(st, config_file_path, word_sz);
    if (this.is_binary_) {
      this.st_.backend_types.put("EncInt", "vector<Ciphertext>");
      this.st_.backend_types.put("EncInt[]", "vector<vector<Ciphertext>>");
    } else {
      this.st_.backend_types.put("EncInt", "Ciphertext");
      this.st_.backend_types.put("EncInt[]", "vector<Ciphertext>");
    }
  }

  protected void append_keygen() {
    append_idx("size_t poly_modulus_degree = 16384;\n");
    append_idx("auto p_mod = PlainModulus::Batching(poly_modulus_degree, 20);\n");
    append_idx("size_t plaintext_modulus = (size_t) p_mod.value();\n");
    append_idx("EncryptionParameters parms(scheme_type::bfv);\n");
    append_idx("parms.set_poly_modulus_degree(poly_modulus_degree);\n");
    append_idx("parms.set_coeff_modulus(CoeffModulus::BFVDefault(poly_modulus_degree));\n");
    append_idx("parms.set_plain_modulus(p_mod);\n");
    append_idx("SEALContext context(parms);\n");
    append_idx("KeyGenerator keygen(context);\n");
    append_idx("SecretKey secret_key = keygen.secret_key();\n");
    append_idx("PublicKey public_key;\n");
    append_idx("RelinKeys relin_keys;\n");
    append_idx("keygen.create_public_key(public_key);\n");
    append_idx("keygen.create_relin_keys(relin_keys);\n");
    append_idx("Encryptor encryptor(context, public_key);\n");
    append_idx("Evaluator evaluator(context);\n");
    append_idx("Decryptor decryptor(context, secret_key);\n");
    append_idx("BatchEncoder batch_encoder(context);\n");
    append_idx("size_t slots = poly_modulus_degree/2;\n");
    append_idx("Plaintext tmp;\n");
    append_idx(this.st_.backend_types.get("EncInt") + " tmp_");
    if (is_binary_) this.asm_.append("(").append(this.word_sz_).append(")");
    this.asm_.append(";\n\n");
  }

  protected void encrypt(String dst, String[] src_lst) {
    if (this.is_binary_) {
      String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
      append_idx("vector<vector<uint64_t>> " + tmp_vec + "(");
      this.asm_.append(this.word_sz_).append(", vector<uint64_t>(slots, 0));\n");
      for (int slot = 0; slot < src_lst.length; slot++) {
        String src = src_lst[slot];
        boolean is_numeric = isNumeric(src);
        if (is_numeric) {
          int[] bin_array = int_to_bin_array(Integer.parseInt(src));
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx(tmp_vec + "[" + i + "][" + slot + "] = " + bin_array[i] + ";\n");
          }
        } else {
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx(tmp_vec + "[" + i + "][" + slot + "] = static_cast<uint64_t>((");
            this.asm_.append(src).append(" >> ").append(this.word_sz_ - i - 1);
            this.asm_.append(") & 1);\n");
          }
        }
      }
      for (int i = 0; i < this.word_sz_; i++) {
        append_idx("batch_encoder.encode(");
        this.asm_.append(tmp_vec).append("[").append(i).append("], tmp);\n");
        append_idx("encryptor.encrypt(tmp, ");
        this.asm_.append(dst).append("[").append(i).append("]").append(")");
        if (i < this.word_sz_ - 1) this.asm_.append(";\n");
      }
    } else {
      assert(src_lst.length == 1);
      append_idx("tmp = uint64_to_hex_string(");
      this.asm_.append(src_lst[0]).append(");\n");
      append_idx("encryptor.encrypt(tmp, ");
      this.asm_.append(dst).append(")");
    }
  }

  /**
   * f0 -> "int"
   * f1 -> "main"
   * f2 -> "("
   * f3 -> "void"
   * f4 -> ")"
   * f5 -> "{"
   * f6 -> ( VarDeclaration() )*
   * f7 -> ( Statement() )*
   * f8 -> "return"
   * f9 -> Expression()
   * f10 -> ";"
   * f11 -> "}"
   */
  public Var_t visit(MainClass n) throws Exception {
    append_idx("#include <iostream>\n");
    append_idx("#include <chrono>\n\n");
    append_idx("#include \"seal/seal.h\"\n");
    append_idx("#include \"../functional_units/functional_units.hpp\"\n\n");
    append_idx("using namespace seal;\n");
    append_idx("using namespace std;\n\n");
    append_idx("int main(void) {\n");
    this.indent_ = 2;
    if (!read_keygen_from_file()) {
      append_keygen();
    }
    n.f6.accept(this);
    n.f7.accept(this);
    append_idx("return ");
    Var_t ret = n.f9.accept(this);
    this.asm_.append(ret.getName()).append(";\n}");
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   */
  public Var_t visit(AssignmentStatement n) throws Exception {
    Var_t lhs = n.f0.accept(this);
    String lhs_type = st_.findType(lhs);
    Var_t rhs = n.f2.accept(this);
    String rhs_type = st_.findType(rhs);
    String rhs_name = rhs.getName();
    if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      // if EncInt <- int
      encrypt(lhs.getName(), new String[]{rhs_name});
      this.semicolon_ = true;
    } else if (lhs_type.equals("EncInt[]") && rhs_type.equals("int[]")) {
      // if EncInt[] <- int[]
      append_idx(lhs.getName());
      this.asm_.append(".resize(").append(rhs_name).append(".size());\n");
      if (this.is_binary_) {
        append_idx("for (size_t " + this.tmp_i + " = 0; " + this.tmp_i + " < ");
        this.asm_.append(rhs_name).append(".size(); ++").append(this.tmp_i);
        this.asm_.append(") {\n");
        this.indent_ += 2;
        append_idx(lhs.getName() + "[" + this.tmp_i + "].resize(" + this.word_sz_ + ");\n");
        this.indent_ -= 2;
        append_idx("}\n");
      }
      append_idx("for (size_t " + this.tmp_i + " = 0; " + this.tmp_i + " < ");
      this.asm_.append(rhs_name).append(".size(); ++").append(this.tmp_i);
      this.asm_.append(") {\n");
      this.indent_ += 2;
      encrypt(lhs.getName() + "[" + this.tmp_i + "]",
              new String[]{rhs_name + "[" + this.tmp_i + "]"});
      this.asm_.append(";\n");
      this.indent_ -= 2;
      append_idx("}\n");
    } else if (lhs_type.equals(rhs_type)) {
      // if the destination has the same type as the source.
      if (this.is_binary_ && (lhs_type.equals("EncInt") || lhs_type.equals("EncInt[]"))) {
        if (rhs_name.startsWith("resize(")) {
          int rhs_new_size = 0;
          rhs_new_size = Integer.parseInt(rhs_name.substring(7, rhs_name.length()-1));
          append_idx(lhs.getName() + ".resize(" + rhs_new_size + ");\n");
          for (int i = 0; i < rhs_new_size; i++) {
            append_idx(lhs.getName() + "[" + i + "].resize(" + this.word_sz_ + ");\n");
          }
        } else {
          append_idx(lhs.getName() + " = " + rhs_name);
          this.semicolon_ = true;
        }
      } else {
        append_idx(lhs.getName());
        if (rhs_name.startsWith("resize(")) {
          this.asm_.append(".");
        } else {
          this.asm_.append(" = ");
        }
        this.asm_.append(rhs_name);
        this.semicolon_ = true;
      }
    } else {
      throw new Exception("Error assignment statement between different " +
              "types: " + lhs_type + ", " + rhs_type);
    }
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "++"
   */
  public Var_t visit(IncrementAssignmentStatement n) throws Exception {
    Var_t id = n.f0.accept(this);
    String id_type = st_.findType(id);
    if (id_type.equals("EncInt")) {
      if (this.is_binary_) {
        append_idx(id.getName());
        this.asm_.append(" = ").append("inc_bin(evaluator, encryptor, ");
        this.asm_.append("batch_encoder, relin_keys, ").append(id.getName());
        this.asm_.append(", slots);\n");
      } else {
        append_idx("tmp = uint64_to_hex_string(1);\n");
        append_idx("evaluator.add_plain_inplace(");
        this.asm_.append(id.getName()).append(", tmp);\n");
      }
    } else {
      append_idx(id.getName());
      this.asm_.append("++");
      this.semicolon_ = true;
    }
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "--"
   */
  public Var_t visit(DecrementAssignmentStatement n) throws Exception {
    Var_t id = n.f0.accept(this);
    String id_type = st_.findType(id);
    if (id_type.equals("EncInt")) {
      if (this.is_binary_) {
        append_idx(id.getName());
        this.asm_.append(" = ").append("dec_bin(evaluator, encryptor, ");
        this.asm_.append("batch_encoder, relin_keys, ").append(id.getName());
        this.asm_.append(", slots);\n");
      } else {
        append_idx("tmp = uint64_to_hex_string(1);\n");
        append_idx("evaluator.sub_plain_inplace(");
        this.asm_.append(id.getName()).append(", tmp);\n");
      }
    } else {
      append_idx(id.getName());
      this.asm_.append("--");
      this.semicolon_ = true;
    }
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> CompoundOperator()
   * f2 -> Expression()
   */
  public Var_t visit(CompoundAssignmentStatement n) throws Exception {
    Var_t lhs = n.f0.accept(this);
    String op = n.f1.accept(this).getName();
    Var_t rhs = n.f2.accept(this);
    String lhs_type = st_.findType(lhs);
    String rhs_type = st_.findType(rhs);
    if (lhs_type.equals("int") && rhs_type.equals("int")) {
      append_idx(lhs.getName());
      this.asm_.append(" ").append(op).append(" ");
      this.asm_.append(rhs.getName());
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      if (this.is_binary_) {
        append_idx(lhs.getName() + " = ");
        switch (op) {
          case "+=":
            this.asm_.append("add_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            break;
          case "*=":
            this.asm_.append("mult_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            break;
          case "-=":
            this.asm_.append("sub_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
        this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
        this.asm_.append(", slots)");
      } else {
        append_idx("evaluator.");
        switch (op) {
          case "+=": this.asm_.append("add("); break;
          case "*=": this.asm_.append("multiply("); break;
          case "-=": this.asm_.append("sub("); break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
        this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
        this.asm_.append(", ").append(lhs.getName()).append(")");
        if (op.equals("*=")) {
          this.asm_.append(";\n");
          append_idx("evaluator.relinearize_inplace(");
          this.asm_.append(lhs.getName()).append(", relin_keys)");
        }
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      if (this.is_binary_) {
        encrypt("tmp_", new String[]{rhs.getName()});
        this.asm_.append(";\n");
        append_idx(lhs.getName() + " = ");
        switch (op) {
          case "+=":
            this.asm_.append("add_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            this.asm_.append(lhs.getName()).append(", tmp_, slots)");
            break;
          case "*=":
            this.asm_.append("mult_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            this.asm_.append(lhs.getName()).append(", tmp_, slots)");
            break;
          case "-=":
            this.asm_.append("sub_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            this.asm_.append(lhs.getName()).append(", tmp_, slots)");
            break;
          case "<<=":
            this.asm_.append("shift_left_bin(encryptor, batch_encoder, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", slots);\n");
            break;
          case ">>=":
            this.asm_.append("shift_right_bin(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", slots);\n");
            break;
          case ">>>=":
            this.asm_.append("shift_right_logical_bin(encryptor, batch_encoder, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", slots);\n");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        append_idx("tmp = uint64_to_hex_string(");
        this.asm_.append(rhs.getName());
        this.asm_.append(");\n");
        append_idx("evaluator.");
        switch (op) {
          case "+=": this.asm_.append("add_plain("); break;
          case "*=": this.asm_.append("multiply_plain("); break;
          case "-=": this.asm_.append("sub_plain("); break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
        this.asm_.append(lhs.getName()).append(", tmp, ");
        this.asm_.append(lhs.getName()).append(")");
      }
    }
    this.semicolon_ = true;
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> CompoundOperator()
   * f5 -> Expression()
   */
  public Var_t visit(CompoundArrayAssignmentStatement n) throws Exception {
    Var_t id = n.f0.accept(this);
    String id_type = st_.findType(id);
    Var_t idx = n.f2.accept(this);
    String op = n.f4.accept(this).getName();
    Var_t rhs = n.f5.accept(this);
    String rhs_type = st_.findType(rhs);
    switch (id_type) {
      case "int[]":
        append_idx(id.getName());
        this.asm_.append("[").append(idx.getName()).append("] ").append(op);
        this.asm_.append(" ").append(rhs.getName());
        break;
      case "EncInt[]":
        if (rhs_type.equals("EncInt")) {
          if (this.is_binary_) {
            append_idx(id.getName() + "[" + idx.getName() + "] = ");
            switch (op) {
              case "+=":
                this.asm_.append("add_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
                break;
              case "*=":
                this.asm_.append("mult_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
                break;
              case "-=":
                this.asm_.append("sub_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
                break;
              default:
                throw new Exception("Error in compound array assignment");
            }
            this.asm_.append(id.getName()).append("[").append(idx.getName());
            this.asm_.append("], ").append(rhs.getName()).append(", slots)");
          } else {
            append_idx("evaluator.");
            switch (op) {
              case "+=": this.asm_.append("add("); break;
              case "*=": this.asm_.append("multiply("); break;
              case "-=": this.asm_.append("sub("); break;
              default:
                throw new Exception("Error in compound array assignment");
            }
            this.asm_.append(id.getName()).append("[").append(idx.getName());
            this.asm_.append("], ").append(rhs.getName()).append(", ");
            this.asm_.append(id.getName()).append("[").append(idx.getName());
            this.asm_.append("])");
            if (op.equals("*=")) {
              this.asm_.append(";\n");
              append_idx("evaluator.relinearize_inplace(");
              this.asm_.append(id.getName()).append("[").append(idx.getName());
              this.asm_.append("], relin_keys)");
            }
          }
          break;
        } else if (rhs_type.equals("int")) {
          if (op.equals("<<=")) {
            this.asm_.append("shift_left_bin(encryptor, batch_encoder, ");
            this.asm_.append(id.getName()).append("[").append(idx.getName()).append("]");
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", slots);\n");
          } else if (op.equals(">>=")) {
            this.asm_.append("shift_right_bin(");
            this.asm_.append(id.getName()).append("[").append(idx.getName()).append("]");
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", slots);\n");
          } else if (op.equals(">>>=")) {
            this.asm_.append("shift_right_logical_bin(encryptor, batch_encoder, ");
            this.asm_.append(id.getName()).append("[").append(idx.getName()).append("]");
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", slots);\n");
          } 
          throw new Exception("Encrypt and move to temporary var.");
        }
      default:
        throw new Exception("error in array assignment");
    }
    this.semicolon_ = true;
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> "="
   * f5 -> Expression()
   */
  public Var_t visit(ArrayAssignmentStatement n) throws Exception {
    Var_t id = n.f0.accept(this);
    String id_type = st_.findType(id);
    Var_t idx = n.f2.accept(this);
    Var_t rhs = n.f5.accept(this);
    String rhs_type = st_.findType(rhs);
    switch (id_type) {
      case "int[]":
        append_idx(id.getName());
        this.asm_.append("[").append(idx.getName()).append("] = ");
        this.asm_.append(rhs.getName());
        break;
      case "EncInt[]":
        if (rhs_type.equals("EncInt")) {
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] = ");
          this.asm_.append(rhs.getName()).append(";\n");
          break;
        } else if (rhs_type.equals("int")) {
          encrypt(id.getName() + "[" + idx.getName() + "]",
                  new String[]{rhs.getName()});
          break;
        }
      default:
        throw new Exception("error in array assignment");
    }
    this.semicolon_ = true;
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> "{"
   * f3 -> Expression()
   * f4 -> ( BatchAssignmentStatementRest() )*
   * f5 -> "}"
   */
  public Var_t visit(BatchAssignmentStatement n) throws Exception {
    Var_t id = n.f0.accept(this);
    String id_type = st_.findType(id);
    Var_t exp = n.f3.accept(this);
    String exp_type = st_.findType(exp);
    switch (id_type) {
      case "int[]":
        append_idx(id.getName());
        this.asm_.append(" = { ").append(exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" };\n");
        break;
      case "EncInt":
        if (this.is_binary_) {
          String[] elems = new String[1 + n.f4.size()];
          elems[0] = exp.getName();
          if (n.f4.present()) {
            for (int i = 0; i < n.f4.size(); i++) {
              elems[i + 1] = (n.f4.nodes.get(i).accept(this)).getName();
            }
          }
          encrypt(id.getName(), elems);
          this.asm_.append(";\n");
        } else {
          String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
          append_idx("vector<uint64_t> " + tmp_vec);
          this.asm_.append(" = { static_cast<uint64_t>(");
          this.asm_.append(exp.getName()).append(")");
          if (n.f4.present()) {
            for (int i = 0; i < n.f4.size(); i++) {
              this.asm_.append(", static_cast<uint64_t>(");
              this.asm_.append((n.f4.nodes.get(i).accept(this)).getName()).append(")");
            }
          }
          this.asm_.append(" };\n");
          append_idx("batch_encoder.encode(");
          this.asm_.append(tmp_vec).append(", tmp);\n");
          append_idx("encryptor.encrypt(tmp, ");
          this.asm_.append(id.getName()).append(");\n");
        }
        break;
      case "EncInt[]":
        String exp_var;
        if (exp_type.equals("int")) {
          exp_var = new_ctxt_tmp();
          encrypt(exp_var, new String[]{exp.getName()});
          this.asm_.append(";\n");
        } else { // exp type is EncInt
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            if (exp_type.equals("int")) {
              String tmp_ = new_ctxt_tmp();
              encrypt(tmp_, new String[]{init});
              this.asm_.append(";\n");
              inits.add(tmp_);
            } else { // exp type is EncInt
              inits.add(init);
            }
          }
        }
        append_idx(id.getName());
        this.asm_.append(" = { ").append(exp_var);
        for (String init : inits) {
          this.asm_.append(", ").append(init);
        }
        this.asm_.append(" };\n");
        break;
      default:
        throw new Exception("Bad operand types: " + id.getName() + " " + exp_type);
    }
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> "="
   * f5 -> "{"
   * f6 -> Expression()
   * f7 -> ( BatchAssignmentStatementRest() )*
   * f8 -> "}"
   */
  public Var_t visit(BatchArrayAssignmentStatement n) throws Exception {
    Var_t id = n.f0.accept(this);
    Var_t index = n.f2.accept(this);
    Var_t exp = n.f6.accept(this);
    String id_type = st_.findType(id);
    assert(id_type.equals("EncInt[]"));
    if (this.is_binary_) {
      String[] elems = new String[1 + n.f7.size()];
      elems[0] = exp.getName();
      if (n.f7.present()) {
        for (int i = 0; i < n.f7.size(); i++) {
          elems[i + 1] = (n.f7.nodes.get(i).accept(this)).getName();
        }
      }
      encrypt(id.getName() + "[" + index.getName() + "]", elems);
      this.asm_.append(";\n");
    } else {
      String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
      append_idx("vector<uint64_t> " + tmp_vec);
      this.asm_.append(" = { static_cast<uint64_t>(").append(exp.getName()).append(")");
      if (n.f7.present()) {
        for (int i = 0; i < n.f7.size(); i++) {
          this.asm_.append(", static_cast<uint64_t>(");
          this.asm_.append((n.f7.nodes.get(i).accept(this)).getName()).append(")");
        }
      }
      this.asm_.append(" };\n");
      append_idx("batch_encoder.encode(");
      this.asm_.append(tmp_vec).append(", tmp);\n");
      append_idx("encryptor.encrypt(tmp, ");
      this.asm_.append(id.getName()).append("[").append(index.getName()).append("]);\n");
    }
    return null;
  }

  /**
   * f0 -> "print"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   */
  public Var_t visit(PrintStatement n) throws Exception {
    Var_t expr = n.f2.accept(this);
    String expr_type = st_.findType(expr);
    switch (expr_type) {
      case "int":
        append_idx("cout << ");
        this.asm_.append(expr.getName());
        this.asm_.append(" << endl");
        break;
      case "EncInt":
        if (this.is_binary_) {
          String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
          append_idx("vector<vector<uint64_t>> " + tmp_vec + "("+ this.word_sz_ + ");\n");
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx(tmp_vec + "[" + i + "] = decrypt_array_batch_to_nums(\n");
            append_idx("  decryptor, batch_encoder, ");
            this.asm_.append(expr.getName()).append("[").append(i).append("], slots);\n");
          }
          append_idx("for (int " + this.tmp_i + " = 0; ");
          this.asm_.append(this.tmp_i).append(" < ").append(this.word_sz_);
          this.asm_.append("; ++").append(this.tmp_i).append(") {\n");
          append_idx("  cout << " + tmp_vec + "[" + this.tmp_i + "][0];\n");
          append_idx("}\n");
          append_idx("cout << endl");
        } else {
          append_idx("decryptor.decrypt(");
          this.asm_.append(expr.getName()).append(", tmp);\n");
          append_idx("cout << \"dec(");
          this.asm_.append(expr.getName()).append(") = \" << tmp << endl");
        }
        break;
      default:
        throw new Exception("Bad type for print statement");
    }
    this.semicolon_ = true;
    return null;
  }

  /**
   * f0 -> "print_batched"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ","
   * f4 -> Expression()
   * f5 -> ")"
   */
  public Var_t visit(PrintBatchedStatement n) throws Exception {
    Var_t expr = n.f2.accept(this);
    String expr_type = st_.findType(expr);
    assert(expr_type.equals("EncInt"));
    Var_t size = n.f4.accept(this);
    String size_type = st_.findType(expr);
    assert(size_type.equals("int"));
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    if (this.is_binary_) {
      append_idx("vector<vector<uint64_t>> " + tmp_vec + "("+ this.word_sz_ + ");\n");
      for (int i = 0; i < this.word_sz_; i++) {
        append_idx(tmp_vec + "[" + i + "] = decrypt_array_batch_to_nums(\n");
        append_idx("  decryptor, batch_encoder, ");
        this.asm_.append(expr.getName()).append("[").append(i).append("], slots);\n");
      }
      String tmp_s = "tmp_s";
      append_idx("for (int " + tmp_s + " = 0; ");
      this.asm_.append(tmp_s).append(" < ").append(size.getName());
      this.asm_.append("; ++").append(tmp_s).append(") {\n");
      this.indent_ += 2;
      append_idx("for (int " + this.tmp_i + " = 0; ");
      this.asm_.append(this.tmp_i).append(" < ").append(this.word_sz_);
      this.asm_.append("; ++").append(this.tmp_i).append(") {\n");
      this.indent_ += 2;
      append_idx("cout << " + tmp_vec + "[" + this.tmp_i + "][" + tmp_s + "];\n");
      this.indent_ -= 2;
      append_idx("}\n");
      append_idx("cout << \"\\t\";\n");
      this.indent_ -= 2;
      append_idx("}\n");
      append_idx("cout << endl");
    } else {
      append_idx("vector<uint64_t> ");
      this.asm_.append(tmp_vec).append(" = decrypt_array_batch_to_nums(\n");
      append_idx("  decryptor, batch_encoder, ");
      this.asm_.append(expr.getName()).append(", slots);\n");
      append_idx("for (int " + this.tmp_i + " = 0; ");
      this.asm_.append(this.tmp_i).append(" < ").append(size.getName());
      this.asm_.append("; ++").append(this.tmp_i).append(") {\n");
      append_idx("  cout << " + tmp_vec + "[" + this.tmp_i + "] << \"\\t\";\n");
      append_idx("}\n");
      append_idx("cout << endl");
    }
    this.semicolon_ = true;
    return null;
  }

  /**
   * f0 -> <REDUCE_NOISE>
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   */
  public Var_t visit(ReduceNoiseStatement n) throws Exception {
    Var_t expr = n.f2.accept(this);
    String expr_type = st_.findType(expr);
    assert(expr_type.equals("EncInt"));
    append_idx("evaluator.mod_switch_to_next_inplace(");
    this.asm_.append(expr.getName()).append(");\n");
    return null;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> BinOperator()
   * f2 -> PrimaryExpression()
   */
  public Var_t visit(BinaryExpression n) throws Exception {
    Var_t lhs = n.f0.accept(this);
    String op = n.f1.accept(this).getName();
    Var_t rhs = n.f2.accept(this);
    String lhs_type = st_.findType(lhs);
    String rhs_type = st_.findType(rhs);
    if (lhs_type.equals("int") && rhs_type.equals("int")) {
      if ("&".equals(op) || "|".equals(op) || "^".equals(op) || "<<".equals(op) ||
          ">>".equals(op) || "+".equals(op) || "-".equals(op) || "*".equals(op) ||
          "/".equals(op) || "%".equals(op)
      ) {
        return new Var_t("int", lhs.getName() + op + rhs.getName());
      } else if ("==".equals(op) || "!=".equals(op) || "<".equals(op) ||
                 "<=".equals(op) || ">".equals(op) || ">=".equals(op) ||
                 "&&".equals(op) || "||".equals(op)) {
        return new Var_t("bool", lhs.getName() + op + rhs.getName());
      }
    } else if (lhs_type.equals("int") && rhs_type.equals("EncInt")) {
      String res_ = new_ctxt_tmp();
      if (this.is_binary_) {
        encrypt("tmp_", new String[]{lhs.getName()});
        this.asm_.append(";\n");
        append_idx(res_ + " = ");
        switch (op) {
          case "+":
            this.asm_.append("add_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, tmp_, ");
            this.asm_.append(rhs.getName()).append(", slots);\n");
            break;
          case "*":
            this.asm_.append("mult_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", slots);\n");
            break;
          case "-":
            this.asm_.append("sub_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, tmp_, ");
            this.asm_.append(rhs.getName()).append(", slots);\n");
            break;
          case "^":
            this.asm_.append("xor_bin(evaluator, relin_keys, tmp_, ");
            this.asm_.append(rhs.getName()).append(", plaintext_modulus);\n");
            break;
          case "==":
            this.asm_.append("eq_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, tmp_, ").append(rhs.getName());
            this.asm_.append(", ").append(this.word_sz_).append(", slots);\n");
            break;
          case "!=":
            this.asm_.append("neq_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, tmp_, ").append(rhs.getName());
            this.asm_.append(", ").append(this.word_sz_).append(", slots);\n");
            break;
          case "<":
            this.asm_.append("lt_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, tmp_, ").append(rhs.getName());
            this.asm_.append(", ").append(this.word_sz_).append(", slots);\n");
            break;
          case "<=":
            this.asm_.append("leq_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, tmp_, ").append(rhs.getName());
            this.asm_.append(", ").append(this.word_sz_).append(", slots);\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        append_idx("tmp = uint64_to_hex_string(" + lhs.getName() + ");\n");
        switch (op) {
          case "+":
            append_idx("evaluator.add_plain(" + rhs.getName() + ", tmp, " + res_ + ");\n");
            break;
          case "*":
            append_idx("evaluator.multiply_plain(" + rhs.getName() + ", tmp, " + res_ + ");\n");
            break;
          case "-":
            append_idx("encryptor.encrypt(tmp, tmp_);\n");
            append_idx("evaluator.sub(tmp_, " + rhs.getName() + ", " + res_ + ");\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "==":
            append_idx(res_);
            this.asm_.append(" = eq_plain(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(rhs.getName());
            this.asm_.append(", tmp, plaintext_modulus);\n");
            break;
          case "!=":
            append_idx(res_);
            this.asm_.append(" = neq_plain(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(rhs.getName());
            this.asm_.append(", tmp, plaintext_modulus);\n");
            break;
          case "<":
            append_idx("encryptor.encrypt(tmp, tmp_);\n");
            append_idx(res_);
            this.asm_.append(" = lt(encryptor, evaluator, ");
            this.asm_.append("relin_keys, tmp_, ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");
            break;
          case "<=":
            append_idx("encryptor.encrypt(tmp, tmp_);\n");
            append_idx(res_);
            this.asm_.append(" = leq(encryptor, evaluator, ");
            this.asm_.append("relin_keys, tmp_, ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
            throw new Exception("Shift over encrypted integers is not possible");
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
      return new Var_t("EncInt", res_);
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      String res_ = new_ctxt_tmp();
      if (this.is_binary_) {
        encrypt("tmp_", new String[]{rhs.getName()});
        this.asm_.append(";\n");
        append_idx(res_ + " = ");
        switch (op) {
          case "+":
            this.asm_.append("add_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, tmp_, ");
            this.asm_.append(rhs.getName()).append(", slots);\n");
            break;
          case "*":
            this.asm_.append("mult_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", slots);\n");
            break;
          case "-":
            this.asm_.append("sub_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, tmp_, ");
            this.asm_.append(rhs.getName()).append(", slots);\n");
            break;
          case "^":
            this.asm_.append("xor_bin(evaluator, relin_keys, ");
            this.asm_.append(lhs.getName()).append(", tmp_, plaintext_modulus);\n");
            break;
          case "==":
            this.asm_.append("eq_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, ").append(lhs.getName());
            this.asm_.append(", tmp_, ").append(this.word_sz_).append(", slots);\n");
            break;
          case "!=":
            this.asm_.append("neq_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, ").append(lhs.getName());
            this.asm_.append(", tmp_, ").append(this.word_sz_).append(", slots);\n");
            break;
          case "<":
            this.asm_.append("lt_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, ").append(lhs.getName());
            this.asm_.append(", tmp_, ").append(this.word_sz_).append(", slots);\n");
            break;
          case "<=":
            this.asm_.append("leq_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, ").append(lhs.getName());
            this.asm_.append(", tmp_, ").append(this.word_sz_).append(", slots);\n");
            break;
          case "<<":
            this.asm_.append("shift_left_bin(encryptor, batch_encoder, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", slots);\n");
            break;
          case ">>":
            this.asm_.append("shift_right_bin(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", slots);\n");
            break;
          case ">>>":
            this.asm_.append("shift_right_logical_bin(encryptor, batch_encoder, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", slots);\n");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        append_idx("tmp = uint64_to_hex_string(" + rhs.getName() + ");\n");
        switch (op) {
          case "+":
            append_idx("evaluator.add_plain(");
            this.asm_.append(lhs.getName()).append(", tmp, ").append(res_).append(");\n");
            break;
          case "*":
            append_idx("evaluator.multiply_plain(");
            this.asm_.append(lhs.getName()).append(", tmp, ").append(res_).append(");\n");
            break;
          case "-":
            append_idx("evaluator.sub_plain(");
            this.asm_.append(lhs.getName()).append(", tmp, ").append(res_).append(");\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "==":
            append_idx(res_);
            this.asm_.append(" = eq_plain(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(lhs.getName());
            this.asm_.append(", tmp, plaintext_modulus);\n");
            break;
          case "!=":
            append_idx(res_);
            this.asm_.append(" = neq_plain(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(lhs.getName());
            this.asm_.append(", tmp, plaintext_modulus);\n");
            break;
          case "<":
            append_idx(res_);
            this.asm_.append(" = lt_plain(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(lhs.getName());
            this.asm_.append(", tmp, plaintext_modulus);\n");
            break;
          case "<=":
            append_idx(res_);
            this.asm_.append(" = leq_plain(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(lhs.getName());
            this.asm_.append(", tmp, plaintext_modulus);\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
            throw new Exception("Shift over encrypted integers is not possible");
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
      return new Var_t("EncInt", res_);
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      String res_ = new_ctxt_tmp();
      if (this.is_binary_) {
        append_idx(res_ + " = ");
        switch (op) {
          case "+":
            this.asm_.append("add_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", slots);\n");
            break;
          case "*":
            this.asm_.append("mult_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", slots);\n");
            break;
          case "-":
            this.asm_.append("sub_bin(evaluator, encryptor, batch_encoder, ");
            this.asm_.append("relin_keys, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", slots);\n");
            break;
          case "^":
            this.asm_.append("xor_bin(evaluator, relin_keys, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");
            break;
          case "==":
            this.asm_.append("eq_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", ").append(this.word_sz_).append(", slots);\n");
            break;
          case "!=":
            this.asm_.append("neq_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", ").append(this.word_sz_).append(", slots);\n");
            break;
          case "<":
            this.asm_.append("lt_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", ").append(this.word_sz_).append(", slots);\n");
            break;
          case "<=":
            this.asm_.append("leq_bin(evaluator, encryptor, batch_encoder, relin_keys, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", ").append(this.word_sz_).append(", slots);\n");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        switch (op) {
          case "+":
            append_idx("evaluator.add(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", ").append(res_).append(");\n");
            break;
          case "*":
            append_idx("evaluator.multiply(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", ").append(res_).append(");\n");
            append_idx("evaluator.relinearize_inplace(");
            this.asm_.append(res_).append(", relin_keys);\n");
            break;
          case "-":
            append_idx("evaluator.sub(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", ").append(res_).append(");\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "==":
            append_idx(res_);
            this.asm_.append(" = eq(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", plaintext_modulus);\n");
            break;
          case "!=":
            append_idx(res_);
            this.asm_.append(" = neq(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", plaintext_modulus);\n");
            break;
          case "<":
            append_idx(res_);
            this.asm_.append(" = lt(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");
            break;
          case "<=":
            append_idx(res_);
            this.asm_.append(" = leq(encryptor, evaluator, ");
            this.asm_.append("relin_keys, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", plaintext_modulus);\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
            throw new Exception("Shift over encrypted integers is not possible");
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
      return new Var_t("EncInt", res_);
    }
    throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
  }

  /**
   * f0 -> "~"
   * f1 -> PrimaryExpression()
   */
  public Var_t visit(BinNotExpression n) throws Exception {
    Var_t exp = n.f1.accept(this);
    String exp_type = st_.findType(exp);
    if (exp_type.equals("int")) {
      if (this.is_binary_) {
        return new Var_t("int", "~" + exp.getName());
      } else {
        return new Var_t("int", "plaintext_modulus + ~" + exp.getName());
      }
    } else if (exp_type.equals("EncInt")) {
      String res_ = new_ctxt_tmp();
      if (this.is_binary_) {
        append_idx(res_ + " = not_bin(evaluator, batch_encoder, ");
        this.asm_.append(exp.getName()).append(", slots);\n");
      } else {
        append_idx("evaluator.negate(");
        this.asm_.append(exp.getName());
        this.asm_.append(", ").append(res_).append(");\n");
        append_idx("tmp = uint64_to_hex_string(1);\n");
        append_idx("evaluator.sub_plain_inplace(" + res_ + ", tmp);\n");
      }
      return new Var_t("EncInt", res_);
    }
    throw new Exception("Wrong type for ~: " + exp_type);
  }

}
