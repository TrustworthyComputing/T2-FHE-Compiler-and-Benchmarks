package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;
import org.twc.terminator.t2dsl_compiler.T2DSLvisitor.GJNoArguDepthFirst;

import java.util.List;
import java.util.ArrayList;

/* Second Visitor Pattern creates the Symbol Table */
public class SymbolTableVisitor extends GJNoArguDepthFirst<Var_t> {

  private SymbolTable st_ = null;

  public SymbolTable getSymbolTable() {
    return st_;
  }

  public void printSymbolTable() {
    st_.printMethod();
    System.out.println();
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
    st_ = new SymbolTable();
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    n.f5.accept(this);
    if (n.f6.present()) {
      for (int i = 0; i < n.f6.size(); ++i) {
        Var_t variable = n.f6.nodes.get(i).accept(this);
        String var_t = variable.getType();
        if (!(var_t.equals("int") || var_t.equals("EncInt") ||
              var_t.equals("double") || var_t.equals("EncDouble") ||
              var_t.equals("bool") ||
              var_t.equals("int[]") || var_t.equals("double[]") ||
              var_t.equals("EncInt[]") || var_t.equals("EncDouble[]")) ) {
          throw new Exception("Unknown type " + var_t);
        }
        if (variable.getVarList() != null) {
          for (Var_t var : variable.getVarList()) {
            if (st_.addVar(new Var_t(var.getType(), var.getName()))) {
              throw new Exception("Variable " + var.getName() + " already exists");
            }
          }
        } else {
          if (st_.addVar(new Var_t(variable.getType(), variable.getName()))) {
            throw new Exception("Variable " + variable.getName() + " already exists");
          }
        }
      }
    }
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); ++i) {
        n.f7.nodes.get(i).accept(this);
      }
    }
    n.f8.accept(this);
    n.f9.accept(this);
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
    Var_t var = n.f0.accept(this);
    var.setName(n.f1.accept(this).getName());
    if (n.f2.present()) {
      List<Var_t> more_vars = new ArrayList<>();
      more_vars.add(var);
      for (int i = 0; i < n.f2.size(); ++i) {
        Var_t curr_var = n.f2.nodes.get(i).accept(this);
        curr_var.setType(var.getType());
        more_vars.add(curr_var);
      }
      return new Var_t(more_vars);
    }
    return var;
  }

  /**
   * f0 -> ","
   * f1 -> Identifier()
   */
  public Var_t visit(VarDeclarationRest n) throws Exception {
    n.f0.accept(this);
    String var_id = n.f1.accept(this).getName();
    return new Var_t("", var_id);
  }

  /**
   * f0 -> Block()
   * | AssignmentStatement() ";"
   * | IncrementAssignmentStatement() ";"
   * | DecrementAssignmentStatement() ";"
   * | CompoundAssignmentStatement() ";"
   * | ArrayAssignmentStatement() ";"
   * | IfStatement()
   * | WhileStatement()
   * | ForStatement()
   * | PrintStatement() ";"
   * | PrintLineStatement() ";"
   */
  public Var_t visit(Statement n) throws Exception {
    return n.f0.accept(this);
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
    n.f4.accept(this);
    n.f6.accept(this);
    n.f8.accept(this);
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
   * f0 -> <IDENTIFIER>
   */
  public Var_t visit(Identifier n) throws Exception {
    String id = n.f0.toString();
    return new Var_t(null, id);
  }

}
