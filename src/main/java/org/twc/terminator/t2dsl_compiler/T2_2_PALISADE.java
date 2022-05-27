package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.Main;
import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_PALISADE extends T2_Compiler {

  protected String vec = "tmp_vec_";
  protected String bin_vec = "tmp_bin_vec_";

  public T2_2_PALISADE(SymbolTable st, String config_file_path,
                       int word_sz) {
    super(st, config_file_path, word_sz);
    if (this.is_binary_) {
      this.st_.backend_types.put("EncInt", "vector<Ciphertext<DCRTPoly>>");
      this.st_.backend_types.put("EncInt[]", "vector<vector<Ciphertext<DCRTPoly>>>");
    } else {
      this.st_.backend_types.put("EncInt", "Ciphertext<DCRTPoly>");
      this.st_.backend_types.put("EncInt[]", "vector<Ciphertext<DCRTPoly>>");
    }
  }

  protected void append_keygen() {
    append_idx("uint32_t depth = 2;\n");
    append_idx("double sigma = 3.2;\n");
    append_idx("SecurityLevel securityLevel = HEStd_128_classic;\n");
    append_idx("size_t plaintext_modulus = 65537;\n");
    append_idx("CryptoContext<DCRTPoly> cc = CryptoContextFactory<\n");
    append_idx("  DCRTPoly>::genCryptoContextBFVrns(plaintext_modulus,\n"); // BFV
    append_idx("    securityLevel, sigma, 0, depth, 0, OPTIMIZED, 2);\n");
    append_idx("cc->Enable(ENCRYPTION);\n");
    append_idx("cc->Enable(SHE);\n");
//    append_idx("cc->Enable(LEVELEDSHE);\n"); // for BGV
    append_idx("auto keyPair = cc->KeyGen();\n");
    append_idx("cc->EvalMultKeyGen(keyPair.secretKey);\n");
    append_idx("size_t slots(cc->GetRingDimension());\n");
    append_idx("vector<int64_t> " + this.vec + "(slots);\n");
    append_idx("Plaintext tmp;\n");
    append_idx("int rots_num = 20;\n");
    append_idx("vector<int> rots(rots_num);\n");
    append_idx("for (int " + this.tmp_i + " = 2; " + this.tmp_i + "< rots_num+2; ");
    this.asm_.append(this.tmp_i).append(" += 2) {\n");
    append_idx("   rots[" + this.tmp_i + " - 2] = " + this.tmp_i + " / 2;\n");
    append_idx("   rots[" + this.tmp_i + " - 1] = -(" + this.tmp_i + " / 2);\n");
    append_idx("}\n");
    append_idx("cc->EvalAtIndexKeyGen(keyPair.secretKey, rots);\n");
    if (is_binary_) {
      append_idx("vector<vector<int64_t>> " + this.bin_vec);
      this.asm_.append("(word_sz, vector<int64_t>(slots, 0));\n");
      append_idx("vector<Ciphertext<DCRTPoly>> tmp_(word_sz);\n\n");
    } else {
      append_idx("Ciphertext<DCRTPoly> tmp_;\n\n");
    }
  }

  protected void encrypt(String dst, String[] src_lst) {
    if (this.is_binary_) {
      for (int slot = 0; slot < src_lst.length; slot++) {
        String src = src_lst[slot];
        boolean is_numeric = isNumeric(src);
        if (is_numeric) {
          int[] bin_array = int_to_bin_array(Long.parseLong(src));
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx(this.bin_vec + "[" + i + "][" + slot + "] = " + bin_array[i] + ";\n");
          }
        } else {
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx(this.bin_vec + "[" + i + "][" + slot + "] = (int64_t)(");
            this.asm_.append(src).append(" >> ").append(this.word_sz_ - i - 1);
            this.asm_.append(") & 1;\n");
          }
        }
      }
      for (int i = 0; i < this.word_sz_; i++) {
        append_idx("tmp = cc->MakePackedPlaintext(");
        this.asm_.append(this.bin_vec).append("[").append(i).append("]);\n");
        append_idx(dst + "[" + i + "] = cc->Encrypt(keyPair.publicKey, tmp)");
        if (i < this.word_sz_ - 1) this.asm_.append(";\n");
      }
    } else {
      if (src_lst.length != 1)
        throw new RuntimeException("encrypt: list length");
      append_idx("fill(" + this.vec + ".begin(), " + this.vec);
      this.asm_.append(".end(), ").append(src_lst[0]).append(");\n");
      append_idx("tmp = cc->MakePackedPlaintext(");
      this.asm_.append(this.vec).append(");\n");
      append_idx(dst + " = cc->Encrypt(keyPair.publicKey, tmp)");
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
    if (this.st_.getScheme() == Main.ENC_TYPE.ENC_DOUBLE) {
      append_idx("#include <iomanip>\n\n");
    }
    append_idx("#include \"palisade.h\"\n");
    append_idx("#include \"../functional_units/functional_units.hpp\"\n\n");
    append_idx("using namespace lbcrypto;\n");
    append_idx("using namespace std;\n\n");
    append_idx("int main(void) {\n");
    this.indent_ = 2;
    if (this.is_binary_) {
      append_idx("size_t word_sz = " + this.word_sz_ + ";\n");
    }
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
        append_idx(lhs.getName() + "[" + this.tmp_i + "].resize(word_sz);\n");
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
            append_idx(lhs.getName() + "[" + i + "].resize(word_sz);\n");
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
       this.asm_.append(" = ").append("inc_bin(cc, ").append(id.getName());
       this.asm_.append(", keyPair.publicKey);\n");
      } else {
        append_idx("fill(" + this.vec + ".begin(), " + this.vec);
        this.asm_.append(".end(), 1);\n");
        append_idx("tmp = cc->MakePackedPlaintext(" + this.vec + ");\n");
        append_idx(id.getName());
        this.asm_.append(" = cc->EvalAdd(tmp, ").append(id.getName()).append(");\n");
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
       this.asm_.append(" = ").append("dec_bin(cc, ").append(id.getName());
       this.asm_.append(", keyPair.publicKey);\n");
      } else {
        append_idx("fill(" + this.vec + ".begin(), " + this.vec);
        this.asm_.append(".end(), 1);\n");
        append_idx("tmp = cc->MakePackedPlaintext(" + this.vec + ");\n");
        append_idx(id.getName());
        this.asm_.append(" = cc->EvalSub(").append(id.getName()).append(", tmp);\n");
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
       append_idx(lhs.getName());
       switch (op) {
         case "+=":
           this.asm_.append(" = add_bin(cc, ");
           break;
         case "*=":
           this.asm_.append(" = mult_bin(cc, ");
           break;
         case "-=":
           this.asm_.append(" = sub_bin(cc, ");
           break;
         default:
           throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
       }
       this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
       this.asm_.append(", keyPair.publicKey)");
      } else {
        append_idx(lhs.getName());
        switch (op) {
          case "+=": this.asm_.append(" = cc->EvalAdd("); break;
          case "*=": this.asm_.append(" = cc->EvalMultAndRelinearize("); break;
          case "-=": this.asm_.append(" = cc->EvalSub("); break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
        this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")");
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      if (this.is_binary_) {
        encrypt("tmp_", new String[]{rhs.getName()});
        this.asm_.append(";\n");
        append_idx(lhs.getName());
        switch (op) {
          case "+=":
            this.asm_.append(" = add_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey)");
            break;
          case "*=":
            this.asm_.append(" = mult_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey)");
            break;
          case "-=":
            this.asm_.append(" = sub_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey)");
            break;
          case "<<=":
            this.asm_.append(" = shift_left_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(", keyPair.publicKey)");
            break;
          case ">>=":
            this.asm_.append(" = shift_right_bin(").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")");
            break;
          case ">>>=":
            this.asm_.append(" = shift_right_logical_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(", keyPair.publicKey)");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        append_idx("fill(" + this.vec + ".begin(), " + this.vec);
        this.asm_.append(".end(), ").append(rhs.getName()).append(");\n");
        append_idx("tmp = cc->MakePackedPlaintext(");
        this.asm_.append(this.vec).append(");\n");
        append_idx(lhs.getName());
        switch (op) {
          case "+=": this.asm_.append(" = cc->EvalAdd("); break;
          case "*=": this.asm_.append(" = cc->EvalMult("); break;
          case "-=": this.asm_.append(" = cc->EvalSub("); break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
        this.asm_.append(lhs.getName()).append(", tmp)");
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
    String idx_type = st_.findType(idx);
    String op = n.f4.accept(this).getName();
    Var_t rhs = n.f5.accept(this);
    String rhs_type = st_.findType(rhs);
    if (id_type.equals("int[]")) {
      append_idx(id.getName());
      this.asm_.append("[").append(idx.getName()).append("] ").append(op);
      this.asm_.append(" ").append(rhs.getName());
    } else if (id_type.equals("EncInt[]")) {
      if (rhs_type.equals("EncInt")) {
        if (this.is_binary_) {
          append_idx(id.getName() + "[" + idx.getName() + "] = ");
          switch (op) {
            case "+=":
              this.asm_.append("add_bin(cc, ");
              break;
            case "*=":
              this.asm_.append("mult_bin(cc, ");
              break;
            case "-=":
              this.asm_.append("sub_bin(cc, ");
              break;
            default:
              throw new Exception("Error in compound array assignment");
          }
          this.asm_.append(id.getName()).append("[").append(idx.getName());
          this.asm_.append("], ").append(rhs.getName()).append(", keyPair.publicKey)");
        } else {
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("]");
          if (op.equals("+=")) {
            this.asm_.append(" = cc->EvalAdd(");
          } else if (op.equals("*=")) {
            this.asm_.append(" = cc->EvalMultAndRelinearize(");
          } else if (op.equals("-=")) {
            this.asm_.append(" = cc->EvalSub(");
          } else {
            throw new Exception("Error in compound array assignment");
          }
          this.asm_.append(id.getName()).append("[").append(idx.getName());
          this.asm_.append("], ").append(rhs.getName()).append(")");
        }
      } else if (rhs_type.equals("int")) {
        if (this.is_binary_) {
          if ("<<=".equals(op)) {
            this.asm_.append(" = shift_left_bin(cc, ").append(id.getName());
            this.asm_.append("[").append(idx.getName()).append("], ");
            this.asm_.append(rhs.getName()).append(", keyPair.publicKey)");
          } else if (">>=".equals(op)) {
            this.asm_.append(" = shift_right_bin(").append(id.getName());
            this.asm_.append("[").append(idx.getName()).append("], ");
            this.asm_.append(rhs.getName()).append(")");
          } else if (">>>=".equals(op)) {
            this.asm_.append(" = shift_right_logical_bin(cc, ").append(id.getName());
            this.asm_.append("[").append(idx.getName()).append("], ");
            this.asm_.append(rhs.getName()).append(", keyPair.publicKey)");
          } else {
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(id.getName() + "[" + idx.getName() + "]");
            if ("+=".equals(op)) {
              this.asm_.append(" = add_bin(cc, ").append(id.getName());
              this.asm_.append("[").append(idx.getName()).append("]");
              this.asm_.append(", tmp_, keyPair.publicKey)");
            } else if ("*=".equals(op)) {
              this.asm_.append(" = mult_bin(cc, ").append(id.getName());
              this.asm_.append("[").append(idx.getName()).append("]");
              this.asm_.append(", tmp_, keyPair.publicKey)");
            } else if ("-=".equals(op)) {
              this.asm_.append(" = sub_bin(cc, ").append(id.getName());
              this.asm_.append("[").append(idx.getName()).append("]");
              this.asm_.append(", tmp_, keyPair.publicKey)");
            } else {
              throw new Exception("Encrypt and move to temporary var.");
            }
          }
        } else {
          append_idx("fill(" + this.vec + ".begin(), " + this.vec);
          this.asm_.append(".end(), ").append(rhs.getName()).append(");\n");
          append_idx("tmp = cc->MakePackedPlaintext(");
          this.asm_.append(this.vec).append(");\n");
          append_idx(id.getName() + "[" + idx.getName() + "]");
          switch (op) {
            case "+=": this.asm_.append(" = cc->EvalAdd("); break;
            case "*=": this.asm_.append(" = cc->EvalMult("); break;
            case "-=": this.asm_.append(" = cc->EvalSub("); break;
            default:
              throw new Exception("Compound array assignment:" + op + " " + rhs_type);
          }
          this.asm_.append(id.getName()).append("[").append(idx.getName());
          this.asm_.append("]").append(", tmp)");
        }
      }
    } else {
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
    String idx_type = st_.findType(idx);
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
          append_idx("vector<int64_t> ");
          this.asm_.append(tmp_vec).append(" = { ").append(exp.getName());
          if (n.f4.present()) {
            for (int i = 0; i < n.f4.size(); i++) {
              this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
            }
          }
          this.asm_.append(" };\n");
          append_idx("tmp = cc->MakePackedPlaintext(");
          this.asm_.append(tmp_vec).append(");\n");
          append_idx(id.getName());
          this.asm_.append(" = cc->Encrypt(keyPair.publicKey, tmp);\n");
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
            String v_type = st_.findType(new Var_t(null, init));
            if (v_type.equals("int") || isNumeric(init)) {
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
    if (!id_type.equals("EncInt[]"))
      throw new RuntimeException("BatchArrayAssignmentStatement");
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
      append_idx("vector<int64_t> ");
      this.asm_.append(tmp_vec).append(" = { ").append(exp.getName());
      if (n.f7.present()) {
        for (int i = 0; i < n.f7.size(); i++) {
          this.asm_.append(", ").append((n.f7.nodes.get(i).accept(this)).getName());
        }
      }
      this.asm_.append(" };\n");
      append_idx("tmp = cc->MakePackedPlaintext(");
      this.asm_.append(tmp_vec).append(");\n");
      append_idx(id.getName());
      this.asm_.append("[").append(index.getName()).append("] = ");
      this.asm_.append("cc->Encrypt(keyPair.publicKey, tmp);\n");
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
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx("cc->Decrypt(keyPair.secretKey,");
            this.asm_.append(expr.getName()).append("[").append(i).append("], &tmp);\n");
            append_idx("tmp->SetLength(slots);\n");
            append_idx(bin_vec + "[" + i + "] = tmp->GetPackedValue();\n");
          }
          append_idx("cout << \"dec(" + expr.getName() + ") = \";\n");
          append_idx("for (size_t " + this.tmp_i + " = 0; ");
          this.asm_.append(this.tmp_i).append(" < word_sz; ++");
          this.asm_.append(this.tmp_i).append(") {\n");
          append_idx("  cout << " + bin_vec + "[" + this.tmp_i + "][0];\n");
          append_idx("}\n");
          append_idx("cout << endl");
        } else {
          append_idx("cc->Decrypt(keyPair.secretKey,");
          this.asm_.append(expr.getName()).append(", &tmp);\n");
          append_idx("tmp->SetLength(1);\n");
          append_idx(this.vec + " = tmp->GetPackedValue();\n");
          append_idx("cout << \"dec(");
          this.asm_.append(expr.getName()).append(") = \" << ");
          this.asm_.append(this.vec).append("[0] << endl");
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
    if (!expr_type.equals("EncInt"))
      throw new RuntimeException("PrintBatchedStatement: expression type");
    Var_t size = n.f4.accept(this);
    String size_type = size.getType();
    if (size_type == null) size_type = st_.findType(size);
    if (!size_type.equals("int"))
      throw new RuntimeException("PrintBatchedStatement: size type");
    if (this.is_binary_) {
      for (int i = 0; i < this.word_sz_; i++) {
        append_idx("cc->Decrypt(keyPair.secretKey,");
        this.asm_.append(expr.getName()).append("[").append(i).append("], &tmp);\n");
        append_idx("tmp->SetLength(slots);\n");
        append_idx(this.bin_vec + "[" + i + "] = tmp->GetPackedValue();\n");
      }
      append_idx("cout << \"dec(" + expr.getName() + ") = \";\n");
      String tmp_s = "tmp_s";
      append_idx("for (int " + tmp_s + " = 0; ");
      this.asm_.append(tmp_s).append(" < ").append(size.getName());
      this.asm_.append("; ++").append(tmp_s).append(") {\n");
      this.indent_ += 2;
      append_idx("for (size_t " + this.tmp_i + " = 0; ");
      this.asm_.append(this.tmp_i).append(" < word_sz; ++");
      this.asm_.append(this.tmp_i).append(") {\n");
      this.indent_ += 2;
      append_idx("cout << " + this.bin_vec + "[" + this.tmp_i + "][" + tmp_s + "];\n");
      this.indent_ -= 2;
      append_idx("}\n");
      append_idx("cout << \" \";\n");
      this.indent_ -= 2;
      append_idx("}\n");
      append_idx("cout << endl");
    } else {
      append_idx("cc->Decrypt(keyPair.secretKey, ");
      this.asm_.append(expr.getName()).append(", ").append("&tmp);\n");
      append_idx("tmp->SetLength(");
      this.asm_.append(size.getName()).append(");\n");
      append_idx(this.vec + "  = tmp->GetPackedValue();\n");
      append_idx("cout << \"dec(" + expr.getName() + ") = \";\n");
      append_idx("for (auto v : " + this.vec + ") {\n");
      append_idx("  cout << v << \" \";\n");
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
    if (!expr_type.equals("EncInt"))
      throw new RuntimeException("ReduceNoiseStatement");
    append_idx("cc->ModReduceInPlace(");
    this.asm_.append(expr.getName()).append(");\n");
    return null;
  }
  
  /**
   * f0 -> <ROTATE_LEFT>
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ","
   * f4 -> Expression()
   * f5 -> ")"
   */
  public Var_t visit(RotateLeftStatement n) throws Exception {
    String ctxt = n.f2.accept(this).getName();
    String amnt = n.f4.accept(this).getName();
    if (this.is_binary_) {
      append_idx("for (size_t " + this.tmp_i + " = 0; " + this.tmp_i + " < ");
      this.asm_.append(this.word_sz_).append("; ++").append(this.tmp_i).append(") {\n");
      append_idx("  " + ctxt + "[" + this.tmp_i + "] = cc->EvalAtIndex(" + ctxt);
      this.asm_.append("[").append(tmp_i).append("], ");
      this.asm_.append(amnt).append(");\n");
      append_idx("}\n");
    } else {
      append_idx(ctxt + " = cc->EvalAtIndex(" + ctxt + ", ");
      this.asm_.append(amnt).append(");\n");
    }
    return null;
  }

  /**
   * f0 -> <ROTATE_RIGHT>
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ","
   * f4 -> Expression()
   * f5 -> ")"
   */
  public Var_t visit(RotateRightStatement n) throws Exception {
    String ctxt = n.f2.accept(this).getName();
    String amnt = n.f4.accept(this).getName();
    if (this.is_binary_) {
      append_idx("for (size_t " + this.tmp_i + " = 0; " + this.tmp_i + " < ");
      this.asm_.append(this.word_sz_).append("; ++").append(this.tmp_i).append(") {\n");
      append_idx("  " + ctxt + "[" + this.tmp_i + "] = cc->EvalAtIndex(" + ctxt);
      this.asm_.append("[").append(tmp_i).append("], -");
      this.asm_.append(amnt).append(");\n");
      append_idx("}\n");
    } else {
      append_idx(ctxt + " = cc->EvalAtIndex(" + ctxt + ", -");
      this.asm_.append(amnt).append(");\n");
    }
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
        append_idx(res_);
        switch (op) {
          case "+":
            this.asm_.append(" = add_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");;
            break;
          case "*":
            this.asm_.append(" = mult_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");;
            break;
          case "-":
            this.asm_.append(" = sub_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");;
            break;
          case "^":
            this.asm_.append(" = xor_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");;
            break;
          case "&":
            this.asm_.append(" = and_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");;
            break;
          case "|":
            this.asm_.append(" = or_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");;
            break;
          case "==":
            this.asm_.append(" = eq_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");;
            break;
          case "!=":
            this.asm_.append(" = neq_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");;
            break;
          case "<":
            this.asm_.append(" = lt_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", word_sz, keyPair.publicKey);\n");
            break;
          case "<=":
            this.asm_.append(" = leq_bin(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");;
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
        append_idx("vector<int64_t> " + tmp_vec + "(slots, " + lhs.getName() + ");\n");
        append_idx("tmp = cc->MakePackedPlaintext(" + tmp_vec + ");\n");
        switch (op) {
          case "+":
            append_idx(res_ + " = cc->");
            this.asm_.append("EvalAdd(").append(rhs.getName()).append(", tmp);\n");
            break;
          case "*":
            append_idx(res_ + " = cc->");
            this.asm_.append("EvalMult(").append(rhs.getName()).append(", tmp);\n");
            break;
          case "-":
            append_idx(res_ + " = cc->");
            this.asm_.append("EvalSub(tmp, ").append(rhs.getName()).append(");\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "&":
            throw new Exception("Bitwise AND over encrypted integers is not possible");
          case "|":
            throw new Exception("Bitwise OR over encrypted integers is not possible");
          case "==":
            append_idx("tmp_ = cc->Encrypt(keyPair.publicKey, tmp);\n");
            append_idx(res_);
            this.asm_.append(" = eq(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");
            break;
          case "!=":
            append_idx("tmp_ = cc->Encrypt(keyPair.publicKey, tmp);\n");
            append_idx(res_);
            this.asm_.append(" = neq(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");
            break;
          case "<":
            append_idx("tmp_ = cc->Encrypt(keyPair.publicKey, tmp);\n");
            append_idx(res_);
            this.asm_.append(" = lt(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey, plaintext_modulus);\n");
            break;
          case "<=":
            append_idx("tmp_ = cc->Encrypt(keyPair.publicKey, tmp);\n");
            append_idx(res_);
            this.asm_.append(" = leq(cc, tmp_, ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey, plaintext_modulus);\n");
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
        append_idx(res_);
        switch (op) {
          case "+":
            this.asm_.append(" = add_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey);\n");
            break;
          case "*":
            this.asm_.append(" = mult_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey);\n");
            break;
          case "-":
            this.asm_.append(" = sub_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey);\n");
            break;
          case "^":
            this.asm_.append(" = xor_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, plaintext_modulus);\n");;
            break;
          case "&":
            this.asm_.append(" = and_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, plaintext_modulus);\n");;
            break;
          case "|":
            this.asm_.append(" = or_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, plaintext_modulus);\n");;
            break;
          case "==":
            this.asm_.append(" = eq_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey);\n");
            break;
          case "!=":
            this.asm_.append(" = neq_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey);\n");
            break;
          case "<":
            this.asm_.append(" = lt_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, word_sz, keyPair.publicKey);\n");
            break;
          case "<=":
            this.asm_.append(" = leq_bin(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey);\n");
            break;
          case "<<":
            this.asm_.append(" = shift_left_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");
            break;
          case ">>":
            this.asm_.append(" = shift_right_bin(").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(");\n");
            break;
          case ">>>":
            this.asm_.append(" = shift_right_logical_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(", keyPair.publicKey);\n");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
        append_idx("vector<int64_t> " + tmp_vec + "(slots, " + rhs.getName() + ");\n");
        append_idx("tmp = cc->MakePackedPlaintext(" + tmp_vec+ ");\n");
        switch (op) {
          case "+":
            append_idx(res_ + " = cc->");
            this.asm_.append("EvalAdd(").append(lhs.getName()).append(", tmp);\n");
            break;
          case "*":
            append_idx(res_ + " = cc->");
            this.asm_.append("EvalMult(").append(lhs.getName()).append(", tmp);\n");
            break;
          case "-":
            append_idx(res_ + " = cc->");
            this.asm_.append("EvalSub(").append(lhs.getName()).append(", tmp);\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "&":
            throw new Exception("Bitwise AND over encrypted integers is not possible");
          case "|":
            throw new Exception("Bitwise OR over encrypted integers is not possible");
          case "==":
            append_idx("tmp_ = cc->Encrypt(keyPair.publicKey, tmp);\n");
            append_idx(res_);
            this.asm_.append(" = eq(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, plaintext_modulus);\n");
            break;
          case "!=":
            append_idx("tmp_ = cc->Encrypt(keyPair.publicKey, tmp);\n");
            append_idx(res_);
            this.asm_.append(" = neq(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, plaintext_modulus);\n");
            break;
          case "<":
            append_idx("tmp_ = cc->Encrypt(keyPair.publicKey, tmp);\n");
            append_idx(res_);
            this.asm_.append(" = lt(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey, plaintext_modulus);\n");
            break;
          case "<=":
            append_idx("tmp_ = cc->Encrypt(keyPair.publicKey, tmp);\n");
            append_idx(res_);
            this.asm_.append(" = leq(cc, ").append(lhs.getName());
            this.asm_.append(", tmp_, keyPair.publicKey, plaintext_modulus);\n");
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
        append_idx(res_);
        switch (op) {
          case "+":
            this.asm_.append(" = add_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");
            break;
          case "*":
            this.asm_.append(" = mult_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");
            break;
          case "-":
            this.asm_.append(" = sub_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");
            break;
          case "^":
            this.asm_.append(" = xor_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");
            break;
          case "&":
            this.asm_.append(" = and_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");
            break;
          case "|":
            this.asm_.append(" = or_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", plaintext_modulus);\n");
            break;
          case "==":
            this.asm_.append(" = eq_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");
            break;
          case "!=":
            this.asm_.append(" = neq_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");
            break;
          case "<":
            this.asm_.append(" = lt_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", word_sz, keyPair.publicKey);\n");
            break;
          case "<=":
            this.asm_.append(" = leq_bin(cc, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName());
            this.asm_.append(", keyPair.publicKey);\n");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        switch (op) {
          case "+":
            append_idx(res_ + " = cc->");
            this.asm_.append("EvalAdd(").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(");\n");
            break;
          case "*":
            append_idx(res_ + " = cc->");
            this.asm_.append("EvalMultAndRelinearize(").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(");\n");
            break;
          case "-":
            append_idx(res_ + " = cc->");
            this.asm_.append("EvalSub(").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(");\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "&":
            throw new Exception("Bitwise AND over encrypted integers is not possible");
          case "|":
            throw new Exception("Bitwise OR over encrypted integers is not possible");
          case "==":
            append_idx(res_);
            this.asm_.append(" = eq(cc, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", plaintext_modulus);\n");
            break;
          case "!=":
            append_idx(res_);
            this.asm_.append(" = neq(cc, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(", plaintext_modulus);\n");
            break;
          case "<":
            append_idx(res_);
            this.asm_.append(" = lt(cc, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName());
            this.asm_.append(", keyPair.publicKey, plaintext_modulus);\n");
            break;
          case "<=":
            append_idx(res_);
            this.asm_.append(" = leq(cc, ").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName());
            this.asm_.append(", keyPair.publicKey, plaintext_modulus);\n");
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
        return new Var_t("int", "~" + exp.getName());
    } else if (exp_type.equals("EncInt")) {
      String res_ = new_ctxt_tmp();
      if (this.is_binary_) {
        append_idx(res_ + " = not_bin(cc, " + exp.getName() + ");\n");
      } else {
        append_idx(res_ + " = cc->EvalNegate(" + exp.getName() + ");\n");
        append_idx("fill(" + this.vec + ".begin(), " + this.vec);
        this.asm_.append(".end(), 1);\n");
        append_idx("tmp = cc->MakePackedPlaintext(" + this.vec + ");\n");
        append_idx(res_ + " = cc->EvalSub(" + res_ + ", tmp);\n");
      }
      return new Var_t("EncInt", res_);
    }
    throw new Exception("Wrong type for ~: " + exp_type);
  }

  /**
   * f0 -> "("
   * f1 -> Expression()
   * f2 -> ")"
   * f3 -> "?"
   * f4 -> Expression()
   * f5 -> ":"
   * f6 -> Expression()
   */
  public Var_t visit(TernaryExpression n) throws Exception {
    Var_t cond = n.f1.accept(this);
    Var_t e1 = n.f4.accept(this);
    Var_t e2 = n.f6.accept(this);
    String cond_t = st_.findType(cond);
    String e1_t = st_.findType(e1);
    String e2_t = st_.findType(e2);
    String res_;
    if (cond_t.equals("bool") || cond_t.equals("int") || cond_t.equals("double")) {
      if (e1_t.equals(e2_t)) {
        res_ = "tmp_" + (++tmp_cnt_);
        append_idx(this.st_.backend_types.get(e1_t) + " " + res_ + " = (");
        this.asm_.append(cond.getName()).append(")").append(" ? ");
        this.asm_.append(e1.getName()).append(" : ").append(e2.getName()).append(";\n");
        return new Var_t(e1_t, res_);
      } else if ((e1_t.equals("EncInt") || e1_t.equals("EncDouble")) &&
                  (e2_t.equals("int") || e2_t.equals("double")) ) {
        res_ = new_ctxt_tmp();
        String e2_enc = new_ctxt_tmp();
        encrypt(e2_enc, new String[]{e2.getName()});
        this.asm_.append(";\n");
        append_idx(res_ + " = (" + cond.getName() + ") ? " + e1.getName());
        this.asm_.append(" : ").append(e2_enc).append(";\n");
        return new Var_t(e1_t, res_);
      } else if ((e2_t.equals("EncInt") || e2_t.equals("EncDouble")) &&
                  (e1_t.equals("int") || e1_t.equals("double")) ) {
        res_ = new_ctxt_tmp();
        String e1_enc = new_ctxt_tmp();
        encrypt(e1_enc, new String[]{e1.getName()});
        this.asm_.append(";\n");
        append_idx(res_ + " = (" + cond.getName() + ") ? " + e1_enc);
        this.asm_.append(" : ").append(e2.getName()).append(";\n");
        return new Var_t(e2_t, res_);
      }
    } else if (cond_t.equals("EncInt") || cond_t.equals("EncDouble")) {
      res_ = new_ctxt_tmp();
      if (e1_t.equals(e2_t)) {
        if (e1_t.equals("int") || e1_t.equals("double")) {
          String e1_enc = new_ctxt_tmp(), e2_enc = new_ctxt_tmp();
          encrypt(e1_enc, new String[]{e1.getName()});
          this.asm_.append(";\n");
          encrypt(e2_enc, new String[]{e2.getName()});
          this.asm_.append(";\n");
          if (this.is_binary_) {
            append_idx(res_ + " = mux_bin(cc, " + cond.getName() + ", ");
          } else {
            append_idx(res_ + " = mux(cc, " + cond.getName() + ", ");
          }
          this.asm_.append(e1_enc).append(", ").append(e2_enc);
          this.asm_.append(");\n");
          return new Var_t("EncInt", res_);
        } else if (e1_t.equals("EncInt") || e1_t.equals("EncDouble")) {
          if (this.is_binary_) {
            append_idx(res_ + " = mux_bin(cc, " + cond.getName() + ", ");
          } else {
            append_idx(res_ + " = mux(cc, " + cond.getName() + ", ");
          }
          this.asm_.append(e1.getName()).append(", ").append(e2.getName());
          this.asm_.append(");\n");
          return new Var_t(e1_t, res_);
        }
      } else if ((e1_t.equals("EncInt") || e1_t.equals("EncDouble")) &&
                  (e2_t.equals("int") || e2_t.equals("double")) ) {
        String e2_enc = new_ctxt_tmp();
        encrypt(e2_enc, new String[]{e2.getName()});
        this.asm_.append(";\n");
        if (this.is_binary_) {
          append_idx(res_ + " = mux_bin(cc, " + cond.getName() + ", ");
        } else {
          append_idx(res_ + " = mux(cc, " + cond.getName() + ", ");
        }
        this.asm_.append(e1.getName()).append(", ").append(e2_enc).append(");\n");
        return new Var_t(e1_t, res_);
      } else if ((e2_t.equals("EncInt") || e2_t.equals("EncDouble")) &&
                  (e1_t.equals("int") || e1_t.equals("double")) ) {
        String e1_enc = new_ctxt_tmp();
        encrypt(e1_enc, new String[]{e1.getName()});
        this.asm_.append(";\n");
        if (this.is_binary_) {
          append_idx(res_ + " = mux_bin(cc, " + cond.getName() + ", ");
        } else {
          append_idx(res_ + " = mux(cc, " + cond.getName() + ", ");
        }
        this.asm_.append(e1_enc).append(", ").append(e2.getName()).append(");\n");
        return new Var_t(e2_t, res_);
      }
    }
    throw new RuntimeException("Ternary condition error: " +
            cond.getName() + " type: " + cond_t +
            e1.getName() + " type: " + e1_t +
            e2.getName() + " type: " + e2_t);
  }

}
