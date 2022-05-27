package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.Main;
import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_HElib extends T2_Compiler {

  private final boolean bootstrapping_;

  public T2_2_HElib(SymbolTable st, String config_file_path, int word_sz,
                    boolean bootstrapping) {
    super(st, config_file_path, word_sz);
    this.bootstrapping_ = bootstrapping;
    if (this.is_binary_) {
      this.st_.backend_types.put("EncInt", "vector<Ctxt>");
      this.st_.backend_types.put("EncInt[]", "vector<vector<Ctxt>>");
    } else {
      this.st_.backend_types.put("EncInt", "Ctxt");
      this.st_.backend_types.put("EncInt[]", "vector<Ctxt>");
    }
  }

  protected String new_ctxt_tmp() {
    String ctxt_tmp_ = "tmp_" + (tmp_cnt_++) + "_";
    append_idx(this.st_.backend_types.get("EncInt"));
    this.asm_.append(" ").append(ctxt_tmp_).append("(");
    if (this.is_binary_) {
      this.asm_.append(this.word_sz_).append(", scratch);\n");
    } else {
      this.asm_.append("public_key);\n");
    }
    return ctxt_tmp_;
  }

  protected void assign_to_all_slots(String lhs, String rhs, String rhs_idx) {
    append_idx("for (int ");
    String tmp_j = this.tmp_i + "j";
    this.asm_.append(tmp_j).append(" = 0; ").append(tmp_j);
    this.asm_.append(" < slots; ").append(tmp_j).append("++) {\n");
    this.indent_ += 2;
    append_idx(lhs);
    this.asm_.append("[").append(tmp_j);
    if (this.st_.getScheme() == Main.ENC_TYPE.ENC_INT) {
      this.asm_.append("] = ");
      if (rhs_idx != null) {
        this.asm_.append(rhs).append("[").append(rhs_idx).append("];\n");
      } else {
        this.asm_.append(rhs).append(";\n");
      }
    } else if (this.st_.getScheme() == Main.ENC_TYPE.ENC_DOUBLE) {
      if (rhs_idx != null) {
        this.asm_.append("] = std::complex<double>(");
        this.asm_.append(rhs).append("[").append(rhs_idx).append("], 0);\n");
      } else {
        this.asm_.append("] = std::complex<double>(");
        this.asm_.append(rhs).append(", 0);\n");
      }
    }
    this.indent_ -= 2;
    append_idx("}\n");
  }

  protected void append_keygen() {
    if (this.is_binary_) {
      if (this.bootstrapping_) {
        append_idx("unsigned long p = 2, m = 4095, r = 1, bits = 500;\n");
      } else {
        append_idx("unsigned long p = 2, m = 16383, r = 1, bits = 250;\n");
      }
    } else {
      append_idx("unsigned long p = 509, m = 14481, r = 1, bits = 120;\n");
    }
    if (this.bootstrapping_) {
      append_idx("vector<long> mvec = {7, 5, 9, 13};\n");
      append_idx("vector<long> gens = {2341, 3277, 911};\n");
      append_idx("vector<long> ords = {6, 4, 6};\n");
    }
    append_idx("unsigned long c = 2;\n");
    append_idx("Context context = helib::ContextBuilder<helib::BGV>()\n");
    if (this.bootstrapping_) {
      append_idx("  .m(m).p(p).r(r).gens(gens).ords(ords).bits(bits)\n");
      append_idx("  .c(c).bootstrappable(true).mvec(mvec).build();\n");
    } else {
      append_idx("  .m(m).p(p).r(r).bits(bits).c(c).build();\n");
    }
    append_idx("SecKey secret_key(context);\n");
    append_idx("secret_key.GenSecKey();\n");
    append_idx("addSome1DMatrices(secret_key);\n");
    if (this.bootstrapping_) {
      append_idx("addFrbMatrices(secret_key);\n");
      append_idx("secret_key.genRecryptData();\n");
    }
    append_idx("PubKey& public_key = secret_key;\n");
    append_idx("const EncryptedArray& ea = context.getEA();\n");
    if (this.is_binary_) {
      append_idx("std::vector<helib::zzX> unpackSlotEncoding;\n");
      append_idx("buildUnpackSlotEncoding(unpackSlotEncoding, ea);\n");
    }
    append_idx("long slots = ea.size();\n");
    append_idx("Ptxt<helib::BGV> tmp(context);\n");
    append_idx("Ctxt scratch(public_key);\n");
    append_idx(this.st_.backend_types.get("EncInt") + " tmp_");
    if (this.is_binary_) {
      this.asm_.append("(").append(this.word_sz_).append(", scratch);\n\n");
    } else {
      this.asm_.append("(public_key);\n\n");
    }
  }

  protected void encrypt(String dst, String[] src_lst) {
    if (this.is_binary_) {
      String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
      append_idx("vector<Ptxt<helib::BGV>> " + tmp_vec + "(");
      this.asm_.append(this.word_sz_).append(", tmp);\n");
      for (int slot = 0; slot < src_lst.length; slot++) {
        String src = src_lst[slot];
        boolean is_numeric = isNumeric(src);
        if (is_numeric) {
          int[] bin_array = int_to_bin_array(Long.parseLong(src));
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx(tmp_vec + "[" + (this.word_sz_ - i - 1) + "][" + slot);
            this.asm_.append("] = ").append(bin_array[i]).append(";\n");
          }
        } else {
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx(tmp_vec + "[" + i + "][" + slot + "] = (");
            this.asm_.append(src).append(" >> ").append(i);
            this.asm_.append(") & 1;\n");
          }
        }
      }
      for (int i = 0; i < this.word_sz_; i++) {
        append_idx("public_key.Encrypt(" + dst + "[" + i + "], ");
        this.asm_.append(tmp_vec).append("[").append(i).append("]").append(")");
        if (i < this.word_sz_ - 1) this.asm_.append(";\n");
      }
    } else {
      if (src_lst.length != 1)
        throw new RuntimeException("encrypt: list length");
      assign_to_all_slots("tmp", src_lst[0], null);
      append_idx("public_key.Encrypt(" + dst + ", tmp)");
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
    append_idx("#include <helib/helib.h>\n");
    append_idx("#include <helib/intraSlot.h>\n");
    append_idx("#include \"../functional_units/functional_units.hpp\"\n\n");
    append_idx("using namespace helib;\n");
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
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ( VarDeclarationRest() )*
   * f3 -> ";"
   */
  public Var_t visit(VarDeclaration n) throws Exception {
    append_idx("");
    String type = n.f0.accept(this).getType();
    this.asm_.append(this.st_.backend_types.get(type));
    this.asm_.append(" ");
    Var_t id = n.f1.accept(this);
    this.asm_.append(id.getName());
    if (type.equals("EncInt") || type.equals("EncDouble")) {
      if (this.is_binary_) {
        this.asm_.append("(").append(this.word_sz_).append(", scratch)");
      } else {
        this.asm_.append("(public_key)");
      }
    }
    if (n.f2.present()) {
      for (int i = 0; i < n.f2.size(); i++) {
        n.f2.nodes.get(i).accept(this);
        if (type.equals("EncInt") || type.equals("EncDouble")) {
          if (this.is_binary_) {
            this.asm_.append("(").append(this.word_sz_).append(", scratch)");
          } else {
            this.asm_.append("(public_key)");
          }
        }
      }
    }
    this.asm_.append(";\n");
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
      if (this.is_binary_) {
        this.asm_.append(".resize(").append(rhs_name).append(".size(), vector<Ctxt>(");
        this.asm_.append(this.word_sz_).append(", scratch));\n");
      } else {
        this.asm_.append(".resize(").append(rhs_name).append(".size(), scratch);\n");
      }
      append_idx("for (size_t ");
      this.asm_.append(this.tmp_i).append(" = 0; ").append(this.tmp_i).append(" < ");
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
      append_idx(lhs.getName());
      if (rhs_name.startsWith("resize(")) {
        this.asm_.append(".");
      } else {
        this.asm_.append(" = ");
      }
      this.asm_.append(rhs_name);
      this.semicolon_ = true;
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
        encrypt("tmp_", new String[]{"1"});
        this.asm_.append(";\n");
        append_idx(id.getName() + " = add_bin(public_key, " + id.getName());
        this.asm_.append(", tmp_, unpackSlotEncoding, slots);\n");
        bootstrapBin(id, "", false);
      } else {
        append_idx(id.getName());
        this.asm_.append(".addConstant(NTL::ZZX(1));\n");
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
        encrypt("tmp_", new String[]{"1"});
        this.asm_.append(";\n");
        append_idx(id.getName() + " = sub_bin(public_key, " + id.getName());
        this.asm_.append(", tmp_, unpackSlotEncoding, slots);\n");
        bootstrapBin(id, "", false);
      } else {
        append_idx(id.getName());
        this.asm_.append(".addConstant(NTL::ZZX(-1));\n");
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
            this.asm_.append("add_bin(public_key, ");
            break;
          case "*=":
            this.asm_.append("mult_bin(public_key, ");
            break;
          case "-=":
            this.asm_.append("sub_bin(public_key, ");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
        this.asm_.append(lhs.getName()).append(", ");
        this.asm_.append(rhs.getName()).append(", ");
        this.asm_.append("unpackSlotEncoding, slots)");
        bootstrapBin(lhs, "", true);
        this.semicolon_ = false;
        return null;
      } else {
        append_idx(lhs.getName());
        switch (op) {
          case "+=":
            this.asm_.append(" += ").append(rhs.getName());
            break;
          case "*=":
            this.asm_.append(".multiplyBy(");
            this.asm_.append(rhs.getName()).append(")");
            break;
          case "-=":
            this.asm_.append(" -= ").append(rhs.getName());
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      if (this.is_binary_) {
        switch (op) {
          case "+=":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(lhs.getName() + " = ");
            this.asm_.append("add_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots)");
            break;
          case "*=":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(lhs.getName() + " = ");
            this.asm_.append("mult_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots)");
            break;
          case "-=":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(lhs.getName() + " = ");
            this.asm_.append("sub_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots)");
            break;
          case "<<=":
            append_idx(lhs.getName() + " = ");
            this.asm_.append("shift_left_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")");
            break;
          case ">>=":
            append_idx(lhs.getName() + " = ");
            this.asm_.append("shift_right_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")");
            break;
          case ">>>=":
            append_idx(lhs.getName() + " = ");
            this.asm_.append("shift_right_logical_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
        bootstrapBin(lhs, "", true);
        this.semicolon_ = false;
        return null;
      } else {
        append_idx(lhs.getName());
        switch (op) {
          case "+=":
            this.asm_.append(".addConstant(NTL::ZZX(");
            break;
          case "*=":
            this.asm_.append(".multByConstant(NTL::ZZX(");
            break;
          case "-=":
            this.asm_.append(".addConstant(NTL::ZZX(-");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
        this.asm_.append(rhs.getName()).append("))");
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
              this.asm_.append("add_bin(public_key, ");
              break;
            case "*=":
              this.asm_.append("mult_bin(public_key, ");
              break;
            case "-=":
              this.asm_.append("sub_bin(public_key, ");
              break;
            default:
              throw new Exception("Bad operand types: EncInt " + op + " " + rhs_type);
          }
          this.asm_.append(id.getName()).append("[").append(idx.getName());
          this.asm_.append("], ").append(rhs.getName());
          this.asm_.append(", unpackSlotEncoding, slots)");
          bootstrapBin(id, idx.getName(), true);
          this.semicolon_ = false;
          return null;
        } else {
          append_idx(id.getName() + "[" + idx.getName() + "] ");
          if (op.equals("+=") || op.equals("-=")) {
            this.asm_.append(op).append(" ").append(rhs.getName());
          } else if (op.equals("*=")) {
            this.asm_.append(".multiplyBy(");
            this.asm_.append(rhs.getName()).append(")");
          } else {
            throw new Exception("Error in compound array assignment");
          }
        }
      } else if (rhs_type.equals("int")) {
        if (this.is_binary_) {
          if ("<<=".equals(op)) {
            this.asm_.append("shift_left_bin(public_key, ");
            this.asm_.append(id.getName()).append("[").append(idx.getName());
            this.asm_.append("], ").append(rhs.getName()).append(")");
          } else if (">>=".equals(op)) {
            this.asm_.append("shift_right_bin(public_key, ");
            this.asm_.append(id.getName()).append("[").append(idx.getName());
            this.asm_.append("], ").append(rhs.getName()).append(")");
          } else if (">>>=".equals(op)) {
            this.asm_.append("shift_right_logical_bin(public_key, ");
            this.asm_.append(id.getName()).append("[").append(idx.getName());
            this.asm_.append("], ").append(rhs.getName()).append(")");
          } else {
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(id.getName() + "[" + idx.getName() + "] = ");
            if ("+=".equals(op)) {
              this.asm_.append("add_bin(public_key, ").append(id.getName());
              this.asm_.append("[").append(idx.getName());
              this.asm_.append("], tmp_, unpackSlotEncoding, slots)");
            } else if ("*=".equals(op)) {
              this.asm_.append("mult_bin(public_key, ").append(id.getName());
              this.asm_.append("[").append(idx.getName());
              this.asm_.append("], tmp_, unpackSlotEncoding, slots)");
            } else if ("-=".equals(op)) {
              this.asm_.append("sub_bin(public_key, ").append(id.getName());
              this.asm_.append("[").append(idx.getName());
              this.asm_.append("], tmp_, unpackSlotEncoding, slots)");
            } else {
              throw new Exception("Encrypt and move to temporary var.");
            }
          }
          bootstrapBin(id, idx.getName(), true);
          this.semicolon_ = false;
          return null;
        } else {
          switch (op) {
            case "*=":
              append_idx(id.getName() + "[" + idx.getName());
              this.asm_.append("].multByConstant(NTL::ZZX(");
              this.asm_.append(rhs.getName()).append("))");
              break;
            case "+=":
              append_idx(id.getName() + "[" + idx.getName());
              this.asm_.append("].addConstant(NTL::ZZX(");
              this.asm_.append(rhs.getName()).append("))");
              break;
            default:
              throw new Exception("Encrypt and move to temporary var.");
          }
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
          this.asm_.append(rhs.getName());
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
          append_idx("tmp[0] = ");
          this.asm_.append(exp.getName()).append(";\n");
          if (n.f4.present()) {
            for (int i = 0; i < n.f4.size(); i++) {
              append_idx("tmp[");
              this.asm_.append(i + 1).append("] = ");
              this.asm_.append((n.f4.nodes.get(i).accept(this)).getName()).append(";\n");
            }
          }
          append_idx("public_key.Encrypt(");
          this.asm_.append(id.getName()).append(", tmp);\n");
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
      append_idx("tmp[0] = " + exp.getName() + ";\n");
      if (n.f7.present()) {
        for (int i = 0; i < n.f7.size(); i++) {
          append_idx("tmp[");
          this.asm_.append(i + 1).append("] = ");
          this.asm_.append((n.f7.nodes.get(i).accept(this)).getName()).append(";\n");
        }
      }
      append_idx("public_key.Encrypt(" + id.getName());
      this.asm_.append("[").append(index.getName()).append("], tmp);\n");
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
          append_idx("cout << \"dec(");
          this.asm_.append(expr.getName()).append(") = \";\n");
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx("secret_key.Decrypt(tmp, " + expr.getName() + "[");
            this.asm_.append(this.word_sz_ - i - 1).append("]);\n");
            append_idx("cout << static_cast<long>(tmp[0]);\n");
          }
          append_idx("cout << endl");
        } else {
          append_idx("secret_key.Decrypt(tmp, ");
          this.asm_.append(expr.getName()).append(");\n");
          append_idx("cout << \"dec(");
          this.asm_.append(expr.getName()).append(") = \" << static_cast<long>(tmp[0]) << endl");
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
      append_idx("cout << \"dec(");
      this.asm_.append(expr.getName()).append(") = \";\n");
      append_idx("for (int " + this.tmp_i + " = 0; ");
      this.asm_.append(this.tmp_i).append(" < ").append(size.getName());
      this.asm_.append("; ++").append(this.tmp_i).append(") {\n");
      this.indent_ += 2;
      for (int i = 0; i < this.word_sz_; i++) {
        append_idx("secret_key.Decrypt(tmp, " + expr.getName());
        this.asm_.append("[").append(this.word_sz_ - i - 1).append("]);\n");
        append_idx("cout << static_cast<long>(tmp[" + this.tmp_i + "]);\n");
      }
      append_idx("cout << \" \";\n");
    } else {
      append_idx("secret_key.Decrypt(tmp, " + expr.getName() + ");\n");
      append_idx("cout << \"dec(");
      this.asm_.append(expr.getName()).append(") = \";\n");
      append_idx("for (int " + this.tmp_i + " = 0; " + this.tmp_i + " < ");
      this.asm_.append(size.getName()).append("; ++").append(this.tmp_i).append(") {\n");
      this.indent_ += 2;
      append_idx("cout << static_cast<long>(tmp[" + this.tmp_i + "]) << \" \";\n");
    }
    this.indent_ -= 2;
    append_idx("}\n");
    append_idx("cout << endl");
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
    // Calls mod switch automatically.
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
      append_idx("  ea.rotate(" + ctxt + "[" + this.tmp_i + "], -");
      this.asm_.append(amnt).append(");\n");
      append_idx("}\n");
    } else {
      append_idx("ea.rotate(" + ctxt + ", -");
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
      append_idx("  ea.rotate(" + ctxt + "[" + this.tmp_i + "], ");
      this.asm_.append(amnt).append(");\n");
      append_idx("}\n");
    } else {
      append_idx("ea.rotate(" + ctxt + ", ");
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
      Var_t ret_var = new Var_t("EncInt", res_);
      if (this.is_binary_) {
        encrypt("tmp_", new String[]{lhs.getName()});
        this.asm_.append(";\n");
        append_idx(res_ + " = ");
        switch (op) {
          case "+":
            this.asm_.append("add_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "*":
            this.asm_.append("mult_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "-":
            this.asm_.append("sub_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "^":
            this.asm_.append("xor_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", p);\n");
            break;
          case "&":
            this.asm_.append("and_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", p);\n");
            break;
          case "|":
            this.asm_.append("or_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", p);\n");
            break;
          case "==":
            this.asm_.append("eq_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "!=":
            this.asm_.append("neq_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "<":
            this.asm_.append("lt_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "<=":
            this.asm_.append("leq_bin(public_key, tmp_, ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          default:
            throw new Exception("Bad operand types: EncInt " + op + " " + rhs_type);
        }
      } else {
        switch (op) {
          case "+":
            append_idx(res_);
            this.asm_.append(" = ").append(rhs.getName()).append(";\n");
            append_idx(res_);
            this.asm_.append(".addConstant(NTL::ZZX(").append(lhs.getName()).append("));\n");
            break;
          case "*":
            append_idx(res_);
            this.asm_.append(" = ").append(rhs.getName()).append(";\n");
            append_idx(res_);
            this.asm_.append(".multByConstant(NTL::ZZX(").append(lhs.getName()).append("));\n");
            break;
          case "-":
            assign_to_all_slots("tmp", lhs.getName(), null);
            append_idx("public_key.Encrypt(");
            this.asm_.append(res_).append(", tmp);\n");
            append_idx(res_);
            this.asm_.append(" -= ").append(rhs.getName()).append(";\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "&":
            throw new Exception("Bitwise AND over encrypted integers is not possible");
          case "|":
            throw new Exception("Bitwise OR over encrypted integers is not possible");
          case "==":
            assign_to_all_slots("tmp", lhs.getName(), null);
            append_idx(res_);
            this.asm_.append(" = eq_plain(public_key, ");
            this.asm_.append(rhs.getName()).append(", tmp, p, slots);\n");
            break;
          case "!=":
            assign_to_all_slots("tmp", lhs.getName(), null);
            append_idx(res_);
            this.asm_.append(" = neq_plain(public_key, ");
            this.asm_.append(rhs.getName()).append(", tmp, p, slots);\n");
            break;
          case "<":
            assign_to_all_slots("tmp", lhs.getName(), null);
            append_idx(res_);
            this.asm_.append(" = lt_plain(public_key, tmp, ");
            this.asm_.append(rhs.getName()).append(", p, slots);\n");
            break;
          case "<=":
            assign_to_all_slots("tmp", lhs.getName(), null);
            append_idx(res_);
            this.asm_.append(" = leq_plain(public_key, tmp, ");
            this.asm_.append(rhs.getName()).append(", p, slots);\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
            throw new Exception("Shift over encrypted integers is not possible");
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
      bootstrapBin(ret_var, "", false);
      return ret_var;
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      String res_ = new_ctxt_tmp();
      Var_t ret_var = new Var_t("EncInt", res_);
      if (this.is_binary_) {
        switch (op) {
          case "+":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("add_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots);\n");
            break;
          case "*":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("mult_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots);\n");
            break;
          case "-":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("sub_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots);\n");
            break;
          case "^":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("xor_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, p);\n");
            break;
          case "&":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("and_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, p);\n");
            break;
          case "|":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("or_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, p);\n");
            break;
          case "==":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("eq_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots);\n");
            break;
          case "!=":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("neq_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots);\n");
            break;
          case "<":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("lt_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots);\n");
            break;
          case "<=":
            encrypt("tmp_", new String[]{rhs.getName()});
            this.asm_.append(";\n");
            append_idx(res_ + " = ");
            this.asm_.append("leq_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", tmp_, unpackSlotEncoding, slots);\n");
            break;
          case "<<":
            append_idx(res_ + " = ");
            this.asm_.append("shift_left_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(");\n");
            break;
          case ">>":
            append_idx(res_ + " = ");
            this.asm_.append("shift_right_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(");\n");
            break;
          case ">>>":
            append_idx(res_ + " = ");
            this.asm_.append("shift_right_logical_bin(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(");\n");
            break;
          default:
            throw new Exception("Bad operand types: EncInt " + op + " " + rhs_type);
        }
      } else {
        append_idx(res_);
        this.asm_.append(" = ").append(lhs.getName()).append(";\n");
        switch (op) {
          case "+":
            append_idx(res_ + ".addConstant(NTL::ZZX(");
            this.asm_.append(rhs.getName()).append("));\n");
            break;
          case "*":
            append_idx(res_ + ".multByConstant(NTL::ZZX(");
            this.asm_.append(rhs.getName()).append("));\n");
            break;
          case "-":
            append_idx(res_ + ".addConstant(NTL::ZZX(-");
            this.asm_.append(rhs.getName()).append("));\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "&":
            throw new Exception("AND over encrypted integers is not possible");
          case "|":
            throw new Exception("OR over encrypted integers is not possible");
          case "==":
            assign_to_all_slots("tmp", rhs.getName(), null);
            append_idx(res_ + " = eq_plain(public_key, ");
            this.asm_.append(lhs.getName()).append(", tmp, p, slots);\n");
            break;
          case "!=":
            assign_to_all_slots("tmp", rhs.getName(), null);
            append_idx(res_ + " = neq_plain(public_key, ");
            this.asm_.append(lhs.getName()).append(", tmp, p, slots);\n");
            break;
          case "<":
            assign_to_all_slots("tmp", rhs.getName(), null);
            append_idx(res_ + " = lt_plain(public_key, ");
            this.asm_.append(lhs.getName()).append(", tmp, p, slots);\n");
            break;
          case "<=":
            assign_to_all_slots("tmp", rhs.getName(), null);
            append_idx(res_ + " = leq_plain(public_key, ");
            this.asm_.append(lhs.getName()).append(", tmp, p, slots);\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
            throw new Exception("Shift over encrypted integers is not possible");
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
      bootstrapBin(ret_var, "", false);
      return ret_var;
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      String res_ = new_ctxt_tmp();
      Var_t ret_var = new Var_t("EncInt", res_);
      if (this.is_binary_) {
        append_idx(res_ + " = ");
        switch (op) {
          case "+":
            this.asm_.append("add_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "*":
            this.asm_.append("mult_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "-":
            this.asm_.append("sub_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "^":
            this.asm_.append("xor_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", p);\n");
            break;
          case "&":
            this.asm_.append("and_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", p);\n");
            break;
          case "|":
            this.asm_.append("or_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", p);\n");
            break;
          case "==":
            this.asm_.append("eq_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "!=":
            this.asm_.append("neq_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "<":
            this.asm_.append("lt_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          case "<=":
            this.asm_.append("leq_bin(public_key, ");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
            this.asm_.append(", unpackSlotEncoding, slots);\n");
            break;
          default:
            throw new Exception("Bad operand types: EncInt " + op + " " + rhs_type);
        }
      } else {
        switch (op) {
          case "*":
          case "+":
          case "-":
            append_idx(res_);
            this.asm_.append(" = ").append(lhs.getName()).append(";\n");
            append_idx(res_);
            this.asm_.append(" ").append(op).append("= ");
            this.asm_.append(rhs.getName()).append(";\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "&":
            throw new Exception("Bitwise AND over encrypted integers is not possible");
          case "|":
            throw new Exception("Bitwise OR over encrypted integers is not possible");
          case "==":
            append_idx(res_);
            this.asm_.append(" = eq(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(", p, slots);\n");
            break;
          case "!=":
            append_idx(res_);
            this.asm_.append(" = neq(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(", p, slots);\n");
            break;
          case "<":
            append_idx(res_);
            this.asm_.append(" = lt(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(", p, slots);\n");
            break;
          case "<=":
            append_idx(res_);
            this.asm_.append(" = leq(public_key, ").append(lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(", p, slots);\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
            throw new Exception("Shift over encrypted integers is not possible");
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
      bootstrapBin(ret_var, "", false);
      return ret_var;
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
      if (this.is_binary_) {
        String res_ = new_ctxt_tmp();
        append_idx(res_ + " = not_bin(public_key, " + exp.getName() + ");\n");
        return new Var_t("EncInt", res_);
      } else {
        append_idx(exp.getName() + ".negate();\n");
        append_idx(exp.getName() + ".addConstant(NTL::ZZX(-1));\n");
        return new Var_t("EncInt", exp.getName());
      }
    }
    throw new Exception("Wrong type for ~: " + exp_type);
  }

  /**
   * f0 -> "new"
   * f1 -> "EncInt"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public Var_t visit(EncryptedArrayAllocationExpression n) throws Exception {
    String size = n.f3.accept(this).getName();
    if (this.is_binary_) {
      return new Var_t("EncInt[]", "resize(" + size +
                        ", vector<Ctxt>(" + this.word_sz_ + ", scratch))");
    } else {
      return new Var_t("EncInt[]", "resize(" + size + ", scratch)");
    }
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
      Var_t res_var = new Var_t("EncInt", res_);
      if (e1_t.equals(e2_t)) {
        if (e1_t.equals("int") || e1_t.equals("double")) {
          String e1_enc = new_ctxt_tmp(), e2_enc = new_ctxt_tmp();
          encrypt(e1_enc, new String[]{e1.getName()});
          this.asm_.append(";\n");
          encrypt(e2_enc, new String[]{e2.getName()});
          this.asm_.append(";\n");
          if (this.is_binary_) {
            append_idx(res_ + " = mux_bin(public_key, " + cond.getName() + ", ");
          } else {
            append_idx(res_ + " = mux(public_key, " + cond.getName() + ", ");
          }
          this.asm_.append(e1_enc).append(", ").append(e2_enc).append(");\n");
        } else if (e1_t.equals("EncInt") || e1_t.equals("EncDouble")) {
          if (this.is_binary_) {
            append_idx(res_ + " = mux_bin(public_key, " + cond.getName() + ", ");
          } else {
            append_idx(res_ + " = mux(public_key, " + cond.getName() + ", ");
          }
          this.asm_.append(e1.getName()).append(", ").append(e2.getName());
          this.asm_.append(");\n");
          res_var.setType(e1_t);
        }
      } else if ((e1_t.equals("EncInt") || e1_t.equals("EncDouble")) &&
                  (e2_t.equals("int") || e2_t.equals("double")) ) {
        String e2_enc = new_ctxt_tmp();
        encrypt(e2_enc, new String[]{e2.getName()});
        this.asm_.append(";\n");
        if (this.is_binary_) {
          append_idx(res_ + " = mux_bin(public_key, " + cond.getName() + ", ");
        } else {
          append_idx(res_ + " = mux(public_key, " + cond.getName() + ", ");
        }
        this.asm_.append(e1.getName()).append(", ").append(e2_enc).append(");\n");
        res_var.setType(e1_t);
      } else if ((e2_t.equals("EncInt") || e2_t.equals("EncDouble")) &&
                  (e1_t.equals("int") || e1_t.equals("double")) ) {
        String e1_enc = new_ctxt_tmp();
        encrypt(e1_enc, new String[]{e1.getName()});
        this.asm_.append(";\n");
        if (this.is_binary_) {
          append_idx(res_ + " = mux_bin(public_key, " + cond.getName() + ", ");
        } else {
          append_idx(res_ + " = mux(public_key, " + cond.getName() + ", ");
        }
        this.asm_.append(e1_enc).append(", ").append(e2.getName()).append(");\n");
        res_var.setType(e2_t);
      }
      bootstrapBin(res_var, "", false);
      return res_var;
    }
    throw new RuntimeException("Ternary condition error: " +
            cond.getName() + " type: " + cond_t +
            e1.getName() + " type: " + e1_t +
            e2.getName() + " type: " + e2_t);
  }

  private void bootstrapBin(Var_t res_var, String array, boolean newline) {
    if (newline) this.asm_.append(";\n");
    if (this.is_binary_ && this.bootstrapping_) {
      append_idx("if (" + res_var.getName() + array + "[0].bitCapacity() <= 150) {\n");
      append_idx("  for (int " + this.tmp_i + " = 0; " + this.tmp_i);
      this.asm_.append("< ").append(this.word_sz_).append("; ");
      this.asm_.append(this.tmp_i).append("++) {\n");
      append_idx("    public_key.thinReCrypt(" + res_var.getName() + array +"[");
      this.asm_.append(this.tmp_i).append("]);\n");
      append_idx("  }\n");
      append_idx("}\n");
    }
  }

}
