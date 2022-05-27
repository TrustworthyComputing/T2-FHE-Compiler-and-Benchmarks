package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_TFHE extends T2_Compiler {

  private boolean print_bin_;

  public T2_2_TFHE(SymbolTable st, String config_file_path, int word_sz,
                   boolean print_bin) {
    super(st, config_file_path, word_sz);
    this.print_bin_ = print_bin;
    this.st_.backend_types.put("EncInt", "vector<LweSample*>");
    this.st_.backend_types.put("EncInt[]", "vector<vector<LweSample*>>");
  }

  protected void append_keygen() {
    append_idx("const size_t word_sz = " + this.word_sz_ + ";\n");
    append_idx("const size_t minimum_lambda = 128;\n");
    append_idx("TFheGateBootstrappingParameterSet* params =\n");
    append_idx("  new_default_gate_bootstrapping_parameters(minimum_lambda);\n");
    append_idx("uint32_t seed[] = { 314, 1592, 657 };\n");
    append_idx("tfhe_random_generator_setSeed(seed, 3);\n");
    append_idx("TFheGateBootstrappingSecretKeySet* key =\n");
    append_idx("  new_random_gate_bootstrapping_secret_keyset(params);\n");
    append_idx("LweSample *tmp, *tmp_;\n\n");
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
    append_idx("#include <iostream>\n");
    append_idx("#include <bitset>\n");
    append_idx("#include <chrono>\n\n");
    append_idx("#include <tfhe/tfhe.h>\n");
    append_idx("#include <tfhe/tfhe_io.h>\n");
    append_idx("#include <tfhe/tfhe_generic_streams.h>\n\n");
    append_idx("#include \"../functional_units/functional_units.hpp\"\n\n");
    append_idx("using namespace std;\n\n");
    append_idx("int main(void) {\n");
    this.indent_ = 2;
    if (!read_keygen_from_file()) {
      append_keygen();
    }
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
      append_idx(lhs.getName());
      this.asm_.append(" = e_client(").append(rhs_name).append(", word_sz, key)");
      this.semicolon_ = true;
    } else if (lhs_type.equals("EncInt[]") && rhs_type.equals("int[]")) {
      // if EncInt[] <- int[]
      append_idx(lhs.getName());
      this.asm_.append(".resize(").append(rhs_name).append(".size());\n");
      append_idx("for (size_t ");
      this.asm_.append(this.tmp_i).append(" = 0; ").append(this.tmp_i).append(" < ");
      this.asm_.append(rhs_name).append(".size(); ++").append(this.tmp_i);
      this.asm_.append(") {\n");
      this.indent_ += 2;
      append_idx(lhs.getName());
      this.asm_.append("[").append(this.tmp_i).append("] = e_cloud(");
      this.asm_.append(rhs_name).append("[").append(this.tmp_i).append("], ");
      this.asm_.append("word_sz, &key->cloud);\n");
      this.indent_ -= 2;
      append_idx("}\n");
    } else if (lhs_type.equals(rhs_type)) {
      // if the destination has the same type as the source.
      switch (lhs_type) {
        case "int":
        case "int[]":
        case "EncInt[]":
          append_idx(lhs.getName());
          if (rhs_name.startsWith("resize(")) {
            this.asm_.append(".");
          } else {
            this.asm_.append(" = ");
          }
          this.asm_.append(rhs_name);
          break;
        case "EncInt":
          append_idx("copy(");
          this.asm_.append(lhs.getName()).append(", ").append(rhs_name);
          this.asm_.append(", word_sz, &key->cloud)");
          break;
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
    if (id_type.equals("EncInt")) {
      append_idx("inc_inplace(");
      this.asm_.append(id.getName()).append(", word_sz, &key->cloud);\n");
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
      append_idx("dec_inplace(");
      this.asm_.append(id.getName()).append(", word_sz, &key->cloud);\n");
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
    } else if (lhs_type.equals("EncInt")) {
      String rhs_enc = null;
      switch (rhs_type) {
        case "int":
          rhs_enc = new_ctxt_tmp();
          append_idx(rhs_enc);
          this.asm_.append(" = e_cloud(").append(rhs.getName());
          this.asm_.append(", word_sz, &key->cloud);\n");
          break;
        case "EncInt":
          rhs_enc = rhs.getName();
          break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
      switch (op) {
        case "+=":
          append_idx("add(");
          this.asm_.append(lhs.getName()).append(", ");
          this.asm_.append(lhs.getName()).append(", ").append(rhs_enc);
          this.asm_.append(", word_sz, &key->cloud)");
          break;
        case "*=":
          append_idx("mult(");
          this.asm_.append(lhs.getName()).append(", ");
          this.asm_.append(lhs.getName()).append(", ").append(rhs_enc);
          this.asm_.append(", word_sz, &key->cloud)");
          break;
        case "-=":
          append_idx("sub(");
          this.asm_.append(lhs.getName()).append(", ");
          this.asm_.append(lhs.getName()).append(", ").append(rhs_enc);
          this.asm_.append(", word_sz, &key->cloud)");
          break;
        case "<<=":
          append_idx("shift_left_bin(");
          this.asm_.append(lhs.getName()).append(", ");
          this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
          this.asm_.append(", word_sz, &key->cloud)");
          break;
        case ">>=":
          append_idx("shift_right_bin(");
          this.asm_.append(lhs.getName()).append(", ");
          this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
          this.asm_.append(", word_sz, &key->cloud)");
          break;
        case ">>>=":
          append_idx("shift_right_logical_bin(");
          this.asm_.append(lhs.getName()).append(", ");
          this.asm_.append(lhs.getName()).append(", ").append(rhs.getName());
          this.asm_.append(", word_sz, &key->cloud)");
          break;
        default:
          throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
    } else {
      throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
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
    if (id_type.equals("int[]")) {
      append_idx(id.getName());
      this.asm_.append("[").append(idx.getName()).append("] ").append(op);
      this.asm_.append(" ").append(rhs.getName());
    } else if (id_type.equals("EncInt[]")) {
      if (rhs_type.equals("EncInt")) {
        if (op.equals("+=")) {
          append_idx("add(");
        } else if (op.equals("*=")) {
          append_idx("mult(");
        } else if (op.equals("-=")) {
          append_idx("sub(");
        } else {
          throw new Exception("Error in compound array assignment");
        }
        this.asm_.append(id.getName()).append("[").append(idx.getName());
        this.asm_.append("], ").append(id.getName()).append("[");
        this.asm_.append(idx.getName()).append("], ").append(rhs.getName());
        this.asm_.append(", word_sz, &key->cloud)");
      } else if (rhs_type.equals("int")) {
        if ("<<=".equals(op)) {
          append_idx("shift_left_bin(");
          this.asm_.append(id.getName()).append("[").append(idx.getName());
          this.asm_.append("], ").append(id.getName());
          this.asm_.append("[").append(idx.getName());
          this.asm_.append("], ").append(rhs.getName());
          this.asm_.append(", word_sz, &key->cloud)");
        } else if (">>=".equals(op)) {
          append_idx("shift_right_bin(");
          this.asm_.append(id.getName()).append("[").append(idx.getName());
          this.asm_.append("], ").append(id.getName());
          this.asm_.append("[").append(idx.getName());
          this.asm_.append("], ").append(rhs.getName());
          this.asm_.append(", word_sz, &key->cloud)");
        } else if (">>>=".equals(op)) {
          append_idx("shift_right_logical_bin(");
          this.asm_.append(id.getName()).append("[").append(idx.getName());
          this.asm_.append("], ").append(id.getName());
          this.asm_.append("[").append(idx.getName());
          this.asm_.append("], ").append(rhs.getName());
          this.asm_.append(", word_sz, &key->cloud)");
        } else {
          String rhs_enc = new_ctxt_tmp();
          append_idx(rhs_enc);
          this.asm_.append(" = e_cloud(").append(rhs.getName());
          this.asm_.append(", word_sz, &key->cloud);\n");
          if (op.equals("+=")){
            append_idx("add(" + id.getName() + "[" + idx.getName() + "], ");
            this.asm_.append(id.getName()).append("[").append(idx.getName());
            this.asm_.append("], ").append(rhs_enc);
            this.asm_.append(", word_sz, &key->cloud)");
          } else if ("*=".equals(op)) {
            append_idx("mult(" + id.getName() + "[" + idx.getName() + "], ");
            this.asm_.append(id.getName()).append("[").append(idx.getName());
            this.asm_.append("], ").append(rhs_enc);
            this.asm_.append(", word_sz, &key->cloud)");
          } else if ("-=".equals(op)) {
            append_idx("sub(" + id.getName() + "[" + idx.getName() + "], ");
            this.asm_.append(id.getName()).append("[").append(idx.getName());
            this.asm_.append("], ").append(rhs_enc);
            this.asm_.append(", word_sz, &key->cloud)");
          } else {
            throw new Exception("Encrypt and move to temporary var.");
          }
        }
      }
    } else {
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
        append_idx(id.getName());
        this.asm_.append("[").append(idx.getName()).append("] = ");
        this.asm_.append(rhs.getName());
        break;
      case "EncInt[]":
        if (rhs_type.equals("EncInt")) {
          append_idx("copy(");
          this.asm_.append(id.getName()).append("[").append(idx.getName());
          this.asm_.append("], ").append(rhs.getName());
          this.asm_.append(", word_sz, &key->cloud)");
          break;
        } else if (rhs_type.equals("int")) {
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] = e_cloud(");
          this.asm_.append(rhs.getName()).append(", word_sz, &key->cloud)");
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
        String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
        append_idx("vector<uint32_t> " + tmp_vec + " = { static_cast<uint32_t>(");
        this.asm_.append(exp.getName()).append(")");
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", static_cast<uint32_t>(");
            this.asm_.append((n.f4.nodes.get(i).accept(this)).getName()).append(")");
          }
        }
        this.asm_.append(" };\n");
        append_idx(id.getName() + " = e_client(");
        this.asm_.append(tmp_vec).append(", word_sz, key);\n");
        break;
      case "EncInt[]":
        String exp_var;
        if (exp_type.equals("int")) {
          exp_var = new_ctxt_tmp();
          append_idx(exp_var + " = e_client(");
          this.asm_.append(exp.getName()).append(", word_sz, key);\n");
        } else { // exp type is EncInt
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            String v_type = st_.findType(new Var_t(null, init));
            if (v_type.equals("int") || isNumeric(init)) {
              String tmp_ = new_ctxt_tmp();
              append_idx(tmp_ + " = e_client(");
              this.asm_.append(init).append(", word_sz, key);\n");
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
    if (!id_type.equals("EncInt[]"))
      throw new RuntimeException("BatchArrayAssignmentStatement");
    String index_type = st_.findType(index);
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    append_idx("vector<uint32_t> " + tmp_vec + " = { static_cast<uint32_t>(");
    this.asm_.append(exp.getName()).append(")");
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); i++) {
        this.asm_.append(", static_cast<uint32_t>(");
        this.asm_.append((n.f7.nodes.get(i).accept(this)).getName()).append(")");
      }
    }
    this.asm_.append(" };\n");
    append_idx(id.getName());
    this.asm_.append("[").append(index.getName()).append("] = ");
    this.asm_.append("e_client(");
    this.asm_.append(tmp_vec).append(", word_sz, key);\n");
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
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    switch (expr_type) {
      case "int":
        append_idx("cout << ");
        this.asm_.append(expr.getName());
        this.asm_.append(" << endl");
        break;
      case "EncInt":
        append_idx("vector<uint32_t> ");
        this.asm_.append(tmp_vec).append(" = d_client(word_sz, ");
        this.asm_.append(expr.getName()).append(", key);\n");
        append_idx("for (auto v : ");
        this.asm_.append(tmp_vec).append(") {\n");
        append_idx("  cout << \"dec(" + expr.getName() + ") = \" << ");
        if (this.print_bin_) {
          this.asm_.append("bitset<").append(this.word_sz_).append(">(v) << \" \";\n");
        } else {
          this.asm_.append("v << \" \";\n");
        }
        append_idx("}\n");
        append_idx("cout << endl");
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
    if (!expr_type.equals("EncInt"))
      throw new RuntimeException("PrintBatchedStatement: expression type");
    Var_t size = n.f4.accept(this);
    String size_type = size.getType();
    if (size_type == null) size_type = st_.findType(size);
    if (!size_type.equals("int"))
      throw new RuntimeException("PrintBatchedStatement: size type");
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    append_idx("vector<uint32_t> ");
    this.asm_.append(tmp_vec).append(" = d_client(word_sz, ");
    this.asm_.append(expr.getName()).append(", key);\n");
    append_idx("  cout << \"dec(" + expr.getName() + ") = \";\n");
    append_idx("for (auto v : ");
    this.asm_.append(tmp_vec).append(") {\n");
    append_idx("  cout << ");
    if (this.print_bin_) {
      this.asm_.append("bitset<").append(this.word_sz_).append(">(v) << \" \";\n");
    } else {
      this.asm_.append("v << \" \";\n");
    }
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
    if (!expr_type.equals("EncInt"))
      throw new RuntimeException("ReduceNoiseStatement");
    // Skip, TFHE bootstraps by default
    return null;
  }

  /**
   * f0 -> <ROTATE_LEFT>
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ","
   * f4 -> Expression()
   * f5 -> ")"
   */
  public Var_t visit(RotateLeftStatement n) throws Exception {
    String ctxt = n.f2.accept(this).getName();
    String amnt = n.f4.accept(this).getName();
    append_idx("rotate(" + ctxt + ".begin(), " + ctxt);
    this.asm_.append(".begin() + ").append(amnt).append(", ");
    this.asm_.append(ctxt).append(".end());\n");
    return null;
  }

  /**
   * f0 -> <ROTATE_RIGHT>
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ","
   * f4 -> Expression()
   * f5 -> ")"
   */
  public Var_t visit(RotateRightStatement n) throws Exception {
    String ctxt = n.f2.accept(this).getName();
    String amnt = n.f4.accept(this).getName();
    append_idx("rotate(" + ctxt + ".rbegin(), " + ctxt);
    this.asm_.append(".rbegin() + ").append(amnt).append(", ");
    this.asm_.append(ctxt).append(".rend());\n");
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
    String lhs_enc = null, rhs_enc = null;
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
      } else {
        throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
      }
    } else if (lhs_type.equals("int") && rhs_type.equals("EncInt")) {
      lhs_enc = new_ctxt_tmp();
      append_idx(lhs_enc);
      this.asm_.append(" = e_cloud(").append(lhs.getName());
      this.asm_.append(", word_sz, &key->cloud);\n");
      rhs_enc = rhs.getName();
      if (op.equals("<<") || op.equals("<<") || op.equals(">>>")) {
        throw new Exception("Shift over encrypted integers is not possible");
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
      lhs_enc = lhs.getName();
      if (!(op.equals("<<") || op.equals("<<") || op.equals(">>>"))) {
        rhs_enc = new_ctxt_tmp();
        append_idx(rhs_enc);
        this.asm_.append(" = e_cloud(").append(rhs.getName());
        this.asm_.append(", word_sz, &key->cloud);\n");
      }
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
      lhs_enc = lhs.getName();
      rhs_enc = rhs.getName();
      if (op.equals("<<") || op.equals("<<") || op.equals(">>>")) {
        throw new Exception("Shift over encrypted integers is not possible");
      }
    } else {
      throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
    }
    String res_ = new_ctxt_tmp();
    append_idx(res_);
    this.asm_.append(" = e_cloud(0, word_sz, &key->cloud);\n");
    switch (op) {
      case "+":
        append_idx("add(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "*":
        append_idx("mult(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "-":
        append_idx("sub(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "^":
        append_idx("e_xor(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "&":
        append_idx("e_and(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "|":
        append_idx("e_or(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "==":
        append_idx("eq(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "!=":
        append_idx("neq(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "<":
        append_idx("lt(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "<=":
        append_idx("leq(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case "<<":
        append_idx("shift_left_bin(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs.getName());
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case ">>":
        append_idx("shift_right_bin(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs.getName());
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      case ">>>":
        append_idx("shift_right_logical_bin(" + res_ + ", ");
        this.asm_.append(lhs_enc).append(", ").append(rhs.getName());
        this.asm_.append(", word_sz, &key->cloud);\n");
        break;
      default:
        throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
    }
    return new Var_t("EncInt", res_);
  }

  /**
   * f0 -> "~"
   * f1 -> PrimaryExpression()
   */
  public Var_t visit(BinNotExpression n) throws Exception {
    Var_t exp = n.f1.accept(this);
    String exp_type = st_.findType(exp);
    if (exp_type.equals("int")) {
        return new Var_t("int", "~" + exp.getName());
    } else if (exp_type.equals("EncInt")) {
      String res_ = new_ctxt_tmp();
      append_idx(res_);
      this.asm_.append(" = e_cloud(0, word_sz, &key->cloud);\n");
      append_idx("e_not(" + res_ + ", ");
      this.asm_.append(exp.getName()).append(", word_sz, &key->cloud);\n");
      return new Var_t("EncInt", res_);
    }
    throw new Exception("Wrong type for ~: " + exp_type);
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
    Var_t e1 = n.f4.accept(this);
    Var_t e2 = n.f6.accept(this);
    String cond_t = st_.findType(cond);
    String e1_t = st_.findType(e1);
    String e2_t = st_.findType(e2);
    String res_;
    if (cond_t.equals("bool") || cond_t.equals("int") || cond_t.equals("double")) {
      if (e1_t.equals(e2_t)) {
        res_ = "tmp_" + (++tmp_cnt_);
        append_idx(this.st_.backend_types.get(e1_t) + " " + res_ + " = (");
        this.asm_.append(cond.getName()).append(")").append(" ? ").append(e1.getName());
        this.asm_.append(" : ").append(e2.getName()).append(";\n");
        return new Var_t(e1_t, res_);
      } else if ((e1_t.equals("EncInt") || e1_t.equals("EncDouble")) &&
                  (e2_t.equals("int") || e2_t.equals("double")) ) {
        res_ = new_ctxt_tmp();
        String e2_enc = new_ctxt_tmp();
        append_idx(e2_enc + " = e_cloud(" + e2.getName());
        this.asm_.append(", word_sz, &key->cloud);\n");
        append_idx(res_ + " = (" + cond.getName() + ") ? " + e1.getName());
        this.asm_.append(" : ").append(e2_enc).append(";\n");
        return new Var_t(e1_t, res_);
      } else if ((e2_t.equals("EncInt") || e2_t.equals("EncDouble")) &&
                  (e1_t.equals("int") || e1_t.equals("double")) ) {
        res_ = new_ctxt_tmp();
        String e1_enc = new_ctxt_tmp();
        append_idx(e1_enc + " = e_cloud(" + e1.getName());
        this.asm_.append(", word_sz, &key->cloud);\n");
        append_idx(res_ + " = (" + cond.getName() + ") ? " + e1_enc);
        this.asm_.append(" : ").append(e2.getName()).append(";\n");
        return new Var_t(e2_t, res_);
      }
    } else if (cond_t.equals("EncInt") || cond_t.equals("EncDouble")) {
      res_ = new_ctxt_tmp();
      if (e1_t.equals(e2_t)) {
        if (e1_t.equals("int") || e1_t.equals("double")) {
          String e1_enc = new_ctxt_tmp(), e2_enc = new_ctxt_tmp();
          append_idx(e1_enc + " = e_cloud(" + e1.getName());
          this.asm_.append(", word_sz, &key->cloud);\n");
          append_idx(e2_enc + " = e_cloud(" + e2.getName());
          this.asm_.append(", word_sz, &key->cloud);\n");
          append_idx(res_ + " = e_cloud(0, word_sz, &key->cloud);\n");
          append_idx("e_mux(" + res_ + ", " + cond.getName() + ", ");
          this.asm_.append(e1_enc).append(", ").append(e2_enc);
          this.asm_.append(", word_sz, &key->cloud);\n");
          return new Var_t("EncInt", res_);
        } else if (e1_t.equals("EncInt") || e1_t.equals("EncDouble")) {
          append_idx(res_ + " = e_cloud(0, word_sz, &key->cloud);\n");
          append_idx("e_mux(" + res_ + ", " + cond.getName() + ", ");
          this.asm_.append(e1.getName()).append(", ").append(e2.getName());
          this.asm_.append(", word_sz, &key->cloud);\n");
          return new Var_t(e1_t, res_);
        }
      } else if ((e1_t.equals("EncInt") || e1_t.equals("EncDouble")) &&
                  (e2_t.equals("int") || e2_t.equals("double")) ) {
        append_idx(res_ + " = e_cloud(0, word_sz, &key->cloud);\n");
        String e2_enc = new_ctxt_tmp();
        append_idx(e2_enc + " = e_cloud(" + e2.getName());
        this.asm_.append(", word_sz, &key->cloud);\n");
        append_idx("e_mux(" + res_ + ", " + cond.getName() + ", ");
        this.asm_.append(e1.getName()).append(", ").append(e2_enc);
        this.asm_.append(", word_sz, &key->cloud);\n");
        return new Var_t(e1_t, res_);
      } else if ((e2_t.equals("EncInt") || e2_t.equals("EncDouble")) &&
                  (e1_t.equals("int") || e1_t.equals("double")) ) {
        append_idx(res_ + " = e_cloud(0, word_sz, &key->cloud);\n");
        String e1_enc = new_ctxt_tmp();
        append_idx(e1_enc + " = e_cloud(" + e1.getName());
        this.asm_.append(", word_sz, &key->cloud);\n");
        append_idx("e_mux(" + res_ + ", " + cond.getName() + ", ");
        this.asm_.append(e1_enc).append(", ").append(e2.getName());
        this.asm_.append(", word_sz, &key->cloud);\n");
        return new Var_t(e2_t, res_);
      }
    }
    throw new RuntimeException("Ternary condition error: " +
                                cond.getName() + " type: " + cond_t +
                                e1.getName() + " type: " + e1_t +
                                e2.getName() + " type: " + e2_t);
  }

}
