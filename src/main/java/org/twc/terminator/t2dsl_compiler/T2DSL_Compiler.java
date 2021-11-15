package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;
import org.twc.terminator.t2dsl_compiler.T2DSLvisitor.GJNoArguDepthFirst;

import java.util.ArrayList;
import java.util.List;

public class T2DSL_Compiler extends GJNoArguDepthFirst<Var_t> {

  private SymbolTable st_;

  private StringBuilder asm_;
  private int indent_, tmp_cnt_;
  private boolean semicolon_;

  public T2DSL_Compiler(SymbolTable st) {
    this.indent_ = 0;
    this.tmp_cnt_ = 0;
    this.st_ = st;
    this.semicolon_ = false;
  }

  public String getASM() {
    return asm_.toString();
  }

  private void append2asm(String str) {
    for (int i = 0; i < this.indent_; ++i) {
      this.asm_.append(" ");
    }
    this.asm_.append(str);
  }

  private String newCtxtTemp() {
    tmp_cnt_++;
    String ctxt_tmp_ = "tmp_" + tmp_cnt_ + "_";
    append2asm("Ciphertext " + ctxt_tmp_ + ";\n");
    return ctxt_tmp_;
  }

  private void appendSEALkeygen() {
    this.asm_.append(
      "  size_t poly_modulus_degree = 16384;\n" +
      "  size_t plaintext_modulus = 20;\n" +
      "  EncryptionParameters parms(scheme_type::bfv);\n" +
      "  parms.set_poly_modulus_degree(poly_modulus_degree);\n" +
      "  parms.set_coeff_modulus(CoeffModulus::BFVDefault" +
              "(poly_modulus_degree));\n" +
      "  parms.set_plain_modulus(PlainModulus::Batching(poly_modulus_degree, " +
              "20));\n" +
      "  SEALContext context(parms);\n" +
      "  KeyGenerator keygen(context);\n" +
      "  SecretKey secret_key = keygen.secret_key();\n" +
      "  PublicKey public_key;\n" +
      "  RelinKeys relin_keys;\n" +
      "  keygen.create_public_key(public_key);\n" +
      "  keygen.create_relin_keys(relin_keys);\n" +
      "  Encryptor encryptor(context, public_key);\n" +
      "  Evaluator evaluator(context);\n" +
      "  Decryptor decryptor(context, secret_key);\n" +
      "  BatchEncoder batch_encoder(context);\n" +
      "  Plaintext tmp;\n" +
      "  Ciphertext tmp_;\n\n");
  }

  /**
   * f0 -> MainClass()
   * f2 -> <EOF>
   */
  public Var_t visit(Goal n) throws Exception {
    n.f0.accept(this);
    n.f1.accept(this);
    return null;
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
    this.asm_ = new StringBuilder();
    this.asm_.append("#include <iostream>\n\n");
    this.asm_.append("#include \"seal/seal.h\"\n");
    this.asm_.append("#include \"../../helper.hpp\"\n\n");
    this.asm_.append("using namespace seal;\n");
    this.asm_.append("using namespace std;\n\n");

    append2asm("int main(void) {\n");
    this.indent_ = 2;
    appendSEALkeygen();
    n.f6.accept(this);
    n.f7.accept(this);
    append2asm("return ");
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
    append2asm("");
    String type = n.f0.accept(this).getType();
    switch (type) {
      case "int[]":
        this.asm_.append("vector<int>");
        break;
      case "EncInt":
        this.asm_.append("Ciphertext");
        break;
      case "EncInt[]":
        this.asm_.append("vector<Ciphertext>");
        break;
      default:
        this.asm_.append(type);
    }
    this.asm_.append(" ");
    Var_t id = n.f1.accept(this);
    this.asm_.append(id.getName());
    if (n.f2.present()) {
      for (int i = 0; i < n.f2.size(); i++) {
        n.f2.nodes.get(i).accept(this);
      }
    }
    this.asm_.append(";\n");
    return null;
  }

