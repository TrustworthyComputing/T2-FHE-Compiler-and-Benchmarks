package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_Lattigo_CKKS extends T2_2_Lattigo {

  public T2_2_Lattigo_CKKS(SymbolTable st, String config_file_path) {
    super(st, config_file_path, false);
    this.st_.backend_types.put("scheme", "ckks");
    this.st_.backend_types.put("double", "float64");
    this.st_.backend_types.put("double[]", "[]float64");
    this.st_.backend_types.put("EncDouble", "*ckks.Ciphertext");
    this.st_.backend_types.put("EncDouble[]", "[]*ckks.Ciphertext");
  }

  protected void append_keygen() {
    append_idx("params, err := ckks.NewParametersFromLiteral(ckks.PN14QP438)\n");
    append_idx("slots := params.LogSlots()\n");
    append_idx("if err != nil {\n");
    append_idx("  panic(err)\n");
    append_idx("}\n");
    append_idx("encoder := ckks.NewEncoder(params)\n");
    append_idx("kgen := ckks.NewKeyGenerator(params)\n");
    append_idx("clientSk, clientPk := kgen.GenKeyPair()\n");
    append_idx("encryptorPk := ckks.NewEncryptor(params, clientPk)\n");
    append_idx("encryptorSk := ckks.NewEncryptor(params, clientSk)\n");
    append_idx("decryptor := ckks.NewDecryptor(params, clientSk)\n");
    append_idx("rlk := kgen.GenRelinearizationKey(clientSk, 1)\n");
    append_idx("evaluator := ckks.NewEvaluator(params, rlwe.EvaluationKey{Rlk: rlk})\n");
    append_idx("funits.FunitsInit(&encryptorPk, &encoder, &evaluator, " +
                   "&params, 0x3ee0001, slots) // TODO: change ptxt_mod\n");
    append_idx("var ptxt *ckks.Plaintext\n");
    append_idx("tmp := make([]complex128, slots)\n");
    append_idx("ptxt = encoder.EncodeNew(tmp, slots)\n");
    append_idx("tmp_ := encryptorPk.EncryptNew(ptxt)\n\n");
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
    if (lhs_type.equals("EncDouble") &&
          (rhs_type.equals("double") || rhs_type.equals("int"))) {
      // if EncDouble <- int | double
      assign_to_all_slots("tmp", rhs_name, null, "float64");
      append_idx("ptxt = encoder.EncodeNew(tmp, slots)\n");
      append_idx(lhs.getName());
      this.asm_.append(" = encryptorSk.EncryptNew(ptxt)");
      this.semicolon_ = true;
    } else if (lhs_type.equals("EncDouble[]") &&
                (rhs_type.equals("double[]") || rhs_type.equals("int[]"))) {
      // if EncDouble[] <- int[] | double[]
      tmp_cnt_++;
      String tmp_i = "i_" + tmp_cnt_;
      append_idx(lhs.getName());
      this.asm_.append(" = make([]*ckks.Ciphertext, len(").append(rhs_name);
      this.asm_.append("))\n");
      append_idx("for ");
      this.asm_.append(tmp_i).append(" := 0; ").append(tmp_i).append(" < len(");
      this.asm_.append(rhs_name).append("); ").append(tmp_i);
      this.asm_.append("++ {\n");
      this.indent_ += 2;
      assign_to_all_slots("tmp", rhs_name, tmp_i, "float64");
      append_idx("ptxt = encoder.EncodeNew(tmp, slots)\n");
      append_idx(lhs.getName());
      this.asm_.append("[").append(tmp_i).append("] = encryptorSk.EncryptNew(ptxt)\n");
      this.indent_ -= 2;
      append_idx("}\n");
    } else if (lhs_type.equals(rhs_type)) {
      // if the destination has the same type as the source.
      append_idx(lhs.getName());
      if (rhs_name.startsWith("resize(")) {
        String t2_type = rhs_type.substring(0, rhs_type.length()-2);
        this.asm_.append(" = make([]");
        this.asm_.append(this.st_.backend_types.get(t2_type)).append(", ");
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
    if (id_type.equals("EncDouble")) {
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx(tmp_vec + " := make([]complex128, slots)\n");
      assign_to_all_slots(tmp_vec, "1", null, "float64");
      append_idx("ptxt = encoder.EncodeNew(" + tmp_vec + ", slots)\n");
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
    if (id_type.equals("EncDouble")) {
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx(tmp_vec + " := make([]complex128, slots)\n");
      assign_to_all_slots(tmp_vec, "1", null, "float64");
      append_idx("ptxt = encoder.EncodeNew(" + tmp_vec + ", slots)\n");
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
    if ((lhs_type.equals("int") || lhs_type.equals("double")) &&
          (rhs_type.equals("int") || rhs_type.equals("double"))) {
      append_idx(lhs.getName());
      this.asm_.append(" ").append(op).append(" ").append(rhs.getName());
    } else if (lhs_type.equals("EncDouble") && rhs_type.equals("EncDouble")) {
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
    } else if (lhs_type.equals("EncDouble") &&
                (rhs_type.equals("int") || rhs_type.equals("double"))) {
      assign_to_all_slots("tmp", rhs.getName(), null, "float64");
      append_idx("ptxt = encoder.EncodeNew(tmp, slots)\n");
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
      case "double[]":
        append_idx(id.getName());
        this.asm_.append("[").append(idx.getName()).append("] ").append(op);
        this.asm_.append(" ").append(rhs.getName());
        break;
      case "EncDouble[]":
        if (rhs_type.equals("EncDouble")) {
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
        } else if (rhs_type.equals("int") || rhs_type.equals("double")) {
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
      case "double[]":
        append_idx(id.getName());
        this.asm_.append("[").append(idx.getName()).append("] = float64(");
        this.asm_.append(rhs.getName());
        break;
      case "EncDouble[]":
        if (rhs_type.equals("EncDouble")) {
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] = ");
          this.asm_.append(rhs.getName());
          break;
        } else if (rhs_type.equals("int") || rhs_type.equals("double")) {
          assign_to_all_slots("tmp", rhs.getName(), null, "float64");
          append_idx("ptxt = encoder.EncodeNew(tmp, slots)\n");
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
      case "double[]":
        append_idx(id.getName() + " = []float64{ " + exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" }\n");
        break;
      case "EncDouble":
        tmp_cnt_++;
        String tmp_vec = "tmp_vec_" + tmp_cnt_;
        append_idx(tmp_vec + " := []complex128{ complex(float64(" + exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append("), 0), complex(float64(").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append("), 0) }\n");
        append_idx("ptxt = encoder.EncodeNew(" + tmp_vec + ", slots)\n");
        append_idx(id.getName() + " = encryptorSk.EncryptNew(ptxt)\n");
        break;
      case "EncDouble[]":
        tmp_cnt_++;
        String exp_var = "tmp_" + tmp_cnt_ + "_";
        if (exp_type.equals("int") || exp_type.equals("double")) {
          assign_to_all_slots("tmp", exp.getName(), null, "float64");
          append_idx("ptxt = encoder.EncodeNew(tmp, slots)\n");
          append_idx(exp_var + " := encryptorSk.EncryptNew(ptxt)\n");
        } else { // exp type is EncDouble
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            if (exp_type.equals("int") || exp_type.equals("double")) {
              assign_to_all_slots("tmp", init, null, "float64");
              append_idx("ptxt = encoder.EncodeNew(tmp, slots)\n");
              tmp_cnt_++;
              String tmp_ = "tmp_" + tmp_cnt_ + "_";
              append_idx(tmp_ + " := encryptorSk.EncryptNew(ptxt)\n");
              inits.add(tmp_);
            } else { // exp type is EncDouble
              inits.add(init);
            }
          }
        }
        append_idx(id.getName());
        this.asm_.append(" = []*ckks.Ciphertext{ ").append(exp_var);
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
    assert(id_type.equals("EncDouble[]"));
    String index_type = st_.findType(index);
    tmp_cnt_++;
    String tmp_vec = "tmp_vec_" + tmp_cnt_;
    append_idx(tmp_vec + " := []complex128{ complex(float64(" + exp.getName());
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); i++) {
        this.asm_.append("), 0), complex(float64(").append((n.f7.nodes.get(i).accept(this)).getName());
      }
    }
    this.asm_.append("), 0) }\n");
    append_idx("ptxt = encoder.EncodeNew(" + tmp_vec + ", slots)\n");
    append_idx(id.getName() + "[" + index.getName() + "] = encryptorSk.EncryptNew(ptxt)\n");
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
      case "double":
        append_idx("fmt.Println(" + expr.getName() + ")\n");
        break;
      case "EncDouble":
        append_idx("ptxt = decryptor.DecryptNew(" + expr.getName() + ")\n");
        append_idx("fmt.Println(encoder.Decode(ptxt, slots)[0])\n");
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
    assert(expr_type.equals("EncDouble"));
    Var_t size = n.f4.accept(this);
    String size_type = st_.findType(expr);
    assert(size_type.equals("int"));
    append_idx("ptxt = decryptor.DecryptNew(" + expr.getName() + ")\n");
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    append_idx(tmp_vec + " := encoder.Decode(ptxt, slots)\n");
    append_idx("for " + this.tmp_i + " := 0; " + this.tmp_i + " < " + size.getName());
    this.asm_.append("; ").append(this.tmp_i).append("++ {\n");
    this.indent_ += 2;
    append_idx("fmt.Print(" + tmp_vec + "[" + this.tmp_i + "], \"\\t\")\n");
    this.indent_ -= 2;
    append_idx("}\n");
    append_idx("fmt.Println()\n");
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
    } else if ((lhs_type.equals("int") || lhs_type.equals("double")) &&
            (rhs_type.equals("int") || rhs_type.equals("double"))) {
      if ("&".equals(op) || "|".equals(op) || "^".equals(op) || "<<".equals(op) ||
              ">>".equals(op) || "+".equals(op) || "-".equals(op) || "*".equals(op) ||
              "/".equals(op) || "%".equals(op)
      ) {
        return new Var_t("double", lhs.getName() + op + rhs.getName());
      } else if ("==".equals(op) || "!=".equals(op) || "<".equals(op) ||
              "<=".equals(op) || ">".equals(op) || ">=".equals(op) ||
              "&&".equals(op) || "||".equals(op)) {
        return new Var_t("bool", lhs.getName() + op + rhs.getName());
      }
    } else if ((lhs_type.equals("int") || lhs_type.equals("double")) &&
                rhs_type.equals("EncDouble")) {
      assign_to_all_slots("tmp", lhs.getName(), null, "float64");
      append_idx("ptxt = encoder.EncodeNew(tmp, slots)\n");
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
    } else if (lhs_type.equals("EncDouble") &&
                (rhs_type.equals("int") || rhs_type.equals("double"))) {
      assign_to_all_slots("tmp", rhs.getName(), null, "float64");
      append_idx("ptxt = encoder.EncodeNew(tmp, slots)\n");
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
    } else if (lhs_type.equals("EncDouble") && rhs_type.equals("EncDouble")) {
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
    return new Var_t("EncDouble", res_);
  }

}
