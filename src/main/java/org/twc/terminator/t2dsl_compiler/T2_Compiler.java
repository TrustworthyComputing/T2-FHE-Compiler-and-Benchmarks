package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.Main;
import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;
import org.twc.terminator.t2dsl_compiler.T2DSLvisitor.GJNoArguDepthFirst;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public abstract class T2_Compiler extends GJNoArguDepthFirst<Var_t> {

  protected SymbolTable st_;

  protected StringBuilder asm_;
  protected int indent_, tmp_cnt_, word_sz_;
  protected boolean semicolon_;
  protected String config_file_path_;
  protected String tmp_i;
  protected boolean is_binary_, timer_used_;
  protected String tstart_, tstop_, tdur_;

  public T2_Compiler(SymbolTable st, String config_file_path, int word_sz) {
    this.indent_ = 0;
    this.tmp_cnt_ = 0;
    this.st_ = st;
    this.st_.backend_types.put("int", "int");
    this.st_.backend_types.put("double", "double");
    this.st_.backend_types.put("int[]", "vector<int>");
    this.st_.backend_types.put("double[]", "vector<double>");
    this.semicolon_ = false;
    this.asm_ = new StringBuilder();
    this.config_file_path_ = config_file_path;
    this.tmp_i = "tmp_i";
    this.word_sz_ = word_sz;
    this.is_binary_ = (word_sz > 0);
    this.tstart_ = "start_timer";
    this.tstop_ = "stop_timer";
    this.tdur_ = "duration";
    this.timer_used_ = false;
  }

  public Main.ENC_TYPE getScheme() {
    return this.st_.getScheme();
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

  protected String new_ctxt_tmp() {
    tmp_cnt_++;
    String ctxt_tmp_ = "tmp_" + tmp_cnt_ + "_";
    append_idx(this.st_.backend_types.get("EncInt"));
    this.asm_.append(" ").append(ctxt_tmp_);
    if (this.is_binary_) {
      this.asm_.append("(").append(this.word_sz_).append(")");
    }
    this.asm_.append(";\n");
    return ctxt_tmp_;
  }

  protected int[] int_to_bin_array(int n) {
    int[] b = new int[this.word_sz_];
    for (int i = 0; i < this.word_sz_; ++i) {
      b[this.word_sz_ - i - 1] = (n >> i) & 1;
    }
    return b;
  }

  public static boolean isNumeric(String str) {
    if (str == null || str.equals("")) return false;
    try {
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException ignored) { }
    return false;
  }

  protected abstract void append_keygen();

  protected boolean read_keygen_from_file() {
    try {
      Path filePath = Paths.get(config_file_path_);
      Stream<String> lines = Files.lines(filePath);
      lines.forEach((line)->{
        this.asm_.append("  ").append(line).append("\n");
      });
      this.asm_.append("\n");
      System.out.println("[ \033[0;32m \u2713 \033[0m ] Configuration file found");
      return true;
    } catch (InvalidPathException | IOException e) {
      System.out.println("[ \033[1;33m ! \033[0m ] Configuration file not " +
                         "found, using default config.");
      return false;
    }
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
    if (this.is_binary_ && type.equals("EncInt")) {
      this.asm_.append("(").append(this.word_sz_).append(")");
    }
    if (n.f2.present()) {
      for (int i = 0; i < n.f2.size(); i++) {
        n.f2.nodes.get(i).accept(this);
        if (this.is_binary_ && type.equals("EncInt")) {
          this.asm_.append("(").append(this.word_sz_).append(")");
        }
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
   * | DoubleArrayType()
   * | EncryptedArrayType()
   * | EncryptedDoubleArrayType()
   * | BooleanType()
   * | IntegerType()
   * | EncryptedIntegerType()
   * | DoubleType()
   * | EncryptedDoubleType()
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
   * f0 -> "double"
   * f1 -> "["
   * f2 -> "]"
   */
  public Var_t visit(DoubleArrayType n) throws Exception {
    return new Var_t("double[]", null);
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
   * f0 -> "EncDouble"
   * f1 -> "["
   * f2 -> "]"
   */
  public Var_t visit(EncryptedDoubleArrayType n) throws Exception {
    return new Var_t("EncDouble[]", null);
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
   * f0 -> "double"
   */
  public Var_t visit(DoubleType n) throws Exception {
    return new Var_t("double", null);
  }

  /**
   * f0 -> "EncDouble"
   */
  public Var_t visit(EncryptedDoubleType n) throws Exception {
    return new Var_t("EncDouble", null);
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
   *       | StartTimerStatement() ";"
   *       | StopTimerStatement() ";"
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
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> CompoundOperator()
   * f5 -> Expression()
   */
  public abstract Var_t visit(CompoundArrayAssignmentStatement n) throws Exception;

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
    String[] _ret = {"+=", "-=", "*=", "/=", "%=", "<<=", ">>=", ">>>=", "&=", "|=", "^="};
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
   * f0 -> "print_batched"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ","
   * f4 -> Expression()
   * f5 -> ")"
   */
  public abstract Var_t visit(PrintBatchedStatement n) throws Exception;

  /**
   * f0 -> <REDUCE_NOISE>
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   */
  public abstract Var_t visit(ReduceNoiseStatement n) throws Exception;


  /**
   * f0 -> <START_TIMER>
   * f1 -> "("
   * f2 -> ")"
   */
  public Var_t visit(StartTimerStatement n) throws Exception {
    if (!this.timer_used_) append_idx("auto " + this.tstart_);
    else append_idx(this.tstart_);
    this.asm_.append(" = chrono::high_resolution_clock::now();\n");
    return null;
  }

  /**
   * f0 -> <END_TIMER>
   * f1 -> "("
   * f2 -> ")"
   */
  public Var_t visit(StopTimerStatement n) throws Exception {
    if (!this.timer_used_) append_idx("auto " + this.tstop_);
    else append_idx(this.tstop_);
    this.asm_.append(" = chrono::high_resolution_clock::now();\n");
    if (!this.timer_used_) append_idx("auto " + this.tdur_);
    else append_idx(this.tdur_);
    this.asm_.append(" = chrono::duration_cast<chrono::milliseconds>(");
    this.asm_.append(this.tstop_).append("-").append(this.tstart_).append(");\n");
    append_idx("cout << \"Time: \" << " + this.tdur_);
    this.asm_.append(".count() << \"ms\" << endl;\n");
    this.timer_used_ = true;
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
  public abstract Var_t visit(BinaryExpression n) throws Exception;

  /**
   * f0 -> "&"
   * |  "|"
   * |  "^"
   * |  "<<"
   * |  ">>"
   * |  ">>>"
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
    String[] _ret = {"&", "|", "^", "<<", ">>", ">>>", "+", "-", "*", "/", "%",
                     "==", "!=", "<", "<=", ">", ">="};
    String op = _ret[n.f0.which];
    if ("&".equals(op) || "|".equals(op) || "^".equals(op) ||
        "<<".equals(op) || ">>".equals(op) || ">>>".equals(op) ||
        "<<=".equals(op) || ">>=".equals(op) || ">>>=".equals(op) ||
        "+".equals(op) || "-".equals(op) || "*".equals(op) ||
        "/".equals(op) || "%".equals(op)) {
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
  public abstract Var_t visit(BinNotExpression n) throws Exception;

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
        append_idx(this.st_.backend_types.get("int"));
        ret.setType("int");
        break;
      case "double[]":
        append_idx(this.st_.backend_types.get("double"));
        ret.setType("double");
        break;
      case "EncInt[]":
        append_idx(this.st_.backend_types.get("EncInt"));
        ret.setType("EncInt");
        break;
      case "EncDouble[]":
        append_idx(this.st_.backend_types.get("EncDouble"));
        ret.setType("EncDouble");
        break;
    }
    this.asm_.append(" ").append(ret.getName());
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
  public abstract Var_t visit(TernaryExpression n) throws Exception;

  /**
   * f0 -> NotExpression()
   * | PrimaryExpression()
   */
  public Var_t visit(Clause n) throws Exception {
    return n.f0.accept(this);
  }

  /**
   * f0 -> IntegerLiteral()
   *       | DoubleLiteral()
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
   * f0 -> <DOUBLE_LITERAL>
   */
  public Var_t visit(DoubleLiteral n) throws Exception {
    return new Var_t("double", n.f0.toString());
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
    return new Var_t("EncInt[]", "resize(" + size + ")");
  }

  /**
   * f0 -> "new"
   * f1 -> "double"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public Var_t visit(ArrayDoubleAllocationExpression n) throws Exception {
    String size = n.f3.accept(this).getName();
    return new Var_t("double[]", "resize(" + size + ")");
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
    return new Var_t("EncDouble[]", "resize(" + size + ")");
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