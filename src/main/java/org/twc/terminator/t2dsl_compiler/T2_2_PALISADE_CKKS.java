package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_PALISADE_CKKS extends T2_2_PALISADE {

  public T2_2_PALISADE_CKKS(SymbolTable st, String config_file_path) {
    super(st, config_file_path);
    this.st_.backend_types.put("EncDouble", "Ciphertext<DCRTPoly>");
    this.st_.backend_types.put("EncDouble[]", "vector<Ciphertext<DCRTPoly>>");
  }

  protected void append_keygen() {
    append_idx("uint32_t multDepth = 5;\n");
    append_idx("uint32_t scaleFactorBits = 50;\n");
    append_idx("uint32_t slots = 8;\n");
    append_idx("SecurityLevel securityLevel = HEStd_128_classic;\n");
    append_idx("uint32_t ringDimension = 0;\n");

    append_idx("CryptoContext<DCRTPoly> cc = CryptoContextFactory<\n");
    append_idx("  DCRTPoly>::genCryptoContextCKKS(multDepth,\n"); // BFV
    append_idx("    scaleFactorBits, slots, securityLevel, ringDimension, EXACTRESCALE);\n");
    append_idx("cc->Enable(ENCRYPTION);\n");
    append_idx("cc->Enable(SHE);\n");
    append_idx("cc->Enable(LEVELEDSHE);\n");

    append_idx("auto keyPair = cc->KeyGen();\n");
    append_idx("cc->EvalMultKeyGen(keyPair.secretKey);\n");
    append_idx("Plaintext tmp;\n");
    append_idx("Ciphertext<DCRTPoly> tmp_;\n\n");
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
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx("vector<double> " + tmp_vec + "(slots, " + rhs_name + ");\n");
      append_idx("tmp = cc->MakeCKKSPackedPlaintext(");
      this.asm_.append(tmp_vec).append(");\n");
      append_idx(lhs.getName());
      this.asm_.append(" = cc->Encrypt(keyPair.publicKey, tmp);\n");
      this.semicolon_ = true;
    } else if (lhs_type.equals("EncDouble[]") &&
                (rhs_type.equals("double[]") || rhs_type.equals("int[]"))) {
      // if EncDouble[] <- int[] | double[]
      tmp_cnt_++;
      String tmp_i = "i_" + tmp_cnt_;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx(lhs.getName());
      this.asm_.append(".resize(").append(rhs_name).append(".size());\n");
      append_idx("for (size_t ");
      this.asm_.append(tmp_i).append(" = 0; ").append(tmp_i).append(" < ");
      this.asm_.append(rhs_name).append(".size(); ++").append(tmp_i);
      this.asm_.append(") {\n");
      this.indent_ += 2;
      append_idx("vector<double> " + tmp_vec + "(slots, " + rhs_name + "[" + tmp_i + "]);\n");
      append_idx("tmp = cc->MakeCKKSPackedPlaintext(");
      this.asm_.append(tmp_vec).append(");\n");
      append_idx(lhs.getName());
      this.asm_.append("[").append(tmp_i).append("] = cc->Encrypt(keyPair");
      this.asm_.append(".publicKey, tmp);\n");
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
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx("vector<double> " + tmp_vec + "(slots, 1);\n");
      append_idx("tmp = cc->MakeCKKSPackedPlaintext(" + tmp_vec + ");\n");
      append_idx(id.getName());
      this.asm_.append(" = cc->EvalAdd(tmp, ").append(id.getName()).append(");\n");
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
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx("vector<double> " + tmp_vec + "(slots, 1);\n");
      append_idx("tmp = cc->MakeCKKSPackedPlaintext(" + tmp_vec + ");\n");
      append_idx(id.getName());
      this.asm_.append(" = cc->EvalSub(").append(id.getName()).append(", tmp);\n");
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
      append_idx(lhs.getName());
      switch (op) {
        case "+=": this.asm_.append(" = cc->EvalAdd("); break;
        case "*=": this.asm_.append(" = cc->EvalMultAndRelinearize("); break;
        case "-=": this.asm_.append(" = cc->EvalSub("); break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")");
    } else if (lhs_type.equals("EncDouble") &&
                (rhs_type.equals("int") || rhs_type.equals("double"))) {
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx("vector<double> " + tmp_vec + "(slots, " + rhs.getName() + ");\n");
      append_idx("tmp = cc->MakeCKKSPackedPlaintext(");
      this.asm_.append(tmp_vec).append(");\n");
      append_idx(lhs.getName());
      switch (op) {
        case "+=": this.asm_.append(" = cc->EvalAdd("); break;
        case "*=": this.asm_.append(" = cc->EvalMult("); break;
//        auto c2_depth1 = cc->Rescale(c2_depth2);
        case "-=": this.asm_.append(" = cc->EvalSub("); break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      this.asm_.append(lhs.getName()).append(", tmp)");
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
          this.asm_.append("[").append(idx.getName()).append("]");
          if (op.equals("+=")) {
            this.asm_.append(" = cc->EvalAdd(");
          } else if (op.equals("*=")) {
            this.asm_.append(" = cc->EvalMultAndRelinearize(");
//            auto c2_depth1 = cc->Rescale(c2_depth2);
          } else if (op.equals("-=")) {
            this.asm_.append(" = cc->EvalSub(");
          } else {
            throw new Exception("Error in compound array assignment");
          }
          this.asm_.append(id.getName()).append("[").append(idx.getName());
          this.asm_.append("], ").append(rhs.getName()).append(")");
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
          tmp_cnt_++;
          String tmp_vec = "tmp_vec_" + tmp_cnt_;
          append_idx("vector<double> " + tmp_vec + "(slots, " + rhs.getName() + ");\n");
          append_idx("tmp = cc->MakeCKKSPackedPlaintext(");
          this.asm_.append(tmp_vec).append(");\n");
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] = cc->Encrypt(");
          this.asm_.append("keyPair.publicKey, tmp);\n");
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
    String tmp_vec = null;
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
        append_idx("vector<double> ");
        tmp_vec = "tmp_vec_" + (++tmp_cnt_);
        this.asm_.append(tmp_vec).append(" = { ").append(exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" };\n");
        append_idx("tmp = cc->MakeCKKSPackedPlaintext(");
        this.asm_.append(tmp_vec).append(");\n");
        append_idx(id.getName());
        this.asm_.append(" = cc->Encrypt(keyPair.publicKey, tmp);\n");
        break;
      case "EncDouble[]":
        tmp_vec = "tmp_vec_" + (++tmp_cnt_);
        String exp_var;
        if (exp_type.equals("int") || exp_type.equals("double")) {
          exp_var = new_ctxt_tmp();
          append_idx("vector<double> " + tmp_vec + " = { " + exp.getName() + " };\n");
          append_idx("tmp = cc->MakeCKKSPackedPlaintext(" + tmp_vec + ");\n");
          append_idx(exp_var);
          this.asm_.append(" = cc->Encrypt(keyPair.publicKey, tmp);\n");
        } else { // exp type is EncDouble
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            if (exp_type.equals("int") || exp_type.equals("double")) {
              String tmp_ = new_ctxt_tmp();
              tmp_vec = "tmp_vec_" + (++tmp_cnt_);
              append_idx("vector<double> ");
              this.asm_.append(tmp_vec).append(" = { ").append(init).append(" };\n");
              append_idx("tmp = cc->MakeCKKSPackedPlaintext(" + tmp_vec + ");\n");
              append_idx(tmp_);
              append_idx(" = cc->Encrypt(keyPair.publicKey, tmp);\n");
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
    assert(id_type.equals("EncDouble[]"));
    String index_type = st_.findType(index);
    tmp_cnt_++;
    String tmp_vec = "tmp_vec_" + tmp_cnt_;
    append_idx("vector<double> ");
    this.asm_.append(tmp_vec).append(" = { ").append(exp.getName());
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); i++) {
        this.asm_.append(", ").append((n.f7.nodes.get(i).accept(this)).getName());
      }
    }
    this.asm_.append(" };\n");
    append_idx("tmp = cc->MakeCKKSPackedPlaintext(");
    this.asm_.append(tmp_vec).append(");\n");
    append_idx(id.getName());
    this.asm_.append("[").append(index.getName()).append("] = ");
    this.asm_.append("cc->Encrypt(keyPair.publicKey, tmp);\n");
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
        append_idx("cc->Decrypt(keyPair.secretKey,");
        this.asm_.append(expr.getName()).append(", &tmp);\n");
        append_idx("tmp->SetLength(1);\n");
        append_idx("cout << \"dec(");
        this.asm_.append(expr.getName()).append(") = \" << tmp << endl");
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
    assert(expr_type.equals("EncDouble"));
    Var_t size = n.f4.accept(this);
    String size_type = st_.findType(expr);
    assert(size_type.equals("int"));
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    append_idx("cc->Decrypt(keyPair.secretKey, ");
    this.asm_.append(expr.getName()).append(", ").append("&tmp);\n");
    append_idx("tmp->SetLength(");
    this.asm_.append(size.getName()).append(");\n");
    append_idx("cout << tmp << \"\\t\" << endl");
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
    assert(expr_type.equals("EncDouble"));
    append_idx("cc->Rescale(");
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
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx("vector<double> " + tmp_vec + "(slots, " + lhs.getName() + ");\n");
      append_idx("tmp = cc->MakeCKKSPackedPlaintext(" + tmp_vec + ");\n");
      append_idx(res_);
      this.asm_.append(" = cc->");
      switch (op) {
        case "+":
          this.asm_.append("EvalAdd(").append(rhs.getName()).append(", tmp);\n");
          break;
        case "*":
          this.asm_.append("EvalMult(").append(rhs.getName()).append(", tmp);\n");
          break;
        case "-":
          this.asm_.append("EvalSub(tmp, ").append(rhs.getName()).append(");\n");
          break;
        case "==":
          // TODO:
          throw new RuntimeException("equality not yet supported");
//          this.asm_.append(" = eq_plain(evaluator, ").append(rhs.getName());
//          this.asm_.append(", tmp, plaintext_modulus);\n");
//          break;
        case "<":
          throw new RuntimeException("less than not yet supported");
//          append_idx("encryptor.encrypt(tmp, tmp_);\n");
//          append_idx(res_);
//          this.asm_.append(" = lt(evaluator, tmp_, ");
//          this.asm_.append(rhs.getName()).append(", plaintext_modulus);\n");
//          break;
        case "<=":
          throw new RuntimeException("less or equal than not yet supported");
//          append_idx("encryptor.encrypt(tmp, tmp_);\n");
//          append_idx(res_);
//          this.asm_.append(" = leq(evaluator, tmp_, ");
//          this.asm_.append(rhs.getName()).append(", plaintext_modulus);\n");
//          break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      return new Var_t("EncDouble", res_);
    } else if (lhs_type.equals("EncDouble") &&
                (rhs_type.equals("int") || rhs_type.equals("double"))) {
      String res_ = new_ctxt_tmp();
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      append_idx("vector<double> " + tmp_vec + "(slots, " + rhs.getName() + ");\n");
      append_idx("tmp = cc->MakeCKKSPackedPlaintext(" + tmp_vec+ ");\n");
      append_idx(res_);
      this.asm_.append(" = cc->");
      switch (op) {
        case "+":
          this.asm_.append("EvalAdd(").append(lhs.getName()).append(", tmp);\n");
          break;
        case "*":
          this.asm_.append("EvalMult(").append(lhs.getName()).append(", tmp);\n");
//          auto c2_depth1 = cc->Rescale(c2_depth2);
          break;
        case "-":
          this.asm_.append("EvalSub(").append(lhs.getName()).append(", tmp);\n");
          break;
        case "==":
          throw new RuntimeException("equality not yet supported");
//          append_idx(res_);
//          this.asm_.append(" = eq_plain(evaluator, ").append(lhs.getName());
//          this.asm_.append(", tmp, plaintext_modulus);\n");
//          break;
        case "<":
          throw new RuntimeException("less than not yet supported");
//          append_idx(res_);
//          this.asm_.append(" = lt_plain(evaluator, ").append(lhs.getName());
//          this.asm_.append(", tmp, plaintext_modulus);\n");
//          break;
        case "<=":
          throw new RuntimeException("less or equal than not yet supported");
//          append_idx(res_);
//          this.asm_.append(" = leq_plain(evaluator, ").append(lhs.getName());
//          this.asm_.append(", tmp, plaintext_modulus);\n");
//          break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      return new Var_t("EncDouble", res_);
    } else if (lhs_type.equals("EncDouble") && rhs_type.equals("EncDouble")) {
      String res_ = new_ctxt_tmp();
      append_idx(res_);
      this.asm_.append(" = cc->");
      switch (op) {
        case "+":
          this.asm_.append("EvalAdd(").append(lhs.getName()).append(", ");
          this.asm_.append(rhs.getName()).append(");\n");
          break;
        case "*":
          this.asm_.append("EvalMultAndRelinearize(").append(lhs.getName());
//          auto c2_depth1 = cc->Rescale(c2_depth2);
          this.asm_.append(", ").append(rhs.getName()).append(");\n");
          break;
        case "-":
          this.asm_.append("EvalSub(").append(lhs.getName()).append(", ");
          this.asm_.append(rhs.getName()).append(");\n");
          break;
        case "==":
          throw new RuntimeException("equality not yet supported");
//          append_idx(res_);
//          this.asm_.append(" = eq(evaluator, ").append(lhs.getName());
//          this.asm_.append(", ").append(rhs.getName());
//          this.asm_.append(", plaintext_modulus);\n");
//          break;
        case "<":
          throw new RuntimeException("less than not yet supported");
//          append_idx(res_);
//          this.asm_.append(" = lt(evaluator, ").append(lhs.getName());
//          this.asm_.append(", ").append(rhs.getName());
//          this.asm_.append(", plaintext_modulus);\n");
//          break;
        case "<=":
          throw new RuntimeException("less or equal than not yet supported");
//          append_idx(res_);
//          this.asm_.append(" = leq(evaluator, ").append(lhs.getName());
//          this.asm_.append(", ").append(rhs.getName());
//          this.asm_.append(", plaintext_modulus);\n");
//          break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      return new Var_t("EncDouble", res_);
    }
    throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
  }

}
