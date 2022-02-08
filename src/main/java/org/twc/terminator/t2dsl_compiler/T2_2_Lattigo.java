package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.Main;
import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_Lattigo extends T2_Compiler {

  protected boolean is_tmp_declared_ = false;

  public T2_2_Lattigo(SymbolTable st, String config_file_path,
                      int word_sz) {
    super(st, config_file_path, word_sz);
    this.st_.backend_types.put("scheme", "bfv");
    this.st_.backend_types.put("int", "int64");
    this.st_.backend_types.put("int[]", "[]int64");
    if (this.is_binary_) {
      this.st_.backend_types.put("EncInt", "[]*bfv.Ciphertext");
      this.st_.backend_types.put("EncInt[]", "[][]*bfv.Ciphertext");
    } else {
      this.st_.backend_types.put("EncInt", "*bfv.Ciphertext");
      this.st_.backend_types.put("EncInt[]", "[]*bfv.Ciphertext");
    }
  }

  protected void assign_to_all_slots(String lhs, String rhs, String rhs_idx) {
    append_idx("for ");
    String tmp_j = this.tmp_i + "j";
    this.asm_.append(tmp_j).append(" := 0; ").append(tmp_j);
    this.asm_.append(" < slots; ").append(tmp_j).append("++ {\n");
    this.indent_ += 2;
    append_idx(lhs);
    this.asm_.append("[").append(tmp_j);
    if (this.st_.getScheme() == Main.ENC_TYPE.ENC_INT) {
      this.asm_.append("] = int64(");
      if (rhs_idx != null) {
        this.asm_.append(rhs).append("[").append(rhs_idx).append("])\n");
      } else {
        this.asm_.append(rhs).append(")\n");
      }
    } else if (this.st_.getScheme() == Main.ENC_TYPE.ENC_DOUBLE) {
      if (rhs_idx != null) {
        this.asm_.append("] = complex(float64(");
        this.asm_.append(rhs).append("[").append(rhs_idx).append("]), 0)\n");
      } else {
        this.asm_.append("] = complex(float64(");
        this.asm_.append(rhs).append("), 0)\n");
      }
    }
    this.indent_ -= 2;
    append_idx("}\n");
  }

  protected void append_keygen() {
    append_idx("// BFV parameters (128 bit security) with plaintext modulus 65929217\n");
    append_idx("paramDef := bfv.PN13QP218\n");
    append_idx("paramDef.T = 0x3ee0001\n");
    append_idx("params, err := bfv.NewParametersFromLiteral(paramDef)\n");
    append_idx("slots := 2048\n");
    append_idx("if err != nil {\n");
    append_idx("  panic(err)\n");
    append_idx("}\n");
    append_idx("encoder := bfv.NewEncoder(params)\n");
    append_idx("kgen := bfv.NewKeyGenerator(params)\n");
    append_idx("clientSk, clientPk := kgen.GenKeyPair()\n");
    append_idx("encryptorPk := bfv.NewEncryptor(params, clientPk)\n");
    append_idx("encryptorSk := bfv.NewEncryptor(params, clientSk)\n");
    append_idx("decryptor := bfv.NewDecryptor(params, clientSk)\n");
    append_idx("rlk := kgen.GenRelinearizationKey(clientSk, 1)\n");
    append_idx("evaluator := bfv.NewEvaluator(params, rlwe.EvaluationKey{Rlk: rlk})\n");
    append_idx("funits.FunitsInit(&encryptorPk, &encoder, &evaluator, " +
                   "&params, int(paramDef.T), slots, " + this.word_sz_ + ")\n");
    append_idx("ptxt := bfv.NewPlaintext(params)\n");
    append_idx("tmp := make([]int64, slots)\n");
    append_idx("encoder.EncodeInt(tmp, ptxt)\n\n");
  }

  protected void declare_tmp_if_not_declared() {
    if (this.is_tmp_declared_) return;
    this.is_tmp_declared_ = true;
    if (this.is_binary_) {
      append_idx("tmp_ := make(" + this.st_.backend_types.get("EncInt"));
      this.asm_.append(", ").append(this.word_sz_).append(")").append("\n");
    } else {
      append_idx("tmp_ := encryptorPk.EncryptNew(ptxt)\n");
    }
  }

  /**
   * f0 -> Block()
   *       | ArrayAssignmentStatement() ";"
   *       | BatchAssignmentStatement() ";"
   *       | BatchArrayAssignmentStatement() ";"
   *       | AssignmentStatement() ";"
   *       | IncrementAssignmentStatement()
   *       | DecrementAssignmentStatement()
   *       | CompoundAssignmentStatement()
   *       | CompoundArrayAssignmentStatement()
   *       | IfStatement()
   *       | WhileStatement()
   *       | ForStatement()
   *       | PrintStatement() ";"
   *       | PrintBatchedStatement() ";"
   *       | ReduceNoiseStatement() ";"
   */
  public Var_t visit(Statement n) throws Exception {
    n.f0.accept(this);
    if (this.semicolon_) {
      this.asm_.append("\n");
    }
    this.semicolon_ = false;
    return null;
  }

  protected void encrypt(String dst, String[] src_lst) {
    if (this.is_binary_) {
      String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
      append_idx(tmp_vec + " := make([][]int64, " + this.word_sz_ + ")\n");
      append_idx("for " + this.tmp_i + " := range " + tmp_vec + " {\n");
      this.indent_ += 2;
      append_idx(tmp_vec + "[" + this.tmp_i + "] = make([]int64, slots)\n");
      this.indent_ -= 2;
      append_idx("}\n");
      for (int slot = 0; slot < src_lst.length; slot++) {
        String src = src_lst[slot];
        boolean is_numeric = isNumeric(src);
        if (is_numeric) {
          int[] bin_array = int_to_bin_array(Integer.parseInt(src));
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx(tmp_vec + "[" + i + "][" + slot + "] = " + bin_array[i] + "\n");
          }
        } else {
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx(tmp_vec + "[" + i + "][" + slot + "] = (");
            this.asm_.append(src).append(" >> ").append(this.word_sz_ - i - 1);
            this.asm_.append(") & 1\n");
          }
        }
      }
      for (int i = 0; i < this.word_sz_; i++) {
        append_idx("encoder.EncodeInt(" + tmp_vec + "[" + i + "], ptxt)\n");
        append_idx(dst + "[" + i + "] = encryptorSk.EncryptNew(ptxt)");
        if (i < this.word_sz_ - 1) this.asm_.append("\n");
      }
    } else {
      if (src_lst.length != 1)
        throw new RuntimeException("encrypt: list length");
      assign_to_all_slots("tmp", src_lst[0], null);
      append_idx("encoder.EncodeInt(tmp, ptxt)\n");
      append_idx(dst + " = encryptorSk.EncryptNew(ptxt)");
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
    append_idx("package main\n\n");
    append_idx("import (\n");
    append_idx("  \"fmt\"\n");
    append_idx("  \"time\"\n");
    append_idx("  \"github.com/ldsec/lattigo/v2/rlwe\"\n");
    append_idx("  \"github.com/ldsec/lattigo/v2/" + this.st_.backend_types.get("scheme")+ "\"\n");
    append_idx("  funits \"Lattigo/functional_units\"\n");
    append_idx(")\n\n");
    append_idx("func main() {\n");
    this.indent_ = 2;
    if (!read_keygen_from_file()) {
      append_keygen();
    }
    n.f6.accept(this);
    n.f7.accept(this);
    n.f9.accept(this);
    this.indent_ = 0;
    append_idx("}\n");
    return null;
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ( VarDeclarationRest() )*
   * f3 -> ";"
   */
  public Var_t visit(VarDeclaration n) throws Exception {
    String type = n.f0.accept(this).getType();
    Var_t id = n.f1.accept(this);
    if (this.is_binary_ && type.equals("EncInt")) {
      append_idx(id.getName() + " := make(" + this.st_.backend_types.get(type));
      this.asm_.append(", ").append(this.word_sz_).append(")\n");
      if (n.f2.present()) {
        for (int i = 0; i < n.f2.size(); i++) {
          append_idx("");
          n.f2.nodes.get(i).accept(this);
          this.asm_.append(" := make(").append(this.st_.backend_types.get(type));
          this.asm_.append(", ").append(this.word_sz_).append(")\n");
        }
      }
    } else {
      append_idx("var ");
      this.asm_.append(id.getName());
      if (n.f2.present()) {
        for (int i = 0; i < n.f2.size(); i++) {
          n.f2.nodes.get(i).accept(this);
        }
      }
      this.asm_.append(" ").append(this.st_.backend_types.get(type));
      this.asm_.append("\n");
    }
    return null;
  }

  /**
   * f0 -> ","
   * f1 -> Identifier()
   */
  public Var_t visit(VarDeclarationRest n) throws Exception {
    Var_t id = n.f1.accept(this);
    String id_t = st_.findType(id);
    if (!(this.is_binary_ && (id_t.equals("EncInt") || id_t.equals("EncDouble")))) {
      this.asm_.append(", ");
    }
    this.asm_.append(id.getName());
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
      append_idx(lhs.getName() + " = make(");
      this.asm_.append(this.st_.backend_types.get("EncInt[]")).append(", len(");
      this.asm_.append(rhs_name).append("))\n");
      if (this.is_binary_) {
        append_idx("for " + this.tmp_i + " := range " + lhs.getName() + " {\n");
        this.indent_ += 2;
        append_idx(lhs.getName() + "[" + this.tmp_i + "] = make(");
        this.asm_.append(this.st_.backend_types.get("EncInt")).append(", slots)\n");
        this.indent_ -= 2;
        append_idx("}\n");
      }
      append_idx("for ");
      this.asm_.append(this.tmp_i).append(" := 0; ").append(this.tmp_i);
      this.asm_.append(" <  len(").append(rhs_name).append("); ");
      this.asm_.append(this.tmp_i).append("++ {\n");
      this.indent_ += 2;
      encrypt(lhs.getName() + "[" + this.tmp_i + "]",
              new String[]{rhs_name + "[" + this.tmp_i + "]"});
      this.asm_.append("\n");
      this.indent_ -= 2;
      append_idx("}\n");
    } else if (lhs_type.equals(rhs_type)) {
      // if the destination has the same type as the source.
      if (this.is_binary_ && (lhs_type.equals("EncInt") || lhs_type.equals("EncInt[]"))) {
        if (rhs_name.startsWith("resize(")) {
          int rhs_new_size = 0;
          rhs_new_size = Integer.parseInt(rhs_name.substring(7, rhs_name.length()-1));
          append_idx(lhs.getName() + " = make(");
          this.asm_.append(this.st_.backend_types.get("EncInt[]")).append(", ");
          this.asm_.append(rhs_new_size).append(")\n");
          for (int i = 0; i < rhs_new_size; i++) {
            append_idx(lhs.getName() + "[" + i + "] = make(");
            this.asm_.append(this.st_.backend_types.get("EncInt"));
            this.asm_.append(", ").append(this.word_sz_).append(")\n");
          }
        } else {
          append_idx(lhs.getName() + " = " + rhs_name);
          this.semicolon_ = true;
        }
      } else {
        append_idx(lhs.getName());
        if (rhs_name.startsWith("resize(")) {
          this.asm_.append(" = make(");
          this.asm_.append(this.st_.backend_types.get(rhs_type)).append(", ");
          this.asm_.append(rhs_name.substring(7));
        } else {
          this.asm_.append(" = ");
          this.asm_.append(rhs_name);
        }
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
        append_idx(id.getName() + " = funits.BinInc(" + id.getName() + ")");
      } else {
        declare_tmp_if_not_declared();
        encrypt("tmp_", new String[]{"1"});
        this.asm_.append("\n");
        append_idx(id.getName() + " = evaluator.AddNew(");
        this.asm_.append(id.getName()).append(", tmp_)");
      }
    } else {
      append_idx(id.getName() + "++");
    }
    this.semicolon_ = true;
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
        append_idx(id.getName() + " = funits.BinDec(" + id.getName() + ")");
      } else {
        declare_tmp_if_not_declared();
        encrypt("tmp_", new String[]{"1"});
        this.asm_.append("\n");
        append_idx(id.getName() + " = evaluator.SubNew(");
        this.asm_.append(id.getName()).append(", tmp_)");
      }
    } else {
      append_idx(id.getName() + "--");
    }
    this.semicolon_ = true;
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
      this.asm_.append(" ").append(op).append(" ").append(rhs.getName());
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      if (this.is_binary_) {
        switch (op) {
          case "+=":
            append_idx(lhs.getName() + " = funits.BinAdd(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")");
            break;
          case "*=":
            append_idx(lhs.getName() + " = funits.BinMult(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")");
            break;
          case "-=":
            append_idx(lhs.getName() + " = funits.BinSub(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }      
      } else {
        switch (op) {
          case "+=":
            append_idx(lhs.getName() + " = evaluator.");
            this.asm_.append("AddNew(").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(")");
            break;
          case "*=":
            declare_tmp_if_not_declared();
            append_idx("tmp_ = evaluator.");
            this.asm_.append("MulNew(").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(")\n");
            append_idx(lhs.getName() + " = evaluator.RelinearizeNew(tmp_)");
            break;
          case "-=":
            append_idx(lhs.getName() + " = evaluator.");
            this.asm_.append("SubNew(").append(lhs.getName()).append(", ");
            this.asm_.append(rhs.getName()).append(")");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      declare_tmp_if_not_declared();
      encrypt("tmp_", new String[]{rhs.getName()});
      this.asm_.append("\n");
      if (this.is_binary_) {
        switch (op) {
          case "+=":
            append_idx(lhs.getName() + " = funits.BinAdd(" + lhs.getName() + ", tmp_)");
            break;
          case "*=":
            append_idx(lhs.getName() + " = funits.BinMult(" + lhs.getName() + ", tmp_)");
            break;
          case "-=":
            append_idx(lhs.getName() + " = funits.BinSub(" + lhs.getName() + ", tmp_)");
            break;
          case "<<=":
            append_idx(lhs.getName() + " = funits.BinShiftLeft(" + lhs.getName() + ", " + rhs.getName() + ")");
            break;
          case ">>=":
            append_idx(lhs.getName() + " = funits.BinShiftRight(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")");
            break;
          case ">>>=":
            append_idx(lhs.getName() + " = funits.BinShiftRightLogical(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        switch (op) {
          case "+=":
            append_idx(lhs.getName() + " = evaluator.");
            this.asm_.append("AddNew(").append(lhs.getName()).append(", tmp_)");
            break;
          case "*=":
            declare_tmp_if_not_declared();
            append_idx("tmp_ = evaluator.");
            this.asm_.append("MulNew(").append(lhs.getName()).append(", tmp_)\n");
            append_idx(lhs.getName() + " = evaluator.RelinearizeNew(tmp_)");
            break;
          case "-=":
            append_idx(lhs.getName() + " = evaluator.");
            this.asm_.append("SubNew(").append(lhs.getName()).append(", tmp_)");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
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
                append_idx("funits.BinAdd(" + id.getName() + "[" + idx.getName() + "], ");
                this.asm_.append(rhs.getName()).append(")");
                break;
              case "*=":
                append_idx("funits.BinMult(" + id.getName() + "[" + idx.getName() + "], ");
                this.asm_.append(rhs.getName()).append(")");
                break;
              case "-=":
                append_idx("funits.BinSub(" + id.getName() + "[" + idx.getName() + "], ");
                this.asm_.append(rhs.getName()).append(")");
                break;
              default:
                throw new Exception("Error in compound array assignment");
            }
          } else {
            switch (op) {
              case "+=":
                append_idx(id.getName() + "[" + idx.getName() + "]");
                this.asm_.append(" = evaluator.AddNew(").append(id.getName());
                this.asm_.append("[").append(idx.getName()).append("], ");
                this.asm_.append(rhs.getName()).append(")");
                break;
              case "*=":
                declare_tmp_if_not_declared();
                this.asm_.append("tmp_ = evaluator.MulNew(").append(id.getName());
                this.asm_.append("[").append(idx.getName()).append("], ");
                this.asm_.append(rhs.getName()).append(")\n");
                append_idx(id.getName() + "[" + idx.getName());
                this.asm_.append("] = evaluator.RelinearizeNew(tmp_)");
                break;
              case "-=":
                append_idx(id.getName() + "[" + idx.getName() + "]");
                this.asm_.append(" = evaluator.SubNew(").append(id.getName());
                this.asm_.append("[").append(idx.getName()).append("], ");
                this.asm_.append(rhs.getName()).append(")");
                break;
              default:
                throw new Exception("Error in compound array assignment");
            }
          }
          break;
        } else if (rhs_type.equals("int")) {
          switch (op) {
            case "<<=":
              append_idx("funits.BinShiftLeft(" + id.getName() + "[");
              this.asm_.append(idx.getName()).append("], ");
              this.asm_.append(rhs.getName()).append(")");
              break;
            case ">>=":
              append_idx("funits.BinShiftRight(" + id.getName() + "[");
              this.asm_.append(idx.getName()).append("], ");
              this.asm_.append(rhs.getName()).append(")");
              break;
            case ">>>=":
              append_idx("funits.BinShiftRightLogical(" + id.getName() + "[");
              this.asm_.append(idx.getName()).append("], ");
              this.asm_.append(rhs.getName()).append(")");
              break;
            default:
              throw new Exception("Encrypt and move to temporary var.");
          }
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
        this.asm_.append("[").append(idx.getName()).append("] = int64(");
        this.asm_.append(rhs.getName()).append(")");
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
        append_idx(id.getName() + " = []int64{ " + exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" }\n");
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
          this.asm_.append("\n");
        } else {
          String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
          append_idx(tmp_vec + " := []int64{ " + exp.getName());
          if (n.f4.present()) {
            for (int i = 0; i < n.f4.size(); i++) {
              this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
            }
          }
          this.asm_.append(" }\n");
          append_idx("encoder.EncodeInt(" + tmp_vec + ", ptxt)\n");
          append_idx(id.getName() + " = encryptorSk.EncryptNew(ptxt)\n");
        }
        break;
      case "EncInt[]":
        String exp_var;
        if (exp_type.equals("int")) {
          exp_var = "tmp_" + (++tmp_cnt_) + "_";
          if (this.is_binary_) {
            append_idx(exp_var + " := make([]*bfv.Ciphertext, slots)\n");
          } else {
            append_idx("var " + exp_var + " *bfv.Ciphertext\n");
          }
          encrypt(exp_var, new String[]{exp.getName()});
          this.asm_.append("\n");
        } else { // exp type is EncInt
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            if (exp_type.equals("int")) {
              String tmp_ = "tmp_" + (++tmp_cnt_) + "_";
              if (this.is_binary_) {
                append_idx(tmp_ + " := make([]*bfv.Ciphertext, slots)\n");
              } else {
                append_idx("var " + tmp_ + " *bfv.Ciphertext\n");
              }
              encrypt(tmp_, new String[]{init});
              this.asm_.append("\n");
              inits.add(tmp_);
            } else { // exp type is EncInt
              inits.add(init);
            }
          }
        }
        append_idx(id.getName() + " = " + this.st_.backend_types.get("EncInt[]"));
        this.asm_.append("{ ").append(exp_var);
        for (String init : inits) {
          this.asm_.append(", ").append(init);
        }
        this.asm_.append(" }\n");
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
      this.asm_.append("\n");
    } else {
      String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
      append_idx(tmp_vec + " := []int64{ " + exp.getName());
      if (n.f7.present()) {
        for (int i = 0; i < n.f7.size(); i++) {
          this.asm_.append(", ").append((n.f7.nodes.get(i).accept(this)).getName());
        }
      }
      this.asm_.append(" }\n");
      append_idx("encoder.EncodeInt(" + tmp_vec + ", ptxt)\n");
      append_idx(id.getName() + "[" + index.getName() + "] = encryptorSk.EncryptNew(ptxt)\n");
    }
    return null;
  }

  /**
   * f0 -> "if"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public Var_t visit(IfthenStatement n) throws Exception {
    append_idx("if ");
    Var_t cond = n.f2.accept(this);
    this.asm_.append(cond.getName());
    n.f4.accept(this);
    return null;
  }

  /**
   * f0 -> "if"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   * f5 -> "else"
   * f6 -> Statement()
   */
  public Var_t visit(IfthenElseStatement n) throws Exception {
    append_idx("if ");
    Var_t cond = n.f2.accept(this);
    this.asm_.append(cond.getName());
    n.f4.accept(this);
    append_idx("else");
    n.f6.accept(this);
    return null;
  }

  /**
   * f0 -> "while"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public Var_t visit(WhileStatement n) throws Exception {
    append_idx("for ");
    Var_t cond = n.f2.accept(this);
    this.asm_.append(cond.getName());
    n.f4.accept(this);
    return null;
  }

  /**
   * f0 -> "for"
   * f1 -> "("
   * f2 -> AssignmentStatement()
   * f3 -> ";"
   * f4 -> Expression()
   * f5 -> ";"
   * f6 -> ( AssignmentStatement() | IncrementAssignmentStatement() | DecrementAssignmentStatement() | CompoundAssignmentStatement() )
   * f7 -> ")"
   * f8 -> Statement()
   */
  public Var_t visit(ForStatement n) throws Exception {
    append_idx("for ");
    int prev_indent = this.indent_;
    this.indent_ = 0;
    n.f2.accept(this);
    this.asm_.append("; ");
    Var_t cond = n.f4.accept(this);
    this.asm_.append(cond.getName()).append("; ");
    n.f6.accept(this);
    this.indent_ = prev_indent;
    n.f8.accept(this);
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
        append_idx("fmt.Println(" + expr.getName() + ")\n");
        break;
      case "EncInt":
        if (this.is_binary_) {
          append_idx("fmt.Print(\"dec(");
          this.asm_.append(expr.getName()).append(") = \")\n");
          for (int i = 0; i < this.word_sz_; i++) {
            append_idx("ptxt = decryptor.DecryptNew(" + expr.getName());
            this.asm_.append("[").append(i).append("])\n");
            append_idx("fmt.Print(encoder.DecodeIntNew(ptxt)[0])\n");
          }
          append_idx("fmt.Println()\n");
        } else {
          append_idx("ptxt = decryptor.DecryptNew(" + expr.getName() + ")\n");
          append_idx("fmt.Print(\"dec(");
          this.asm_.append(expr.getName()).append(") = \")\n");
          append_idx("fmt.Println(encoder.DecodeIntNew(ptxt)[0])\n");
        }
        break;
      default:
        throw new Exception("Bad type for print statement");
    }
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
    if (size_type == null) size_type = st_.findType(expr);
    if (!size_type.equals("int"))
      throw new RuntimeException("PrintBatchedStatement: size type");
    if (this.is_binary_) {
      append_idx("fmt.Print(\"dec(");
      this.asm_.append(expr.getName()).append(") = \")\n");
      append_idx("for " + this.tmp_i + " := 0; ");
      this.asm_.append(this.tmp_i).append(" < ").append(size.getName());
      this.asm_.append("; ").append(this.tmp_i).append("++ {\n");
      this.indent_ += 2;
      for (int i = 0; i < this.word_sz_; i++) {
        append_idx("ptxt = decryptor.DecryptNew(" + expr.getName());
        this.asm_.append("[").append(i).append("])\n");
        append_idx("fmt.Print(encoder.DecodeIntNew(ptxt)[" + this.tmp_i + "])\n");
      }
      append_idx("fmt.Print(\" \")\n");
      this.indent_ -= 2;
      append_idx("}\n");
      append_idx("fmt.Println()\n");
    } else {
      append_idx("ptxt = decryptor.DecryptNew(" + expr.getName() + ")\n");
      append_idx("fmt.Print(\"dec(");
      this.asm_.append(expr.getName()).append(") = \")\n");
      append_idx("for " + this.tmp_i + " := 0; ");
      this.asm_.append(this.tmp_i).append(" < ").append(size.getName());
      this.asm_.append("; ").append(this.tmp_i).append("++ {\n");
      append_idx("  fmt.Print(encoder.DecodeIntNew(ptxt)[" + this.tmp_i + "])\n");
      append_idx("  fmt.Print(\" \")\n");
      append_idx("}\n");
      append_idx("fmt.Println()\n");
    }
    return null;
  }

  /**
   * f0 -> <REDUCE_NOISE>
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   */
  public Var_t visit(ReduceNoiseStatement n) throws Exception {
    return null;
  }

  /**
   * f0 -> <START_TIMER>
   * f1 -> "("
   * f2 -> ")"
   */
  public Var_t visit(StartTimerStatement n) throws Exception {
    append_idx(this.tstart_ + " ");
    if (!this.timer_used_) this.asm_.append(":");
    this.asm_.append("= time.Now()\n");
    return null;
  }

  /**
   * f0 -> <END_TIMER>
   * f1 -> "("
   * f2 -> ")"
   */
  public Var_t visit(StopTimerStatement n) throws Exception {
    append_idx(this.tdur_ + " ");
    if (!this.timer_used_) this.asm_.append(":");
    this.asm_.append("= time.Since(").append(this.tstart_).append(")\n");
    append_idx("fmt.Println(\"Time: \", " + this.tdur_ + ")\n");
    this.timer_used_ = true;
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
    String res_ = "tmp_" + (++tmp_cnt_);
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
      declare_tmp_if_not_declared();
      encrypt("tmp_", new String[]{lhs.getName()});
      this.asm_.append("\n");
      if (this.is_binary_) {
        switch (op) {
          case "+":
            append_idx(res_ + " := funits.BinAdd(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "*":
            append_idx(res_ + " := funits.BinMult(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "-":
            append_idx(res_ + " := funits.BinSub(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "^":
            append_idx(res_ + " := funits.BinXor(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "==":
            append_idx(res_ + " := funits.BinEq(tmp_, ");
            this.asm_.append(rhs.getName()).append(", ");
            this.asm_.append(this.word_sz_).append(")\n");
            break;
          case "!=":
            append_idx(res_ + " := funits.BinNeq(tmp_, ");
            this.asm_.append(rhs.getName()).append(", ");
            this.asm_.append(this.word_sz_).append(")\n");
            break;
          case "<":
            append_idx(res_ + " := funits.BinLt(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "<=":
            append_idx(res_ + " := funits.BinLeq(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        switch (op) {
          case "+":
            append_idx(res_ + " := evaluator.AddNew(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "*":
            declare_tmp_if_not_declared();
            append_idx("tmp_ = evaluator.MulNew(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            append_idx(res_ + " := evaluator.RelinearizeNew(tmp_)\n");
            break;
          case "-":
            append_idx(res_ + " := evaluator.SubNew(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "==":
            append_idx(res_ + " := funits.Eq(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "!=":
            append_idx(res_ + " := funits.Neq(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "<":
            append_idx(res_ + " := funits.Lt(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "<=":
            append_idx(res_ + " := funits.Leq(tmp_, ");
            this.asm_.append(rhs.getName()).append(")\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
            throw new Exception("Shift over encrypted integers is not possible");
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      declare_tmp_if_not_declared();
      encrypt("tmp_", new String[]{rhs.getName()});
      this.asm_.append("\n");
      if (this.is_binary_) {
        switch (op) {
          case "+":
            append_idx(res_ + " := funits.BinAdd(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "*":
            append_idx(res_ + " := funits.BinMult(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "-":
            append_idx(res_ + " := funits.BinSub(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "^":
            append_idx(res_ + " := funits.BinXor(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "==":
            append_idx(res_ + " := funits.BinEq(");
            this.asm_.append(lhs.getName()).append(", tmp_, ");
            this.asm_.append(this.word_sz_).append(")\n");
            break;
          case "!=":
            append_idx(res_ + " := funits.BinNeq(");
            this.asm_.append(lhs.getName()).append(", tmp_, ");
            this.asm_.append(this.word_sz_).append(")\n");
            break;
          case "<":
            append_idx(res_ + " := funits.BinLt(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "<=":
            append_idx(res_ + " := funits.BinLeq(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "<<":
            append_idx(res_ + " := funits.BinShiftLeft(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")\n");
            break;
          case ">>":
            append_idx(res_ + " := funits.BinShiftRight(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")\n");
            break;
          case ">>>":
            append_idx(res_ + " := funits.BinShiftRightLogical(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")\n");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        switch (op) {
          case "+":
            append_idx(res_ + " := evaluator.AddNew(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "*":
            declare_tmp_if_not_declared();
            append_idx("tmp_ = evaluator.MulNew(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            append_idx(res_ + " := evaluator.RelinearizeNew(tmp_)\n");
            break;
          case "-":
            append_idx(res_ + " := evaluator.SubNew(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "==":
            append_idx(res_ + " := funits.Eq(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "!=":
            append_idx(res_ + " := funits.Neq(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "<":
            append_idx(res_ + " := funits.Lt(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "<=":
            append_idx(res_ + " := funits.Leq(");
            this.asm_.append(lhs.getName()).append(", tmp_)\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
            throw new Exception("Shift over encrypted integers is not possible");
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      if (this.is_binary_) {
        switch (op) {
          case "+":
            append_idx(res_ + " := funits.BinAdd(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")\n");
            break;
          case "*":
            append_idx(res_ + " := funits.BinMult(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")\n");
            break;
          case "-":
            append_idx(res_ + " := funits.BinSub(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")\n");
            break;
          case "^":
            append_idx(res_ + " := funits.BinXor(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")\n");
            break;
          case "==":
            append_idx(res_ + " := funits.BinEq(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(", ");
            this.asm_.append(this.word_sz_).append(")\n");
            break;
          case "!=":
            append_idx(res_ + " := funits.BinNeq(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(", ");
            this.asm_.append(this.word_sz_).append(")\n");
            break;
          case "<":
            append_idx(res_ + " := funits.BinLt(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")\n");
            break;
          case "<=":
            append_idx(res_ + " := funits.BinLeq(" + lhs.getName());
            this.asm_.append(", ").append(rhs.getName()).append(")\n");
            break;
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      } else {
        switch (op) {
          case "+":
            append_idx(res_ + " := evaluator.AddNew(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
            break;
          case "*":
            declare_tmp_if_not_declared();
            append_idx("tmp_ = evaluator.MulNew(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
            append_idx(res_ + " := evaluator.RelinearizeNew(tmp_)\n");
            break;
          case "-":
            append_idx(res_ + " := evaluator.SubNew(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
            break;
          case "^":
            throw new Exception("XOR over encrypted integers is not possible");
          case "==":
            append_idx(res_ + " := funits.Eq(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
            break;
          case "!=":
            append_idx(res_ + " := funits.Neq(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
            break;
          case "<":
            append_idx(res_ + " := funits.Lt(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
            break;
          case "<=":
            append_idx(res_ + " := funits.Leq(");
            this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
            break;
          case "<<":
          case ">>":
          case ">>>":
            throw new Exception("Shift over encrypted integers is not possible");
          default:
            throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
        }
      }
    } else {
      throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
    }
    return new Var_t("EncInt", res_);
  }

  /**
   * f0 -> "~"
   * f1 -> PrimaryExpression()
   */
  public Var_t visit(BinNotExpression n) throws Exception {
    Var_t exp = n.f1.accept(this);
    String exp_type = st_.findType(exp);
    if (exp_type.equals("int")) {
        return new Var_t("int", "^" + exp.getName());
    } else if (exp_type.equals("EncInt")) {
      String res_ = "tmp_" + (++tmp_cnt_);
      if (this.is_binary_) {
        append_idx(res_ + " := funits.BinNot(" + exp.getName() + ")\n");
      } else {
        append_idx(res_ + " := evaluator.NegNew(" + exp.getName() + ")\n");
        declare_tmp_if_not_declared();
        encrypt("tmp_", new String[]{"1"});
        this.asm_.append("\n");
        append_idx(res_ + " = evaluator.SubNew(" + res_ + ", tmp_)\n");
      }
      return new Var_t("EncInt", res_);
    }
    throw new Exception("Wrong type for ~: " + exp_type);
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  public Var_t visit(ArrayLookup n) throws Exception {
    Var_t arr = n.f0.accept(this);
    Var_t idx = n.f2.accept(this);
    String arr_type = st_.findType(arr);
    tmp_cnt_++;
    Var_t ret = new Var_t("", "ret_" + tmp_cnt_);
    switch (arr_type) {
      case "int[]":
        ret.setType("int");
        break;
      case "double[]":
        ret.setType("double");
        break;
      case "EncInt[]":
        ret.setType("EncInt");
        break;
      case "EncDouble[]":
        ret.setType("EncDouble");
        break;
    }
    append_idx(ret.getName());
    this.asm_.append(" := ").append(arr.getName()).append("[");
    this.asm_.append(idx.getName()).append("]\n");
    return ret;
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
    String res_ = "tmp_" + (++tmp_cnt_);
    if (cond_t.equals("bool") || cond_t.equals("int") || cond_t.equals("double")) {
      if (e1_t.equals(e2_t)) {
        append_idx("var " + res_ + " " + this.st_.backend_types.get(e1_t) + "\n");
        append_idx("if " + cond.getName() + " {\n");
        append_idx("  " + res_ + " = " + e1.getName() + "\n");
        append_idx("} else {\n");
        append_idx("  " + res_ + " = " + e2.getName() + "\n");
        append_idx("}\n");
        return new Var_t(e1_t, res_);
      } else if ((e1_t.equals("EncInt") || e1_t.equals("EncDouble")) &&
                  (e2_t.equals("int") || e2_t.equals("double")) ) {
        append_idx("var " + res_ + " " + this.st_.backend_types.get(e1_t) + "\n");
        String e2_enc = "tmp_" + (++tmp_cnt_);
        if (this.is_binary_) {
          append_idx(e2_enc + " := make(" + this.st_.backend_types.get(e1_t));
          this.asm_.append(", ").append(this.word_sz_).append(")").append("\n");
        } else {
          append_idx("var " + e2_enc + " " + this.st_.backend_types.get(e1_t) + "\n");
        }
        encrypt(e2_enc, new String[]{e2.getName()});
        this.asm_.append("\n");
        append_idx("if " + cond.getName() + " {\n");
        append_idx("  " + res_ + " = " + e1.getName() + "\n");
        append_idx("} else {\n");
        append_idx("  " + res_ + " = " + e2_enc + "\n");
        append_idx("}\n");
        return new Var_t(e1_t, res_);
      } else if ((e2_t.equals("EncInt") || e2_t.equals("EncDouble")) &&
                  (e1_t.equals("int") || e1_t.equals("double")) ) {
        append_idx("var " + res_ + " " + this.st_.backend_types.get(e2_t) + "\n");
        String e1_enc = "tmp_" + (++tmp_cnt_);
        if (this.is_binary_) {
          append_idx(e1_enc + " := make(" + this.st_.backend_types.get(e2_t));
          this.asm_.append(", ").append(this.word_sz_).append(")").append("\n");
        } else {
          append_idx("var " + e1_enc + " " + this.st_.backend_types.get(e2_t) + "\n");
        }
        encrypt(e1_enc, new String[]{e1.getName()});
        this.asm_.append("\n");
        append_idx("if " + cond.getName() + " {\n");
        append_idx("  " + res_ + " = " + e1_enc + "\n");
        append_idx("} else {\n");
        append_idx("  " + res_ + " = " + e2.getName() + "\n");
        append_idx("}\n");
        return new Var_t(e2_t, res_);
      }
    } else if (cond_t.equals("EncInt") || cond_t.equals("EncDouble")) {
      if (e1_t.equals(e2_t)) {
        if (e1_t.equals("int") || e1_t.equals("double")) {
          String e1_enc = "tmp_" + (++tmp_cnt_), e2_enc = "tmp_" + (++tmp_cnt_);
          if (this.is_binary_) {
            append_idx(e1_enc + " := make(" + this.st_.backend_types.get("EncInt"));
            this.asm_.append(", ").append(this.word_sz_).append(")").append("\n");
            append_idx(e2_enc + " := make(" + this.st_.backend_types.get("EncInt"));
            this.asm_.append(", ").append(this.word_sz_).append(")").append("\n");
          } else {
            append_idx("var " + e1_enc + " " + this.st_.backend_types.get("EncInt") + "\n");
            append_idx("var " + e2_enc + " " + this.st_.backend_types.get("EncInt") + "\n");
          }
          encrypt(e1_enc, new String[]{e1.getName()});
          this.asm_.append(";\n");
          encrypt(e2_enc, new String[]{e2.getName()});
          this.asm_.append(";\n");
          if (this.is_binary_) {
            append_idx(res_ + " := funits.BinMux(" + cond.getName() + ", ");
          } else {
            append_idx(res_ + " := funits.Mux(" + cond.getName() + ", ");
          }
          this.asm_.append(e1_enc).append(", ").append(e2_enc);
          this.asm_.append(")\n");
          return new Var_t("EncInt", res_);
        } else if (e1_t.equals("EncInt") || e1_t.equals("EncDouble")) {
          if (this.is_binary_) {
            append_idx(res_ + " := funits.BinMux(" + cond.getName() + ", ");
          } else {
            append_idx(res_ + " := funits.Mux(" + cond.getName() + ", ");
          }
          this.asm_.append(e1.getName()).append(", ").append(e2.getName());
          this.asm_.append(")\n");
          return new Var_t(e1_t, res_);
        }
      } else if ((e1_t.equals("EncInt") || e1_t.equals("EncDouble")) &&
                  (e2_t.equals("int") || e2_t.equals("double")) ) {
        String e2_enc = "tmp_" + (++tmp_cnt_);
        if (this.is_binary_) {
          append_idx(e2_enc + " := make(" + this.st_.backend_types.get(e1_t));
          this.asm_.append(", ").append(this.word_sz_).append(")").append("\n");
        } else {
          append_idx("var " + e2_enc + " " + this.st_.backend_types.get(e1_t) + "\n");
        }
        encrypt(e2_enc, new String[]{e2.getName()});
        this.asm_.append("\n");
        if (this.is_binary_) {
          append_idx(res_ + " := funits.BinMux(" + cond.getName() + ", ");
        } else {
          append_idx(res_ + " := funits.Mux(" + cond.getName() + ", ");
        }
        this.asm_.append(e1.getName()).append(", ").append(e2_enc).append(")\n");
        return new Var_t(e1_t, res_);
      } else if ((e2_t.equals("EncInt") || e2_t.equals("EncDouble")) &&
                  (e1_t.equals("int") || e1_t.equals("double")) ) {
        String e1_enc = "tmp_" + (++tmp_cnt_);
        if (this.is_binary_) {
          append_idx(e1_enc + " := make(" + this.st_.backend_types.get(e2_t));
          this.asm_.append(", ").append(this.word_sz_).append(")").append("\n");
        } else {
          append_idx("var " + e1_enc + " " + this.st_.backend_types.get(e2_t) + "\n");
        }
        encrypt(e1_enc, new String[]{e1.getName()});
        this.asm_.append("\n");
        if (this.is_binary_) {
          append_idx(res_ + " := funits.BinMux(" + cond.getName() + ", ");
        } else {
          append_idx(res_ + " := funits.Mux(" + cond.getName() + ", ");
        }
        this.asm_.append(e1_enc).append(", ").append(e2.getName()).append(")\n");
        return new Var_t(e2_t, res_);
      }
    }
    throw new RuntimeException("Ternary condition error: " +
            cond.getName() + " type: " + cond_t +
            e1.getName() + " type: " + e1_t +
            e2.getName() + " type: " + e2_t);
  }

}
