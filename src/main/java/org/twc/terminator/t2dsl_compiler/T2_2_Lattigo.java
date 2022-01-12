package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_Lattigo extends T2_Compiler {

  public T2_2_Lattigo(SymbolTable st, String config_file_path,
                      boolean is_binary) {
    super(st, config_file_path, is_binary);
    this.st_.backend_types.put("scheme", "bfv");
    this.st_.backend_types.put("int", "uint64");
    this.st_.backend_types.put("int[]", "[]uint64");
    this.st_.backend_types.put("EncInt", "*bfv.Ciphertext");
    this.st_.backend_types.put("EncInt[]", "[]*bfv.Ciphertext");
  }

  protected void assign_to_all_slots(String lhs, String rhs,
                                     String rhs_idx, String type) {
    append_idx("for ");
    this.asm_.append(this.tmp_i).append(" := 0; ").append(this.tmp_i);
    this.asm_.append(" < slots; ").append(this.tmp_i).append("++ {\n");
    this.indent_ += 2;
    append_idx(lhs);
    this.asm_.append("[").append(this.tmp_i);
    if (type.equals("uint64")) {
      this.asm_.append("] = uint64(");
      if (rhs_idx != null) {
        this.asm_.append(rhs).append("[").append(rhs_idx).append("])\n");
      } else {
        this.asm_.append(rhs).append(")\n");
      }
    } else if (type.equals("float64")) {
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
                   "&params, 0x3ee0001, slots) // TODO: change ptxt_mod\n");
    append_idx("ptxt := bfv.NewPlaintext(params)\n");
    append_idx("tmp := make([]uint64, slots)\n");
    append_idx("encoder.EncodeUint(tmp, ptxt)\n");
    append_idx("tmp_ := encryptorPk.EncryptNew(ptxt)\n\n");
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
    append_idx("var ");
    String type = n.f0.accept(this).getType();
    Var_t id = n.f1.accept(this);
    this.asm_.append(id.getName());
    if (n.f2.present()) {
      for (int i = 0; i < n.f2.size(); i++) {
        n.f2.nodes.get(i).accept(this);
      }
    }
    this.asm_.append(" ").append(this.st_.backend_types.get(type));
    this.asm_.append("\n");
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
      assign_to_all_slots("tmp", rhs_name, null,"uint64");
      append_idx("encoder.EncodeUint(tmp, ptxt)\n");
      append_idx(lhs.getName());
      this.asm_.append(" = encryptorSk.EncryptNew(ptxt)");
      this.semicolon_ = true;
    } else if (lhs_type.equals("EncInt[]") && rhs_type.equals("int[]")) {
      // if EncInt[] <- int[]
      tmp_cnt_++;
      String tmp_i = "i_" + tmp_cnt_;
      append_idx(lhs.getName());
      this.asm_.append(" = make([]*bfv.Ciphertext, len(").append(rhs_name);
      this.asm_.append("))\n");
      append_idx("for ");
      this.asm_.append(tmp_i).append(" := 0; ").append(tmp_i).append(" < len(");
      this.asm_.append(rhs_name).append("); ").append(tmp_i);
      this.asm_.append("++ {\n");
      this.indent_ += 2;
      assign_to_all_slots("tmp", rhs_name, tmp_i, "uint64");
      append_idx("encoder.EncodeUint(tmp, ptxt)\n");
      append_idx(lhs.getName());
      this.asm_.append("[").append(tmp_i).append("] = encryptorSk.EncryptNew(ptxt)\n");
      this.indent_ -= 2;
      append_idx("}\n");
    } else if (lhs_type.equals(rhs_type)) {
      // if the destination has the same type as the source.
      append_idx(lhs.getName());
      if (rhs_name.startsWith("resize(")) {
        this.asm_.append(" = make([]");
        this.asm_.append(rhs_type.substring(0, rhs_type.length()-2)).append(", ");
        this.asm_.append(rhs_name.substring(7));
      } else {
        this.asm_.append(" = ");
        this.asm_.append(rhs_name);
      }
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
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx(tmp_vec + " := make([]uint64, slots)\n");
      assign_to_all_slots(tmp_vec, "1", null, "uint64");
      append_idx("encoder.EncodeUint(" + tmp_vec + ", ptxt)\n");
      append_idx("tmp_ = encryptorPk.EncryptNew(ptxt)\n");
      append_idx(id.getName() + " = evaluator.AddNew(");
      this.asm_.append(id.getName()).append(", tmp_)\n");
    } else {
      append_idx(id.getName() + "++");
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
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx(tmp_vec + " := make([]uint64, slots)\n");
      assign_to_all_slots(tmp_vec, "1", null, "uint64");
      append_idx("encoder.EncodeUint(" + tmp_vec + ", ptxt)\n");
      append_idx("tmp_ = encryptorPk.EncryptNew(ptxt)\n");
      append_idx(id.getName() + " = evaluator.SubNew(");
      this.asm_.append(id.getName()).append(", tmp_)\n");
    } else {
      append_idx(id.getName() + "--");
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
      this.asm_.append(" ").append(op).append(" ").append(rhs.getName());
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      switch (op) {
        case "+=":
          append_idx(lhs.getName() + " = evaluator.");
          this.asm_.append("AddNew(").append(lhs.getName()).append(", ");
          this.asm_.append(rhs.getName()).append(")");
          break;
        case "*=":
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
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      assign_to_all_slots("tmp", rhs.getName(), null, "uint64");
      append_idx("encoder.EncodeUint(tmp, ptxt)\n");
      append_idx("tmp_ = encryptorPk.EncryptNew(ptxt)\n");
      switch (op) {
        case "+=":
          append_idx(lhs.getName() + " = evaluator.");
          this.asm_.append("AddNew(").append(lhs.getName()).append(", tmp_)");
          break;
        case "*=":
          append_idx(lhs.getName() + " = evaluator.");
          this.asm_.append("MulNew(").append(lhs.getName()).append(", tmp_)");
          break;
        case "-=":
          append_idx(lhs.getName() + " = evaluator.");
          this.asm_.append("SubNew(").append(lhs.getName()).append(", tmp_)");
          break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
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
          switch (op) {
            case "+=":
              append_idx(id.getName() + "[" + idx.getName() + "]");
              this.asm_.append(" = evaluator.AddNew(").append(id.getName());
              this.asm_.append("[").append(idx.getName()).append("], ");
              this.asm_.append(rhs.getName()).append(")");
              break;
            case "*=":
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
            default: throw new Exception("Error in compound array assignment");
          }
          break;
        } else if (rhs_type.equals("int")) {
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
    String idx_type = st_.findType(idx);
    Var_t rhs = n.f5.accept(this);
    String rhs_type = st_.findType(rhs);
    switch (id_type) {
      case "int[]":
        append_idx(id.getName());
        this.asm_.append("[").append(idx.getName()).append("] = uint64(");
        this.asm_.append(rhs.getName());
        break;
      case "EncInt[]":
        if (rhs_type.equals("EncInt")) {
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] = ");
          this.asm_.append(rhs.getName());
          break;
        } else if (rhs_type.equals("int")) {
          assign_to_all_slots("tmp", rhs.getName(), null, "uint64");
          append_idx("encoder.EncodeUint(tmp, ptxt)\n");
          append_idx(id.getName() + "[" + idx.getName()+ "] = ");
          this.asm_.append("encryptorSk.EncryptNew(ptxt)");
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
        append_idx(id.getName() + " = []uint64{ " + exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" }\n");
        break;
      case "EncInt":
        tmp_cnt_++;
        String tmp_vec = "tmp_vec_" + tmp_cnt_;
        append_idx(tmp_vec + " := []uint64{ " + exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" }\n");
        append_idx("encoder.EncodeUint(" + tmp_vec + ", ptxt)\n");
        append_idx(id.getName() + " = encryptorSk.EncryptNew(ptxt)\n");
        break;
      case "EncInt[]":
        tmp_cnt_++;
        String exp_var = "tmp_" + tmp_cnt_ + "_";
        if (exp_type.equals("int")) {
          assign_to_all_slots("tmp", exp.getName(), null, "uint64");
          append_idx("encoder.EncodeUint(tmp, ptxt)\n");
          append_idx(exp_var + " := encryptorSk.EncryptNew(ptxt)\n");
        } else { // exp type is EncInt
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            if (exp_type.equals("int")) {
              assign_to_all_slots("tmp", init, null, "uint64");
              append_idx("encoder.EncodeUint(tmp, ptxt)\n");
              tmp_cnt_++;
              String tmp_ = "tmp_" + tmp_cnt_ + "_";
              append_idx(tmp_ + " := encryptorSk.EncryptNew(ptxt)\n");
              inits.add(tmp_);
            } else { // exp type is EncInt
              inits.add(init);
            }
          }
        }
        append_idx(id.getName());
        this.asm_.append(" = []*bfv.Ciphertext{ ").append(exp_var);
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
    assert(id_type.equals("EncInt[]"));
    String index_type = st_.findType(index);
    tmp_cnt_++;
    String tmp_vec = "tmp_vec_" + tmp_cnt_;
    append_idx(tmp_vec + " := []uint64{ " + exp.getName());
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); i++) {
        this.asm_.append(", ").append((n.f7.nodes.get(i).accept(this)).getName());
      }
    }
    this.asm_.append(" }\n");
    append_idx("encoder.EncodeUint(" + tmp_vec + ", ptxt)\n");
    append_idx(id.getName() + "[" + index.getName() + "] = encryptorSk.EncryptNew(ptxt)\n");
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
        append_idx("ptxt = decryptor.DecryptNew(" + expr.getName() + ")\n");
        append_idx("fmt.Println(encoder.DecodeUintNew(ptxt)[0])\n");
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
    assert(expr_type.equals("EncInt"));
    Var_t size = n.f4.accept(this);
    String size_type = st_.findType(expr);
    assert(size_type.equals("int"));
    append_idx("ptxt = decryptor.DecryptNew(" + expr.getName() + ")\n");
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    append_idx(tmp_vec + " := encoder.DecodeUintNew(ptxt))\n");
    append_idx("for " + this.tmp_i + " := 0; " + this.tmp_i + " < " + size.getName());
    this.asm_.append("; ").append(this.tmp_i).append("++ {\n");
    this.indent_ += 2;
    append_idx("fmt.Println(" + tmp_vec + "[" + this.tmp_i + "])\n");
    this.indent_ -= 2;
    append_idx("}\n");
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
      assign_to_all_slots("tmp", lhs.getName(), null, "uint64");
      append_idx("encoder.EncodeUint(tmp, ptxt)\n");
      append_idx("tmp_ = encryptorPk.EncryptNew(ptxt)\n");
      switch (op) {
        case "+":
          append_idx(res_ + " := evaluator.AddNew(tmp_, ");
          this.asm_.append(rhs.getName()).append(")\n");
          break;
        case "*":
          append_idx(res_ + " := evaluator.MulNew(tmp_, ");
          this.asm_.append(rhs.getName()).append(")\n");
          break;
        case "-":
          append_idx(res_ + " := evaluator.SubNew(tmp_, ");
          this.asm_.append(rhs.getName()).append(")\n");
          break;
        case "^":
          append_idx(res_ + " := evaluator.XorNew(tmp_, ");
          this.asm_.append(rhs.getName()).append(")\n");
          break;
        case "==":
          append_idx(res_ + " := funits.Eq(tmp_, ");
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
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      assign_to_all_slots("tmp", rhs.getName(), null, "uint64");
      append_idx("encoder.EncodeUint(tmp, ptxt)\n");
      append_idx("tmp_ = encryptorPk.EncryptNew(ptxt)\n");
      switch (op) {
        case "+":
          append_idx(res_ + " := evaluator.AddNew(");
          this.asm_.append(lhs.getName()).append(", tmp_)\n");
          break;
        case "*":
          append_idx(res_ + " := evaluator.MulNew(");
          this.asm_.append(lhs.getName()).append(", tmp_)\n");
          break;
        case "-":
          append_idx(res_ + " := evaluator.SubNew(");
          this.asm_.append(lhs.getName()).append(", tmp_)\n");
          break;
        case "^":
          append_idx(res_ + " := evaluator.XorNew(");
          this.asm_.append(lhs.getName()).append(", tmp_)\n");
          break;
        case "==":
          append_idx(res_ + " := funits.Eq(");
          this.asm_.append(rhs.getName()).append(", tmp_)\n");
          break;
        case "<":
          append_idx(res_ + " := funits.Lt(");
          this.asm_.append(rhs.getName()).append(", tmp_)\n");
          break;
        case "<=":
          append_idx(res_ + " := funits.Leq(");
          this.asm_.append(rhs.getName()).append(", tmp_)\n");
          break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      switch (op) {
        case "+":
          append_idx(res_ + " := evaluator.AddNew(");
          this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
          break;
        case "*":
          append_idx("tmp_ = evaluator.MulNew(");
          this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
          append_idx(res_ + " := evaluator.RelinearizeNew(tmp_)\n");
          break;
        case "-":
          append_idx(res_ + " := evaluator.SubNew(");
          this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
          break;
        case "^":
          append_idx(res_ + " := evaluator.XorNew(");
          this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")\n");
          break;
        case "==":
          append_idx(res_ + " := funits.Eq(");
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
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
    } else {
      throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
    }
    return new Var_t("EncInt", res_);
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

}
