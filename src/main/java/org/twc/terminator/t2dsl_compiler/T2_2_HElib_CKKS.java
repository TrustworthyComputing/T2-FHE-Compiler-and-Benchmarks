package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_HElib_CKKS extends T2_2_HElib {

  public T2_2_HElib_CKKS(SymbolTable st, String config_file_path) {
    super(st, config_file_path, 0);
    this.st_.backend_types.put("EncDouble", "Ctxt");
    this.st_.backend_types.put("EncDouble[]", "vector<Ctxt>");
  }

  protected void append_keygen() {
    append_idx("Context context = helib::ContextBuilder<helib::CKKS>()\n");
    append_idx("  .m(128).precision(20).bits(30).c(3).build();\n");
    append_idx("SecKey secret_key(context);\n");
    append_idx("secret_key.GenSecKey();\n");
    append_idx("addSome1DMatrices(secret_key);\n");
    append_idx("const PubKey& public_key = secret_key;\n");
    append_idx("long slots = context.getNSlots();\n");
    append_idx("vector<std::complex<double>> tmp(slots);\n");
    append_idx("PtxtArray ptxt(context);\n");
    append_idx("Ctxt tmp_(public_key);\n\n");
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
      // if EncDouble <- int
      assign_to_all_slots("tmp", rhs_name, null);
      append_idx("ptxt = tmp; // encode\n");
      append_idx("ptxt.encrypt(");
      this.asm_.append(lhs.getName()).append(")");
      this.semicolon_ = true;
    } else if (lhs_type.equals("EncDouble[]") &&
                (rhs_type.equals("double[]") || rhs_type.equals("int[]"))) {
      // if EncDouble[] <- int[]
      append_idx(lhs.getName());
      this.asm_.append(".resize(").append(rhs_name).append(".size(), tmp_);\n");
      append_idx("for (size_t ");
      this.asm_.append(this.tmp_i).append(" = 0; ").append(this.tmp_i).append(" < ");
      this.asm_.append(rhs_name).append(".size(); ++").append(this.tmp_i);
      this.asm_.append(") {\n");
      this.indent_ += 2;
      assign_to_all_slots("tmp", rhs_name, this.tmp_i);
      append_idx("ptxt = tmp; // encode\n");
      append_idx("ptxt.encrypt(");
      this.asm_.append(lhs.getName()).append("[").append(this.tmp_i);
      this.asm_.append("]);\n");
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
    append_idx(id.getName() + " += 1.0");
    this.semicolon_ = true;
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "--"
   */
  public Var_t visit(DecrementAssignmentStatement n) throws Exception {
    Var_t id = n.f0.accept(this);
    append_idx(id.getName() + " -= 1.0");
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
    if ((lhs_type.equals("int") || lhs_type.equals("double")) &&
          (rhs_type.equals("int") || rhs_type.equals("double"))) {
      append_idx(lhs.getName());
      this.asm_.append(" ").append(op).append(" ");
      this.asm_.append(rhs.getName());
    } else if (lhs_type.equals("EncDouble") &&
                (rhs_type.equals("int") || rhs_type.equals("double") ||
                 rhs_type.equals("EncDouble"))
    ) {
      append_idx(lhs.getName());
      this.asm_.append(" ").append(op).append(" ").append(rhs.getName());
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
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] ");
          if (op.equals("+=") || op.equals("-=") || op.equals("*=")) {
            this.asm_.append(op).append(" ").append(rhs.getName());
          } else {
            throw new Exception("Error in compound array assignment");
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
        this.asm_.append("[").append(idx.getName()).append("] = ");
        this.asm_.append(rhs.getName());
        break;
      case "EncDouble[]":
        if (rhs_type.equals("EncDouble")) {
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] = ");
          this.asm_.append(rhs.getName()).append(";\n");
          break;
        } else if (rhs_type.equals("int") || rhs_type.equals("double")) {
          assign_to_all_slots("tmp", rhs.getName(), null);
          append_idx("ptxt = tmp; // encode\n");
          append_idx("ptxt.encrypt(");
          this.asm_.append(id.getName()).append("[").append(idx.getName()).append("]);\n");
          break;
        }
      default:
        throw new Exception("error in array assignment");
    }
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
        append_idx(id.getName());
        this.asm_.append(" = { ").append(exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" };\n");
        break;
      case "EncDouble":
        append_idx("tmp[0] = ");
        this.asm_.append(exp.getName()).append(";\n");
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            append_idx("tmp[");
            this.asm_.append(i + 1).append("] = ");
            this.asm_.append((n.f4.nodes.get(i).accept(this)).getName()).append(";\n");
          }
        }
        append_idx("ptxt = tmp; // encode\n");
        append_idx("ptxt.encrypt(");
        this.asm_.append(id.getName()).append(");\n");
        break;
      case "EncDouble[]":
        String exp_var;
        if (exp_type.equals("int") || exp_type.equals("double")) {
          exp_var = new_ctxt_tmp();
          assign_to_all_slots("tmp", exp.getName(), null);
          append_idx("ptxt = tmp; // encode\n");
          append_idx("ptxt.encrypt(");
          this.asm_.append(exp_var).append(");\n");
        } else { // exp type is EncDouble
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            String v_type = st_.findType(new Var_t(null, init));
            if (v_type.equals("int") || v_type.equals("double") || isNumeric(init)) {
              String tmp_ = new_ctxt_tmp();
              assign_to_all_slots("tmp", init, null);
              append_idx("ptxt = tmp; // encode\n");
              append_idx("ptxt.encrypt(");
              this.asm_.append(tmp_).append(");\n");
              inits.add(tmp_);
            } else { // exp type is EncDouble
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
//    TODO: merge with bfv
    Var_t id = n.f0.accept(this);
    Var_t index = n.f2.accept(this);
    Var_t exp = n.f6.accept(this);
    String id_type = st_.findType(id);
    if (!id_type.equals("EncDouble[]"))
      throw new RuntimeException("BatchArrayAssignmentStatement");
    String index_type = st_.findType(index);
    tmp_cnt_++;
    append_idx("tmp[0] = ");
    this.asm_.append(exp.getName()).append(";\n");
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); i++) {
        append_idx("tmp[");
        this.asm_.append(i + 1).append("] = ");
        this.asm_.append((n.f7.nodes.get(i).accept(this)).getName()).append(";\n");
      }
    }
    append_idx("ptxt = tmp; // encode\n");
    append_idx("ptxt.encrypt(");
    this.asm_.append(id.getName()).append("[").append(index.getName()).append("]);\n");
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
        append_idx("cout << ");
        this.asm_.append(expr.getName());
        this.asm_.append(" << endl");
        break;
      case "EncDouble":
        append_idx("ptxt.decryptComplex(");
        this.asm_.append(expr.getName()).append(", secret_key);\n");
        append_idx("ptxt.store(tmp);\n");
        append_idx("cout << \"dec(" + expr.getName());
        this.asm_.append(") = \" << fixed << setprecision(1) << real(tmp[0]) << endl");
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
    if (!expr_type.equals("EncDouble"))
      throw new RuntimeException("PrintBatchedStatement: expression type");
    Var_t size = n.f4.accept(this);
    String size_type = size.getType();
    if (size_type == null) size_type = st_.findType(size);
    if (!size_type.equals("int"))
      throw new RuntimeException("PrintBatchedStatement: size type");
    append_idx("ptxt.decryptComplex(");
    this.asm_.append(expr.getName()).append(", secret_key);\n");
    append_idx("ptxt.store(tmp);\n");
    append_idx("cout << \"dec(" + expr.getName() + ") = \";\n");
    append_idx("for (int " + this.tmp_i + " = 0; " + this.tmp_i + " < ");
    this.asm_.append(size.getName()).append("; ++").append(this.tmp_i).append(") {\n");
    append_idx("  cout << fixed << setprecision(1) << real(tmp[" + this.tmp_i + "]) << \" \";\n");
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
                  rhs_type.equals("EncDouble"))  {
      String res_ = new_ctxt_tmp();
      switch (op) {
        case "+":
        case "*":
          append_idx(res_);
          this.asm_.append(" = ").append(rhs.getName()).append(";\n");
          append_idx(res_);
          this.asm_.append(" ").append(op).append("= (double)").append(lhs.getName()).append(";\n");
          break;
        case "-":
          assign_to_all_slots("tmp", lhs.getName(), null);
          append_idx("ptxt = tmp; // encode\n");
          append_idx("ptxt.encrypt(");
          this.asm_.append(res_).append(");\n");
          append_idx(res_);
          this.asm_.append(" -= ").append(rhs.getName()).append(";\n");
          break;
        case "^":
          throw new Exception("XOR over encrypted doubles is not possible");
        case "==":
        case "<":
        case "<=":
          throw new RuntimeException("Comparisons not possible in CKKS");
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      return new Var_t("EncDouble", res_);
    } else if (lhs_type.equals("EncDouble") &&
                (rhs_type.equals("int") || rhs_type.equals("double"))) {
      String res_ = new_ctxt_tmp();
      append_idx(res_);
      this.asm_.append(" = ").append(lhs.getName()).append(";\n");
      append_idx(res_);
      switch (op) {
        case "+":
        case "*":
        case "-":
          this.asm_.append(" ").append(op).append("= (double)").append(rhs.getName()).append(";\n");
          break;
        case "^":
          throw new Exception("XOR over encrypted doubles is not possible");
        case "==":
        case "<":
        case "<=":
          throw new RuntimeException("Comparisons not possible in CKKS");
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      return new Var_t("EncDouble", res_);
    } else if (lhs_type.equals("EncDouble") && rhs_type.equals("EncDouble")) {
      String res_ = new_ctxt_tmp();
      append_idx(res_);
      this.asm_.append(" = ").append(lhs.getName()).append(";\n");
      append_idx(res_);
      switch (op) {
        case "*":
        case "+":
        case "-":
          this.asm_.append(" ").append(op).append("= ");
          this.asm_.append(rhs.getName()).append(";\n");
          break;
        case "^":
          throw new Exception("XOR over encrypted doubles is not possible");
        case "==":
        case "<":
        case "<=":
          throw new RuntimeException("Comparisons not possible in CKKS");
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      return new Var_t("EncDouble", res_);
    }
    throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
  }

  /**
   * f0 -> "new"
   * f1 -> "EncDouble"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public Var_t visit(EncryptedArrayDoubleAllocationExpression n) throws Exception {
    String size = n.f3.accept(this).getName();
    return new Var_t("EncDouble[]", "resize(" + size + ", tmp_)");
  }

}
