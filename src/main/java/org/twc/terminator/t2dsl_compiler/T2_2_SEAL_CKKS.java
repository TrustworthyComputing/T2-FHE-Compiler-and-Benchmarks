package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_SEAL_CKKS extends T2_2_SEAL {

  public T2_2_SEAL_CKKS(SymbolTable st, String config_file_path) {
    super(st, config_file_path, 0);
    this.st_.backend_types.put("EncDouble", "Ciphertext");
    this.st_.backend_types.put("EncDouble[]", "vector<Ciphertext>");
  }

  protected void append_keygen() {
    append_idx("EncryptionParameters parms(scheme_type::ckks);\n");
    append_idx("size_t poly_modulus_degree = 16384;\n");
    append_idx("parms.set_poly_modulus_degree(poly_modulus_degree);\n");
    append_idx("parms.set_coeff_modulus(CoeffModulus::Create(poly_modulus_degree,"
               + " { 60, 40, 40, 40, 40, 40, 60 }));\n");
    append_idx("double scale = pow(2.0, 40);\n");
    append_idx("SEALContext context(parms);\n");
    append_idx("KeyGenerator keygen(context);\n");
    append_idx("SecretKey secret_key = keygen.secret_key();\n");
    append_idx("PublicKey public_key;\n");
    append_idx("RelinKeys relin_keys;\n");
    append_idx("GaloisKeys gal_keys;\n");
    append_idx("keygen.create_public_key(public_key);\n");
    append_idx("keygen.create_relin_keys(relin_keys);\n");
    append_idx("keygen.create_galois_keys(gal_keys);\n");
    append_idx("Encryptor encryptor(context, public_key);\n");
    append_idx("Evaluator evaluator(context);\n");
    append_idx("Decryptor decryptor(context, secret_key);\n");
    append_idx("CKKSEncoder encoder(context);\n");
    append_idx("size_t slot_count = encoder.slot_count();\n");
    append_idx("Plaintext tmp;\n");
    append_idx("Ciphertext tmp_;\n\n");
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
      append_idx("encoder.encode(");
      this.asm_.append(rhs_name);
      this.asm_.append(", scale, tmp);\n");
      append_idx("encryptor.encrypt(tmp, ");
      this.asm_.append(lhs.getName()).append(")");
      this.semicolon_ = true;
    } else if (lhs_type.equals("EncDouble[]") &&
                (rhs_type.equals("double[]") || rhs_type.equals("int[]"))) {
      // if EncDouble[] <- int[] | double[]
      append_idx(lhs.getName());
      this.asm_.append(".resize(").append(rhs_name).append(".size());\n");
      append_idx("for (size_t ");
      this.asm_.append(this.tmp_i).append(" = 0; ").append(this.tmp_i).append(" < ");
      this.asm_.append(rhs_name).append(".size(); ++").append(this.tmp_i);
      this.asm_.append(") {\n");
      this.indent_ += 2;
      append_idx("encoder.encode(");
      this.asm_.append(rhs_name).append("[").append(this.tmp_i).append("], scale, tmp);\n");
      append_idx("encryptor.encrypt(tmp, ");
      this.asm_.append(lhs.getName()).append("[").append(this.tmp_i).append("]);\n");
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
    if (id_type.equals("EncDouble")) {
      append_idx("encoder.encode(1.0, scale, tmp);\n");
      append_idx("evaluator.mod_switch_to_inplace(tmp, " + id.getName() + ".parms_id());\n");
      append_idx("evaluator.add_plain_inplace(");
      this.asm_.append(id.getName()).append(", tmp);\n");
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
    if (id_type.equals("EncDouble")) {
      append_idx("encoder.encode(1.0, scale, tmp);\n");
      append_idx("evaluator.mod_switch_to_inplace(tmp, " + id.getName() + ".parms_id());\n");
      append_idx("evaluator.sub_plain_inplace(");
      this.asm_.append(id.getName()).append(", tmp);\n");
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
    if ((lhs_type.equals("int") || lhs_type.equals("double")) &&
          (rhs_type.equals("int") || rhs_type.equals("double"))) {
      append_idx(lhs.getName());
      this.asm_.append(" ").append(op).append(" ");
      this.asm_.append(rhs.getName());
    } else if (lhs_type.equals("EncDouble") && rhs_type.equals("EncDouble")) {
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
    } else if (lhs_type.equals("EncDouble") &&
                (rhs_type.equals("int") || rhs_type.equals("double"))) {
      append_idx("encoder.encode(");
      this.asm_.append(rhs.getName()).append(", scale, tmp);\n");
      append_idx("evaluator.mod_switch_to_inplace(tmp, " + lhs.getName() + ".parms_id());\n");
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
      case "double[]":
        append_idx(id.getName());
        this.asm_.append("[").append(idx.getName()).append("] ").append(op);
        this.asm_.append(" ").append(rhs.getName());
        break;
      case "EncDouble[]":
        if (rhs_type.equals("EncDouble")) {
          append_idx("evaluator.");
          if (op.equals("+=")) {
            this.asm_.append("add(");
          } else if (op.equals("*=")) {
            this.asm_.append("multiply(");
          } else if (op.equals("-=")) {
            this.asm_.append("sub(");
          } else {
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
          append_idx("encoder.encode(");
          this.asm_.append(rhs.getName()).append(", scale, tmp);\n");
          append_idx("encryptor.encrypt(tmp, ");
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
        String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
        append_idx("vector<double> ");
        this.asm_.append(tmp_vec).append(" = { ").append(exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" };\n");
        append_idx("encoder.encode(");
        this.asm_.append(tmp_vec).append(", scale, tmp);\n");
        append_idx("encryptor.encrypt(tmp, ");
        this.asm_.append(id.getName()).append(");\n");
        break;
      case "EncDouble[]":
        String exp_var;
        if (exp_type.equals("int") || exp_type.equals("double")) {
          exp_var = new_ctxt_tmp();
          append_idx("encoder.encode(" + exp.getName() + ", scale, tmp);\n");
          append_idx("encryptor.encrypt(tmp, ");
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
              append_idx("encoder.encode(" + init + ", scale, tmp);\n");
              append_idx("encryptor.encrypt(tmp, ");
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
    Var_t id = n.f0.accept(this);
    Var_t index = n.f2.accept(this);
    Var_t exp = n.f6.accept(this);
    String id_type = st_.findType(id);
    if (!id_type.equals("EncDouble[]"))
      throw new RuntimeException("BatchArrayAssignmentStatement: id type");
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    append_idx("vector<double> ");
    this.asm_.append(tmp_vec).append(" = { ").append(exp.getName());
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); i++) {
        this.asm_.append(", ").append((n.f7.nodes.get(i).accept(this)).getName());
      }
    }
    this.asm_.append(" };\n");
    append_idx("encoder.encode(");
    this.asm_.append(tmp_vec).append(", scale, tmp);\n");
    append_idx("encryptor.encrypt(tmp, ");
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
        append_idx("decryptor.decrypt(");
        this.asm_.append(expr.getName()).append(", tmp);\n");
        String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
        append_idx("vector<double> " + tmp_vec + ";\n");
        append_idx("encoder.decode(tmp, " + tmp_vec + ");\n");
        append_idx("cout << \"dec(");
        this.asm_.append(expr.getName()).append(") = \" << fixed << setprecision(1) << ");
        this.asm_.append(tmp_vec).append("[0]").append(" << endl");
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
    if (size_type == null) size_type = st_.findType(expr);
    if (!size_type.equals("int"))
      throw new RuntimeException("PrintBatchedStatement: size type");
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    append_idx("decryptor.decrypt(");
    this.asm_.append(expr.getName()).append(", tmp);\n");
    append_idx("vector<double> " + tmp_vec + ";\n");
    append_idx("encoder.decode(tmp, " + tmp_vec + ");\n");
    append_idx("cout << \"dec(" + expr.getName() + ") = \";\n");
    append_idx("for (int i = 0; i < ");
    this.asm_.append(size.getName()).append("; ++i) {\n");
    append_idx("  cout << fixed << setprecision(1) << " + tmp_vec + "[i] << \" \";\n");
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
    Var_t expr = n.f2.accept(this);
    String expr_type = st_.findType(expr);
    if (!expr_type.equals("EncDouble"))
      throw new RuntimeException("ReduceNoiseStatement");
    append_idx("evaluator.rescale_to_next_inplace(");
    this.asm_.append(expr.getName()).append(");\n");
    return null;
  }

  /**
   * f0 -> <MATCH_PARAMS>
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ","
   * f4 -> Expression()
   * f5 -> ")"
   */
  public Var_t visit(MatchParamsStatement n) throws Exception { //is int
    n.f0.accept(this);
    n.f1.accept(this);
    Var_t dst = n.f2.accept(this);
    n.f3.accept(this);
    Var_t src = n.f4.accept(this);
    append_idx("evaluator.mod_switch_to_inplace(" + dst.getName());
    this.asm_.append(", ").append(src.getName()).append(".parms_id());\n");
    append_idx(dst.getName() + ".scale() = scale;\n");
    append_idx(src.getName() + ".scale() = scale;\n");
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
                  rhs_type.equals("EncDouble")) {
      String res_ = new_ctxt_tmp();
      append_idx("encoder.encode(" + lhs.getName() + ", scale, tmp);\n");
      append_idx("evaluator.mod_switch_to_inplace(tmp, " + rhs.getName() + ".parms_id());\n");
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
      append_idx("encoder.encode(" + rhs.getName() + ", scale, tmp);\n");
      append_idx("evaluator.mod_switch_to_inplace(tmp, " + lhs.getName() + ".parms_id());\n");
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

}
