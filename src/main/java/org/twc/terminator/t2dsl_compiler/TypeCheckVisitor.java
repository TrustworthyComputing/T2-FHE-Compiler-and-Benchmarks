package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;
import org.twc.terminator.t2dsl_compiler.T2DSLvisitor.GJNoArguDepthFirst;

import java.util.ArrayList;
import java.util.List;

public class TypeCheckVisitor extends GJNoArguDepthFirst<Var_t> {

  private SymbolTable st_;

  public TypeCheckVisitor(SymbolTable st) {
    this.st_ = st;
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
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);
    n.f7.accept(this);
    n.f8.accept(this);
    Var_t ret = n.f9.accept(this);
    String ret_type = st_.findType(ret);
    if (!ret_type.equals("int")) {
      throw new Exception("warning: returning '" + ret_type + "' from " +
              "a function with return type 'int'");
    }
    n.f10.accept(this);
    n.f11.accept(this);
    return null;
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ( VarDeclarationRest() )*
   * f3 -> ";"
   */
  public Var_t visit(VarDeclaration n) throws Exception {
    Var_t type = n.f0.accept(this);
    String type_str = type.getType();
    if (type_str == null) type_str = type.getName();
    String assigned_type = (n.f1.accept(this)).getType();
    if (assigned_type != null && !type_str.equals(assigned_type)) {
// TODO(jimouris): probably should remove this
      if (!type_str.equals("EncInt") || !assigned_type.equals("int")) {
        throw new Exception("Error in inline assignment. Different types: " + type_str + " " + assigned_type);
      }
    }
    if (n.f2.present()) {
      for (int i = 0; i < n.f2.size(); i++) {
        assigned_type = (n.f2.nodes.get(i).accept(this)).getType();
        if (assigned_type != null && !type_str.equals(assigned_type)) {
          if (!type_str.equals("EncInt") || !assigned_type.equals("int")) {
            throw new Exception("Error in inline assignment. Different types: " + type_str + " " + assigned_type);
          }
        }
      }
    }
    return null;
  }