  /**
   * f0 -> ","
   * f1 -> Identifier()
   */
  public Var_t visit(VarDeclarationRest n) throws Exception {
    this.asm_.append(", ");
    Var_t id = n.f1.accept(this);
    this.asm_.append(id.getName());
    return null;
  }

  /**
   * f0 -> ArrayType()
   * | EncryptedArrayType()
   * | BooleanType()
   * | IntegerType()
   * | EncryptedIntegerType()
   * | Identifier()
   */
  public Var_t visit(Type n) throws Exception {
    return n.f0.accept(this);
  }

  /**
   * f0 -> "int"
   * f1 -> "["
   * f2 -> "]"
   */
  public Var_t visit(ArrayType n) throws Exception {
    return new Var_t("int[]", null);
  }

  /**
   * f0 -> "EncInt"
   * f1 -> "["
   * f2 -> "]"
   */
  public Var_t visit(EncryptedArrayType n) throws Exception {
    return new Var_t("EncInt[]", null);
  }

  /**
   * f0 -> "boolean"
   */
  public Var_t visit(BooleanType n) throws Exception {
    return new Var_t("bool", null);
  }

  /**
   * f0 -> "int"
   */
  public Var_t visit(IntegerType n) throws Exception {
    return new Var_t("int", null);
  }

