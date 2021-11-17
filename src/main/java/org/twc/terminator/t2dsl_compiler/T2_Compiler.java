package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;
import org.twc.terminator.t2dsl_compiler.T2DSLvisitor.GJNoArguDepthFirst;

public abstract class T2_Compiler extends GJNoArguDepthFirst<Var_t> {

  protected SymbolTable st_;

  protected StringBuilder asm_;
  protected int indent_, tmp_cnt_;
  protected boolean semicolon_;

  public T2_Compiler(SymbolTable st) {
    this.indent_ = 0;
    this.tmp_cnt_ = 0;
    this.st_ = st;
    this.semicolon_ = false;
    this.asm_ = new StringBuilder();
  }

  public String get_asm() {
    return asm_.toString();
  }

  protected void append_idx(String str) {
    for (int i = 0; i < this.indent_; ++i) {
      this.asm_.append(" ");
    }
    this.asm_.append(str);
  }

  protected abstract String new_ctxt_tmp();

  protected abstract void append_keygen();

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
  public abstract Var_t visit(MainClass n) throws Exception;

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
    append_idx("}\n");
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   */
  public abstract Var_t visit(AssignmentStatement n) throws Exception;

  /**
   * f0 -> Identifier()
   * f1 -> "++"
   */
  public abstract Var_t visit(IncrementAssignmentStatement n) throws Exception;

  /**
   * f0 -> Identifier()
   * f1 -> "--"
   */
  public abstract Var_t visit(DecrementAssignmentStatement n) throws Exception;

  /**
   * f0 -> Identifier()
   * f1 -> CompoundOperator()
   * f2 -> Expression()
   */
  public abstract Var_t visit(CompoundAssignmentStatement n) throws Exception;

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
  public abstract Var_t visit(ArrayAssignmentStatement n) throws Exception;

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> "{"
   * f3 -> Expression()
   * f4 -> ( BatchAssignmentStatementRest() )*
   * f5 -> "}"
   */
  public abstract Var_t visit(BatchAssignmentStatement n) throws Exception;

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
  public abstract Var_t visit(BatchArrayAssignmentStatement n) throws Exception;

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
    append_idx("if (");
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
    append_idx("if (");
    Var_t cond = n.f2.accept(this);
    this.asm_.append(cond.getName()).append(")");
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
    append_idx("while (");
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
    append_idx("for (");
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
  public abstract Var_t visit(PrintStatement n) throws Exception;

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
  public abstract Var_t visit(BinaryExpression n) throws Exception;

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
  public abstract Var_t visit(ArrayLookup n) throws Exception;

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
  public abstract Var_t visit(ArrayAllocationExpression n) throws Exception;

  /**
   * f0 -> "new"
   * f1 -> "EncInt"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public abstract Var_t visit(EncryptedArrayAllocationExpression n) throws Exception;

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