  /**
   * f0 -> ","
   * f1 -> Identifier()
   */
  public Var_t visit(VarDeclarationRest n) throws Exception {
    Var_t var = n.f1.accept(this);
    return new Var_t(var.getType(), var.getName());
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
   * f0 -> "bool"
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
   *       | IncrementAssignmentStatement() ";"
   *       | DecrementAssignmentStatement() ";"
   *       | CompoundAssignmentStatement() ";"
   *       | IfStatement()
   *       | WhileStatement()
   *       | ForStatement()
   *       | PrintStatement() ";"
   *       | PrintLineStatement() ";"
   */
  public Var_t visit(Statement n) throws Exception {
    return n.f0.accept(this);
  }

  /**
   * f0 -> "{"
   * f1 -> ( Statement() )*
   * f2 -> "}"
   */
  public Var_t visit(Block n) throws Exception {
    n.f1.accept(this);
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   */
  public Var_t visit(AssignmentStatement n) throws Exception {
    Var_t t1 = n.f0.accept(this);
    n.f1.accept(this);
    Var_t t2 = n.f2.accept(this);
    String t1_type = st_.findType(t1);
    String t2_type = st_.findType(t2);
    if (t1_type.equals(t2_type) ||
        (t1_type.equals("EncInt") && t2_type.equals("int")) ||
        (t1_type.equals("EncInt[]") && t2_type.equals("int[]")) ) {
      return null;
    }
    throw new Exception("Error assignment between different types: " +
                        t1_type + " " + t2_type);
  }

  /**
   * f0 -> Identifier()
   * f1 -> "++"
   */
  public Var_t visit(IncrementAssignmentStatement n) throws Exception {
    Var_t t1 = n.f0.accept(this);
    n.f1.accept(this);
    String t1_type = st_.findType(t1);
    if (!(t1_type.equals("int") || t1_type.equals("EncInt"))) {
      throw new Exception("Error increment assignment (++) is only allowed to" +
                          " integers. Found " + t1_type);
    }
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "--"
   */
  public Var_t visit(DecrementAssignmentStatement n) throws Exception {
    Var_t t1 = n.f0.accept(this);
    n.f1.accept(this);
    String t1_type = st_.findType(t1);
    if (!(t1_type.equals("int") || t1_type.equals("EncInt"))) {
      throw new Exception("Error decrement assignment (--) is only allowed to" +
                          " integers. Found " + t1_type);
    }
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> CompoundOperator()
   * f2 -> Expression()
   */
  public Var_t visit(CompoundAssignmentStatement n) throws Exception {
    Var_t t1 = n.f0.accept(this);
    String operator = n.f1.accept(this).getName();
    Var_t t2 = n.f2.accept(this);
    String t1_t = st_.findType(t1);
    String t2_t = st_.findType(t2);
    if ((t1_t.equals("int") && t2_t.equals("int")) ||
        (t1_t.equals("EncInt") && (t2_t.equals("int") || t2_t.equals("EncInt")))) {
      return null;
    }
    throw new Exception("Error compound assignment between different types (" +
                        operator + ") : " + t1_t + " " + t2_t);
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
    return new Var_t("int", _ret[n.f0.which]);
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
    Var_t array = n.f0.accept(this);
    String array_type = st_.findType(array);
    Var_t idx = n.f2.accept(this);
    n.f1.accept(this);
    Var_t val = n.f5.accept(this);
    String idx_type = st_.findType(idx);
    String val_type = st_.findType(val);
    if (!idx_type.equals("int")) {
      throw new Exception("Array index should be an integer: " + idx_type);
    }
    if (array_type.equals("int[]") && val_type.equals("int")) {
      return null;
    } else if (array_type.equals("EncInt[]") &&
                (val_type.equals("EncInt") || val_type.equals("int"))) {
      return null;
    }
    throw new Exception("Error: assignment in " + array_type + " array an "
                        + val_type + " type");
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
    Var_t exp = n.f3.accept(this);
    String id_type = st_.findType(id);
    String exp_type_first = st_.findType(exp);
    if (!((id_type.equals("int[]") && exp_type_first.equals("int")) ||
          (id_type.equals("EncInt") && exp_type_first.equals("int")) ||
          (id_type.equals("EncInt[]") && exp_type_first.equals("int")) ||
          (id_type.equals("EncInt[]") && exp_type_first.equals("EncInt")) )) {
      throw new Exception("Error in batching assignment between different " +
                          "types: " + id_type + " {" + exp_type_first + "}");
    }
    if (n.f4.present()) {
      for (int i = 0; i < n.f4.size(); i++) {
        String exp_type = st_.findType((n.f4.nodes.get(i).accept(this)));
        if (exp_type_first != exp_type) {
          throw new Exception("Error in batching assignment types mismatch: " +
                              exp_type_first + " " + exp_type);
        }
      }
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
    String index_type = st_.findType(index);
    String exp_type_first = st_.findType(exp);
    if (!index_type.equals("int")) {
      throw new Exception("array index type mismatch: " + index_type);
    }
    if (!(id_type.equals("EncInt[]") && exp_type_first.equals("int"))) {
      throw new Exception("Error in batching assignment between different " +
              "types: " + id_type + " {" + exp_type_first + "}");
    }
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); i++) {
        String exp_type = st_.findType((n.f7.nodes.get(i).accept(this)));
        if (exp_type_first != exp_type) {
          throw new Exception("Error in batching assignment types mismatch: " +
                  exp_type_first + " " + exp_type);
        }
      }
    }
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
    n.f0.accept(this);
    n.f1.accept(this);
    Var_t expr = n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    String expr_type = st_.findType(expr);
    if (expr_type.equals("bool")) {
      return null;
    } else if (expr_type.equals("EncInt")) {
      throw new Exception("IfthenStatement Cannot branch on encrypted data: " + expr_type);
    }
    throw new Exception("IfthenStatement is not a boolean expression: " + expr_type);
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
    n.f0.accept(this);
    n.f1.accept(this);
    Var_t expr = n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    n.f6.accept(this);
    String expr_type = st_.findType(expr);
    if (expr_type.equals("bool")) {
      return null;
    } else if (expr_type.equals("EncInt")) {
      throw new Exception("IfthenElseStatement Cannot branch on encrypted data: " + expr_type);
    }
    throw new Exception("IfthenElseStatement is not a boolean expression: " + expr_type);
  }

  /**
   * f0 -> "while"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public Var_t visit(WhileStatement n) throws Exception {
    n.f0.accept(this);
    n.f1.accept(this);
    Var_t expr = n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    String expr_type = st_.findType(expr);
    if (expr_type.equals("bool")) {
      return null;
    } else if (expr_type.equals("EncInt")) {
      throw new Exception("WhileStatement Cannot branch on encrypted data: " + expr_type);
    }
    throw new Exception("WhileStatement is not a boolean Expression");
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
    n.f2.accept(this);
    Var_t cond = n.f4.accept(this);
    n.f6.accept(this);
    n.f8.accept(this);
    String cond_type = st_.findType(cond);
    if (cond_type.equals("bool")) {
      return null;
    } else if (cond_type.equals("EncInt")) {
      throw new Exception("For Statement cannot branch on encrypted data: " + cond_type);
    } else {
      throw new Exception("The condition in the for loop is not a boolean expression");
    }
  }

  /**
   * f0 -> "System.out.println"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   */
  public Var_t visit(PrintStatement n) throws Exception { //is int
    n.f0.accept(this);
    n.f1.accept(this);
    Var_t expr = n.f2.accept(this);
    n.f3.accept(this);
    String expr_type = st_.findType(expr);
    if (expr_type.equals("bool") || expr_type.equals("int") ||
          expr_type.equals("EncInt")) {
      return null;
    }
    throw new Exception("Print statement not boolean, int, or EncInt.");
  }

  /**
   * f0 -> "System.out.println"
   * f1 -> "("
   * f2 -> ")"
   */
  public Var_t visit(PrintLineStatement n) throws Exception {
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    return null;
  }

  /**
   * f0 -> LogicalAndExpression()
   * | LogicalOrExpression()
   * | BinaryExpression()
   * | BinNotExpression()
   * | ArrayLookup()
   * | ArrayLength()
   * | MessageSend()
   * | TernaryExpression()
   * | PublicReadExpression()
   * | PrivateReadExpression()
   * | PublicSeekExpression()
   * | PrivateSeekExpression()
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
    Var_t clause_1 = n.f0.accept(this);
    n.f1.accept(this);
    Var_t clause_2 = n.f2.accept(this);
    String t1 = st_.findType(clause_1);
    String t2 = st_.findType(clause_2);
    if (t1.equals("bool") && t2.equals("bool")) {
      return new Var_t("bool", null);
    }
    throw new Exception("Bad operand types for operator '&&': " + t1 + " " + t2);
  }

  /**
   * f0 -> Clause()
   * f1 -> "||"
   * f2 -> Clause()
   */
  public Var_t visit(LogicalOrExpression n) throws Exception {
    Var_t clause_1 = n.f0.accept(this);
    n.f1.accept(this);
    Var_t clause_2 = n.f2.accept(this);
    String t1 = st_.findType(clause_1);
    String t2 = st_.findType(clause_2);
    if (t1.equals("bool") && t2.equals("bool")) {
      return new Var_t("bool", null);
    }
    throw new Exception("Bad operand types for operator '||': " + t1 + " " + t2);
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> BinOperator()
   * f2 -> PrimaryExpression()
   */
  public Var_t visit(BinaryExpression n) throws Exception {
    Var_t clause_1 = n.f0.accept(this);
    String op = n.f1.accept(this).getName();
    Var_t clause_2 = n.f2.accept(this);
    String t1 = st_.findType(clause_1);
    String t2 = st_.findType(clause_2);
    if ("&".equals(op) || "|".equals(op) || "^".equals(op) || "<<".equals(op) ||
        ">>".equals(op) || "+".equals(op) || "-".equals(op) || "*".equals(op) ||
        "/".equals(op) || "%".equals(op)
    ) {
      if (t1.equals("int") && t2.equals("int")) {
        return new Var_t("int", null);
      } else if ((t1.equals("int") || t1.equals("EncInt")) &&
                    (t2.equals("int") || t2.equals("EncInt"))) {
        return new Var_t("EncInt", null);
      }
    } else if ("==".equals(op) || "!=".equals(op) || "<".equals(op) ||
               "<=".equals(op) || ">".equals(op) || ">=".equals(op)) {
      if (t1.equals("bool") && t2.equals("bool")) {
        return new Var_t("bool", null);
      } else if (t1.equals("int") && t2.equals("int")) {
        return new Var_t("bool", null);
      } else if (t1.equals("EncInt") && t2.equals("EncInt")) {
        return new Var_t("EncInt", null);
      } else if (t1.equals("EncInt") && t2.equals("int") || t1.equals("int")
                  && t2.equals("EncInt")) {
        return new Var_t("EncInt", null);
      }
    }
    throw new Exception("Bad operand types for operator '" + op + "': " + t1 +
            " " + t2);
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
      return new Var_t("bool", op);
    } else {
      throw new IllegalStateException("BinOperator: Unexpected value: " + op);
    }
  }

  /**
   * f0 -> "~"
   * f1 -> PrimaryExpression()
   */
  public Var_t visit(BinNotExpression n) throws Exception {
    Var_t clause_1 = n.f1.accept(this);
    String t1 = st_.findType(clause_1);
    if (t1.equals("int")) {
      return new Var_t("int", null);
    } else if (t1.equals("EncInt")) {
      return new Var_t("EncInt", null);
    }
    throw new Exception("Bad operand type for operator '~': " + t1);
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  public Var_t visit(ArrayLookup n) throws Exception {
    Var_t array = n.f0.accept(this);
    n.f1.accept(this);
    Var_t idx = n.f2.accept(this);
    String array_type = st_.findType(array);
    String idx_type = st_.findType(idx);
    if (array_type.equals("int[]") && idx_type.equals("int")) {
      return new Var_t("int", null);
    } else if (array_type.equals("EncInt[]") && idx_type.equals("int")) {
      return new Var_t("EncInt", null);
    }
    throw new Exception("ArrayLookup between different types: " + array_type + " " + idx_type);
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
    Var_t expr = n.f1.accept(this);
    String expr_type = st_.findType(expr);
    Var_t expr_1 = n.f4.accept(this);
    String expr_1_type = st_.findType(expr_1);
    Var_t expr_2 = n.f6.accept(this);
    String expr_2_type = st_.findType(expr_2);
    if (expr_type.equals("bool")) {
      if (expr_1_type.equals(expr_2_type)) {
        return new Var_t(expr_1_type, null);
      }
      throw new Exception("Ternary types mismatch: " + expr_1_type + " " + expr_2_type);
    } else if (expr_type.equals("EncInt")) {
      if (expr_1_type.equals(expr_2_type) || (
              expr_1_type.equals("int") || expr_1_type.equals("EncInt") &&
                      expr_2_type.equals("int") || expr_2_type.equals("EncInt"))
      ) {
        return new Var_t("EncInt", null);
      }
      throw new Exception("Ternary types mismatch: " + expr_1_type + " " + expr_2_type);
    }
    throw new Exception("If-condition is not a boolean Expression " + expr_type);
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
   * | TrueLiteral()
   * | FalseLiteral()
   * | Identifier()
   * | ThisExpression()
   * | ArrayAllocationExpression()
   * | EncryptedArrayAllocationExpression()
   * | AllocationExpression()
   * | BracketExpression()
   */
  public Var_t visit(PrimaryExpression n) throws Exception {
    return n.f0.accept(this);
  }

  /**
   * f0 -> <INTEGER_LITERAL>
   */
  public Var_t visit(IntegerLiteral n) throws Exception {
    return new Var_t("int", null);
  }

  /**
   * f0 -> "true"
   */
  public Var_t visit(TrueLiteral n) throws Exception {
    return new Var_t("bool", null);
  }

  /**
   * f0 -> "false"
   */
  public Var_t visit(FalseLiteral n) throws Exception {
    return new Var_t("bool", null);
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
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    Var_t t = n.f3.accept(this);
    String t_type = st_.findType(t);
    if (!t_type.equals("int")) {
      throw new Exception("Error: new int[" + t_type + "], " + t_type + " should be int");
    }
    n.f4.accept(this);
    return new Var_t("int[]", null);
  }

  /**
   * f0 -> "new"
   * f1 -> "EncInt"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public Var_t visit(EncryptedArrayAllocationExpression n) throws Exception {
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    Var_t t = n.f3.accept(this);
    String t_type = st_.findType(t);
    if (!t_type.equals("int")) {
      throw new Exception("Error: new EncInt[" + t_type + "], " + t_type + " should be int");
    }
    n.f4.accept(this);
    return new Var_t("EncInt[]", null);
  }

  /**
   * f0 -> "!"
   * f1 -> Clause()
   */
  public Var_t visit(NotExpression n) throws Exception {
    n.f0.accept(this);
    Var_t t = n.f1.accept(this);
    String t_type = st_.findType(t);
    if (t_type.equals("bool")) {
      return new Var_t("bool", null);
    }
    throw new Exception("Error: Not Clause, " + t_type + " type_ given. Can apply only to boolean");
  }

  /**
   * f0 -> "("
   * f1 -> Expression()
   * f2 -> ")"
   */
  public Var_t visit(BracketExpression n) throws Exception {
    n.f0.accept(this);
    n.f2.accept(this);
    return n.f1.accept(this);
  }

}