  /**
   * f0 -> "EncInt"
   */
  public Var_t visit(EncryptedIntegerType n) throws Exception {
    return new Var_t("EncInt", null);
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
   *       | IfStatement()
   *       | WhileStatement()
   *       | ForStatement()
   *       | PrintStatement() ";"
   */
  public Var_t visit(Statement n) throws Exception {
    n.f0.accept(this);
    if (this.semicolon_) {
      this.asm_.append(";\n");
    }
    this.semicolon_ = false;
    return null;
  }

  /**
   * f0 -> "{"
   * f1 -> ( Statement() )*
   * f2 -> "}"
   */
  public Var_t visit(Block n) throws Exception {
    this.asm_.append(" {\n");
    this.indent_ += 2;
    n.f1.accept(this);
    this.indent_ -= 2;
    append2asm("}\n");
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
      append2asm("tmp = uint64_to_hex_string(");
      this.asm_.append(rhs_name);
      this.asm_.append(");\n");
      append2asm("encryptor.encrypt(tmp, ");
      this.asm_.append(lhs.getName()).append(")");
      this.semicolon_ = true;
    } else if (lhs_type.equals("EncInt[]") && rhs_type.equals("int[]")) {
      // if EncInt[] <- int[]
      tmp_cnt_++;
      String tmp_i = "i_" + tmp_cnt_;
      append2asm(lhs.getName());
      this.asm_.append(".resize(").append(rhs_name).append(".size());\n");
      append2asm("for (size_t ");
      this.asm_.append(tmp_i).append(" = 0; ").append(tmp_i).append(" < ");
      this.asm_.append(rhs_name).append(".size(); ++").append(tmp_i);
      this.asm_.append(") {\n");
      this.indent_ += 2;
      append2asm("tmp = uint64_to_hex_string(");
      this.asm_.append(rhs_name).append("[").append(tmp_i).append("]);\n");
      append2asm("encryptor.encrypt(tmp, ");
      this.asm_.append(lhs.getName()).append("[").append(tmp_i).append("]);\n");
      this.indent_ -= 2;
      append2asm("}\n");
    } else if (lhs_type.equals(rhs_type)) {
      // if the destination has the same type as the source.
      append2asm(lhs.getName());
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
      append2asm("tmp = uint64_to_hex_string(1);\n");
      append2asm("evaluator.add_plain_inplace(");
      this.asm_.append(id.getName()).append(", tmp);\n");
    } else {
      append2asm(id.getName());
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
      append2asm("tmp = uint64_to_hex_string(1);\n");
      append2asm("evaluator.sub_plain_inplace(");
      this.asm_.append(id.getName()).append(", tmp);\n");
    } else {
      append2asm(id.getName());
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
      append2asm(lhs.getName());
      this.asm_.append(" ").append(op).append(" ");
      this.asm_.append(rhs.getName());
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      append2asm("evaluator.");
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
        append2asm("evaluator.relinearize_inplace(");
        this.asm_.append(lhs.getName()).append(", relin_keys)");
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      append2asm("evaluator.");
      switch (op) {
        case "+=": this.asm_.append("add_plain("); break;
        case "*=": this.asm_.append("multiply_plain("); break;
        case "-=": this.asm_.append("sub_plain("); break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
      this.asm_.append(", ").append(lhs.getName()).append(")");
    }
    this.semicolon_ = true;
    return null;
  }

  /**
   * f0 -> "+="
   * |   "-="
   * |   "*="
   * |   "/="
   * |   "%="
   * |   "<<="
   * |   ">>="
   * |   "&="
   * |   "|="
   * |   "^="
   */
  public Var_t visit(CompoundOperator n) throws Exception {
    String[] _ret = {"+=", "-=", "*=", "/=", "%=", "<<=", ">>=", "&=", "|=", "^="};
    String op = _ret[n.f0.which];
    return new Var_t("int", op);
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
        append2asm(id.getName());
        this.asm_.append("[").append(idx.getName()).append("] = ");
        this.asm_.append(rhs.getName()).append(";\n");
        break;
      case "EncInt[]":
        if (rhs_type.equals("EncInt")) {
          append2asm(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] = ");
          this.asm_.append(rhs.getName()).append(";\n");
          break;
        } else if (rhs_type.equals("int")) {
          append2asm("tmp = uint64_to_hex_string(");
          this.asm_.append(rhs.getName());
          this.asm_.append(");\n");
          append2asm("encryptor.encrypt(tmp, ");
          this.asm_.append(id.getName()).append("[").append(idx.getName()).append("])");
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
        append2asm(id.getName());
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
        append2asm("vector<uint64_t> ");
        this.asm_.append(tmp_vec).append(" = { ").append(exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" };\n");
        append2asm("batch_encoder.encode(");
        this.asm_.append(tmp_vec).append(", tmp);\n");
        append2asm("encryptor.encrypt(tmp, ");
        this.asm_.append(id.getName()).append(");\n");
        break;
      case "EncInt[]":
        String exp_var;
        if (exp_type.equals("int")) {
          exp_var = newCtxtTemp();
          append2asm("tmp = uint64_to_hex_string(" + exp.getName() + ");\n");
          append2asm("encryptor.encrypt(tmp, ");
          this.asm_.append(exp_var).append(");\n");
        } else { // exp type is EncInt
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            if (exp_type.equals("int")) {
              String tmp_ = newCtxtTemp();
              append2asm("tmp = uint64_to_hex_string(" + init + ");\n");
              append2asm("encryptor.encrypt(tmp, ");
              this.asm_.append(tmp_).append(");\n");
              inits.add(tmp_);
            } else { // exp type is EncInt
              inits.add(init);
            }
          }
        }
        append2asm(id.getName());
        this.asm_.append(" = { ").append(exp_var);
        for (String init : inits) {
          this.asm_.append(", ").append(init);
        }
        this.asm_.append(" };\n");
        break;
      default:
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
    append2asm("vector<uint64_t> ");
    this.asm_.append(tmp_vec).append(" = { ").append(exp.getName());
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); i++) {
        this.asm_.append(", ").append((n.f7.nodes.get(i).accept(this)).getName());
      }
    }
    this.asm_.append(" };\n");
    append2asm("batch_encoder.encode(");
    this.asm_.append(tmp_vec).append(", tmp);\n");
    append2asm("encryptor.encrypt(tmp, ");
    this.asm_.append(id.getName()).append("[").append(index.getName()).append("]);\n");
    return null;
  }

  /**
   * f0 -> ","
   * f1 -> Expression()
   */
  public Var_t visit(BatchAssignmentStatementRest n) throws Exception {
    return n.f1.accept(this);
  }

  /**
   * f0 -> IfthenElseStatement()
   * | IfthenStatement()
   */
  public Var_t visit(IfStatement n) throws Exception {
    return n.f0.accept(this);
  }

  /**
   * f0 -> "if"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public Var_t visit(IfthenStatement n) throws Exception {
    append2asm("if (");
    Var_t cond = n.f2.accept(this);
    this.asm_.append(cond.getName()).append(")");
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
    append2asm("if (");
    Var_t cond = n.f2.accept(this);
    this.asm_.append(cond.getName()).append(")");
    n.f4.accept(this);
    append2asm("else");
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
    append2asm("while (");
    Var_t cond = n.f2.accept(this);
    this.asm_.append(cond.getName()).append(")");
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
    append2asm("for (");
    int prev_indent = this.indent_;
    this.indent_ = 0;
    n.f2.accept(this);
    this.asm_.append("; ");
    Var_t cond = n.f4.accept(this);
    this.asm_.append(cond.getName()).append("; ");
    n.f6.accept(this);
    this.asm_.append(")");
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
        append2asm("cout << ");
        this.asm_.append(expr.getName());
        this.asm_.append(" << endl;\n");
        break;
      case "EncInt":
        append2asm("decryptor.decrypt(");
        this.asm_.append(expr.getName()).append(", tmp);\n");
        append2asm("cout << \"dec(");
        this.asm_.append(expr.getName()).append(") = \" << tmp << endl;\n");
        break;
      default:
        throw new Exception("Bad type for print statement");
    }

    return null;
  }

  /**
   * f0 -> LogicalAndExpression()
   * | LogicalOrExpression()
   * | BinaryExpression()
   * | BinNotExpression()
   * | ArrayLookup()
   * | ArrayLength()
   * | TernaryExpression()
   * | Clause()
   */
  public Var_t visit(Expression n) throws Exception {
    return n.f0.accept(this);
  }

  /**
   * f0 -> Clause()
   * f1 -> "&&"
   * f2 -> Clause()
   */
  public Var_t visit(LogicalAndExpression n) throws Exception {
    n.f0.accept(this);
    this.asm_.append(" && ");
    n.f2.accept(this);
    return null;
  }

  /**
   * f0 -> Clause()
   * f1 -> "||"
   * f2 -> Clause()
   */
  public Var_t visit(LogicalOrExpression n) throws Exception {
    n.f0.accept(this);
    this.asm_.append(" || ");
    n.f2.accept(this);
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
      String res_ = newCtxtTemp();
      append2asm("tmp = uint64_to_hex_string(" + lhs.getName() + ");\n");
      switch (op) {
        case "+":
          append2asm("evaluator.add_plain(" + rhs.getName() + ", tmp, " + res_ + ");\n");
          break;
        case "*":
          append2asm("evaluator.multiply_plain(" + rhs.getName() + ", tmp, " + res_ + ");\n");
          break;
        case "-":
          append2asm("encryptor.encrypt(tmp, tmp_);\n");
          append2asm("evaluator.sub(tmp_, " + rhs.getName() + ", " + res_ + ");\n");
          break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      return new Var_t("EncInt", res_);
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      String res_ = newCtxtTemp();
      String op_str;
      switch (op) {
        case "+": op_str = "add_plain"; break;
        case "*": op_str = "multiply_plain"; break;
        case "-": op_str = "sub_plain"; break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      append2asm("tmp = uint64_to_hex_string(" + rhs.getName() + ");\n");
      append2asm("evaluator." + op_str + "(" + lhs.getName() + ", tmp, " + res_ + ");\n");
      return new Var_t("EncInt", res_);
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      String res_ = newCtxtTemp();
      String op_str;
      switch (op) {
        case "+": op_str = "add"; break;
        case "*": op_str = "multiply"; break;
        case "-": op_str = "sub"; break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      append2asm("evaluator." + op_str + "(" + lhs.getName() + ", " +
                 rhs.getName() + ", " + res_ + ");\n");
      if (op.equals("*")) {
        this.asm_.append(";\n");
        append2asm("evaluator.relinearize_inplace(");
        this.asm_.append(lhs.getName()).append(", relin_keys)");
      }
      return new Var_t("EncInt", res_);
    }
    throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
  }

  /**
   * f0 -> "&"
   * |  "|"
   * |  "^"
   * |  "<<"
   * |  ">>"
   * |  "+"
   * |  "-"
   * |  "*"
   * |  "/"
   * |  "%"
   * |  "=="
   * |  "!="
   * |  "<"
   * |  "<="
   * |  ">"
   * |  ">="
   */
  public Var_t visit(BinOperator n) throws Exception {
    String[] _ret = {"&", "|", "^", "<<", ">>", "+", "-", "*", "/", "%", "==",
                     "!=", "<", "<=", ">", ">="};
    String op = _ret[n.f0.which];
    if ("&".equals(op) || "|".equals(op) || "^".equals(op) || "<<".equals(op) ||
        ">>".equals(op) || "<<=".equals(op) || ">>=".equals(op) || "+".equals(op) ||
        "-".equals(op) || "*".equals(op) || "/".equals(op) || "%".equals(op)) {
      return new Var_t("int", op);
    } else if ("==".equals(op) || "!=".equals(op) || "<".equals(op) ||
               "<=".equals(op) || ">".equals(op) || ">=".equals(op)) {
      return new Var_t("boolean", op);
    } else {
      throw new IllegalStateException("BinOperator: Unexpected value: " + op);
    }
  }

  /**
   * f0 -> "~"
   * f1 -> PrimaryExpression()
   */
  public Var_t visit(BinNotExpression n) throws Exception {
    this.asm_.append("~");
    return n.f1.accept(this);
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
    if (arr_type.equals("int[]")) {
      append2asm("int ");
      ret.setType("int");
    } else if (arr_type.equals("EncInt[]")) {
      append2asm("Ciphertext ");
      ret.setType("EncInt");
    }
    this.asm_.append(ret.getName());
    this.asm_.append(" = ").append(arr.getName()).append("[");
    this.asm_.append(idx.getName()).append("];\n");
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
    this.asm_.append("(");
    n.f1.accept(this);
    this.asm_.append(") ?");
    n.f4.accept(this);
    this.asm_.append(" : ");
    n.f6.accept(this);
    return null;
  }

  /**
   * f0 -> NotExpression()
   * | PrimaryExpression()
   */
  public Var_t visit(Clause n) throws Exception {
    return n.f0.accept(this);
  }

  /**
   * f0 -> IntegerLiteral()
   *       | TrueLiteral()
   *       | FalseLiteral()
   *       | Identifier()
   *       | ArrayAllocationExpression()
   *       | EncryptedArrayAllocationExpression()
   *       | BracketExpression()
   */
  public Var_t visit(PrimaryExpression n) throws Exception {
    return n.f0.accept(this);
  }

  /**
   * f0 -> <INTEGER_LITERAL>
   */
  public Var_t visit(IntegerLiteral n) throws Exception {
    return new Var_t("int", n.f0.toString());
  }

  /**
   * f0 -> "true"
   */
  public Var_t visit(TrueLiteral n) throws Exception {
    return new Var_t("bool", "true");
  }

  /**
   * f0 -> "false"
   */
  public Var_t visit(FalseLiteral n) throws Exception {
    return new Var_t("bool", "false");
  }

  /**
   * f0 -> <IDENTIFIER>
   */
  public Var_t visit(Identifier n) throws Exception {
    return new Var_t(null, n.f0.toString());
  }

  /**
   * f0 -> "new"
   * f1 -> "int"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public Var_t visit(ArrayAllocationExpression n) throws Exception {
    String size = n.f3.accept(this).getName();
    return new Var_t("int[]", "resize(" + size + ")");
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
//    v.resize(5, 10);
    return new Var_t("EncInt[]", "resize(" + size + ")");
  }

  /**
   * f0 -> "!"
   * f1 -> Clause()
   */
  public Var_t visit(NotExpression n) throws Exception {
    this.asm_.append("!");
    n.f1.accept(this);
    return null;
  }

  /**
   * f0 -> "("
   * f1 -> Expression()
   * f2 -> ")"
   */
  public Var_t visit(BracketExpression n) throws Exception {
    Var_t res = n.f1.accept(this);
    return new Var_t(res.getType(), "(" + res.getName() + ")");
  }

}