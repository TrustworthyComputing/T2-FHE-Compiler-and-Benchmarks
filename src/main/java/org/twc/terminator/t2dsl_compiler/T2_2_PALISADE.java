package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_PALISADE extends T2_Compiler {

  public T2_2_PALISADE(SymbolTable st) {
    super(st);
    this.st_.backend_types.put("EncInt", "Ciphertext<DCRTPoly>");
    this.st_.backend_types.put("EncInt[]", "vector<Ciphertext<DCRTPoly>>");
  }

  protected void append_keygen() {
    append_idx("uint32_t depth = 5;\n");
    append_idx("double sigma = 3.2;\n");
    append_idx("SecurityLevel securityLevel = HEStd_128_classic;\n");
    append_idx("size_t plaintext_modulus = 786433;\n");
    append_idx("CryptoContext<DCRTPoly> cc = CryptoContextFactory<\n");
    append_idx("  DCRTPoly>::genCryptoContextBFVrns(plaintext_modulus,\n"); // BFV
    append_idx("    securityLevel, sigma, 0, depth, 0, OPTIMIZED);\n");
    append_idx("cc->Enable(ENCRYPTION);\n");
    append_idx("cc->Enable(SHE);\n");
//    append_idx("cc->Enable(LEVELEDSHE);\n"); // for BGV
    append_idx("auto keyPair = cc->KeyGen();\n");
    append_idx("cc->EvalMultKeyGen(keyPair.secretKey);\n");
    append_idx("Plaintext tmp;\n");
//    append_idx("vector<int64_t> tmp_vec = { 0 };\n");
    append_idx("Ciphertext tmp_;\n\n");
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
    append_idx("#include <iostream>\n\n");
    append_idx("#include \"palisade.h\"\n");
    append_idx("#include \"../../helper.hpp\"\n\n");
    append_idx("using namespace lbcrypto;\n");
    append_idx("using namespace std;\n\n");
    append_idx("int main(void) {\n");
    this.indent_ = 2;
    append_keygen();
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
      append_idx("tmp = cc->MakePackedPlaintext({");
      this.asm_.append(rhs_name).append("});\n");
      append_idx(lhs.getName());
      this.asm_.append(" = cc->Encrypt(keyPair.publicKey, tmp)");
      this.semicolon_ = true;
    } else if (lhs_type.equals("EncInt[]") && rhs_type.equals("int[]")) {
      // if EncInt[] <- int[]
      tmp_cnt_++;
      String tmp_i = "i_" + tmp_cnt_;
      append_idx(lhs.getName());
      this.asm_.append(".resize(").append(rhs_name).append(".size());\n");
      append_idx("for (size_t ");
      this.asm_.append(tmp_i).append(" = 0; ").append(tmp_i).append(" < ");
      this.asm_.append(rhs_name).append(".size(); ++").append(tmp_i);
      this.asm_.append(") {\n");
      this.indent_ += 2;
      append_idx("tmp = cc->MakePackedPlaintext({ ");
      this.asm_.append(rhs_name).append("[").append(tmp_i).append("] });\n");
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
    if (id_type.equals("EncInt")) {
      append_idx("tmp = cc->MakePackedPlaintext({1});\n");
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
    if (id_type.equals("EncInt")) {
      append_idx("tmp = cc->MakePackedPlaintext({1});\n");
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
    if (lhs_type.equals("int") && rhs_type.equals("int")) {
      append_idx(lhs.getName());
      this.asm_.append(" ").append(op).append(" ");
      this.asm_.append(rhs.getName());
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      append_idx(lhs.getName());
      switch (op) {
        case "+=": this.asm_.append(" = cc->EvalAdd("); break;
        case "*=": this.asm_.append(" = cc->EvalMultAndRelinearize("); break;
        case "-=": this.asm_.append(" = cc->EvalSub("); break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      this.asm_.append(lhs.getName()).append(", ").append(rhs.getName()).append(")");
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      append_idx("tmp = cc->MakePackedPlaintext({");
      this.asm_.append(rhs.getName()).append("});\n");
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
        this.asm_.append(rhs.getName()).append(";\n");
        break;
      case "EncInt[]":
        if (rhs_type.equals("EncInt")) {
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] = ");
          this.asm_.append(rhs.getName()).append(";\n");
          break;
        } else if (rhs_type.equals("int")) {
          append_idx("tmp = cc->MakePackedPlaintext({");
          this.asm_.append(rhs.getName());
          this.asm_.append("});\n");
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
        tmp_cnt_++;
        String tmp_vec = "tmp_vec_" + tmp_cnt_;
        append_idx("vector<uint64_t> ");
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
        break;
      case "EncInt[]":
        String exp_var;
        if (exp_type.equals("int")) {
          exp_var = new_ctxt_tmp();
          append_idx("tmp = cc->MakePackedPlaintext({" + exp.getName() + "});\n");
          append_idx(exp_var);
          this.asm_.append(" = cc->Encrypt(keyPair.publicKey, tmp);\n");
        } else { // exp type is EncInt
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            if (exp_type.equals("int")) {
              String tmp_ = new_ctxt_tmp();
              append_idx("tmp = cc->MakePackedPlaintext({" + init + "});\n");
              append_idx(tmp_);
              append_idx(" = cc->Encrypt(keyPair.publicKey, tmp);\n");
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
    String index_type = st_.findType(index);
    tmp_cnt_++;
    String tmp_vec = "tmp_vec_" + tmp_cnt_;
    append_idx("vector<uint64_t> ");
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
    this.asm_.append("[").append(index.getName()).append("]) = ");
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
        append_idx("cout << ");
        this.asm_.append(expr.getName());
        this.asm_.append(" << endl;\n");
        break;
      case "EncInt":
        append_idx("cc->Decrypt(keyPair.secretKey,");
        this.asm_.append(expr.getName()).append(", &tmp);\n");
        append_idx("cout << \"dec(");
        this.asm_.append(expr.getName()).append(") = \" << tmp << endl;\n");
        break;
      default:
        throw new Exception("Bad type for print statement");
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
    Var_t expr = n.f2.accept(this);
    String expr_type = st_.findType(expr);
    assert(expr_type.equals("EncInt"));
    append_idx("cc->ModReduceInPlace(");
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
                 "<=".equals(op) || ">".equals(op) || ">=".equals(op)) {
        return new Var_t("bool", lhs.getName() + op + rhs.getName());
      }
    } else if (lhs_type.equals("int") && rhs_type.equals("EncInt")) {
      String res_ = new_ctxt_tmp();
      append_idx("tmp = cc->MakePackedPlaintext({" + lhs.getName() + "});\n");
      append_idx(res_);
      this.asm_.append(" = cc->");
      switch (op) {
        case "+":
          this.asm_.append("EvalAdd(").append(rhs.getName()).append(", tmp);\n");
          break;
        case "*":
          this.asm_.append("EvalMult(").append(rhs.getName()).append(", tmp)\n");
          break;
        case "-":
          this.asm_.append("EvalSub(tmp, ").append(rhs.getName()).append(")\n");
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
      return new Var_t("EncInt", res_);
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      String res_ = new_ctxt_tmp();
      append_idx("tmp = cc->MakePackedPlaintext({" + rhs.getName() + "});\n");
      append_idx(res_);
      this.asm_.append(" = cc->");
      switch (op) {
        case "+":
          this.asm_.append("EvalAdd(").append(lhs.getName()).append(", tmp);\n");
          break;
        case "*":
          this.asm_.append("EvalMult(").append(lhs.getName()).append(", tmp);\n");
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
      return new Var_t("EncInt", res_);
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
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
      return new Var_t("EncInt", res_);
    }
    throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
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
    Var_t exp_1 = n.f4.accept(this);
    Var_t exp_2 = n.f6.accept(this);
    String cond_type = st_.findType(cond);
    String exp_1_type = st_.findType(exp_1);
    String exp_2_type = st_.findType(exp_2);
    if (!exp_1_type.equals(exp_2_type)) {
      throw new RuntimeException(exp_1_type + " is not equal to" + exp_2_type +
                                 " in ternary expression");
    }
    String res_ = new_ctxt_tmp();
    switch (cond_type) {
      case "bool":
      case "int":
        append_idx(res_);
        this.asm_.append(" = (").append(cond.getName()).append(") ? ");
        this.asm_.append(exp_1.getName()).append(" : ").append(exp_2.getName());
        this.asm_.append(";\n");
        return new Var_t(exp_1_type, res_);
      case "EncInt":
        append_idx(res_);
        this.asm_.append(" = mux(").append(cond.getName()).append(", ");
        this.asm_.append(exp_1.getName()).append(", ").append(exp_2.getName());
        this.asm_.append(");\n");
        return new Var_t("EncInt", res_);
      default:
        throw new RuntimeException("condition in ternary should be either " +
                "bool or EncInt but it is of type " + cond_type);
    }
  }

}