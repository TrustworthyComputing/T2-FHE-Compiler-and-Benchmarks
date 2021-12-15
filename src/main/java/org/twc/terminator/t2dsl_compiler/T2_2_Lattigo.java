package org.twc.terminator.t2dsl_compiler;

import org.twc.terminator.SymbolTable;
import org.twc.terminator.Var_t;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.*;

import java.util.ArrayList;
import java.util.List;

public class T2_2_Lattigo extends T2_Compiler {

  public T2_2_Lattigo(SymbolTable st) {
    super(st);
    this.st_.backend_types.put("int", "uint64");
    this.st_.backend_types.put("int[]", "[]uint64");
    this.st_.backend_types.put("EncInt", "*bfv.Ciphertext");
    this.st_.backend_types.put("EncInt[]", "[]*bfv.Ciphertext");
  }

  protected void append_keygen() {
    append_idx("// BFV parameters (128 bit security) with plaintext modulus 65929217\n");
    append_idx("paramDef := bfv.PN13QP218\n");
    append_idx("paramDef.T = 0x3ee0001\n");
    append_idx("params, err := bfv.NewParametersFromLiteral(paramDef)\n");
    append_idx("slots := 2048\n");
    append_idx("if err != nil {\n");
    append_idx("  panic(err)\n");
    append_idx("}\n");
    append_idx("encoder := bfv.NewEncoder(params)\n");
    append_idx("kgen := bfv.NewKeyGenerator(params)\n");
    append_idx("clientSk, _ := kgen.GenKeyPair()\n");
    append_idx("decryptor := bfv.NewDecryptor(params, clientSk)\n");
//    append_idx("encryptorPk := bfv.NewEncryptor(params, clientPk)\n");
    append_idx("encryptorSk := bfv.NewEncryptor(params, clientSk)\n");
    append_idx("evaluator := bfv.NewEvaluator(params, rlwe.EvaluationKey{})\n");
    append_idx("ptxt := bfv.NewPlaintext(params)\n");
    append_idx("tmp := make([]uint64, slots)\n");
    append_idx("encoder.EncodeUint(tmp, ptxt)\n");
    append_idx("tmp_ := encryptorSk.EncryptNew(ptxt)\n\n");
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
    append_idx("package main\n\n");
    append_idx("import (\n");
    append_idx("  \"fmt\"\n");
    append_idx("  \"math\"\n");
    append_idx("  \"math/bits\"\n\n");
    append_idx("  \"github.com/ldsec/lattigo/v2/rlwe\"\n");
    append_idx("  \"github.com/ldsec/lattigo/v2/utils\"\n");
    append_idx("  \"github.com/ldsec/lattigo/v2/bfv\"\n");
    append_idx("  \"github.com/ldsec/lattigo/v2/ring\"\n");
    append_idx(")\n\n");
    append_idx("func main() {\n");
    this.indent_ = 2;
    append_keygen();
    n.f6.accept(this);
    n.f7.accept(this);
    n.f9.accept(this);
    this.indent_ = 0;
    append_idx("}\n");
    return null;
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ( VarDeclarationRest() )*
   * f3 -> ";"
   */
  public Var_t visit(VarDeclaration n) throws Exception {
    append_idx("var ");
    String type = n.f0.accept(this).getType();
    Var_t id = n.f1.accept(this);
    this.asm_.append(id.getName());
    if (n.f2.present()) {
      for (int i = 0; i < n.f2.size(); i++) {
        n.f2.nodes.get(i).accept(this);
      }
    }
    this.asm_.append(" ").append(this.st_.backend_types.get(type));
    this.asm_.append("\n");
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
      append_idx("tmp[0] = uint64(");
      this.asm_.append(rhs_name);
      this.asm_.append(")\n");
      append_idx("encoder.EncodeUint(tmp, ptxt)\n");
      append_idx(lhs.getName());
      this.asm_.append(" = encryptorSk.EncryptNew(ptxt)\n");
    } else if (lhs_type.equals("EncInt[]") && rhs_type.equals("int[]")) {
      // if EncInt[] <- int[]
      tmp_cnt_++;
      String tmp_i = "i_" + tmp_cnt_;
      append_idx(lhs.getName());
      this.asm_.append(" = make([]*bfv.Ciphertext, len(").append(rhs_name);
      this.asm_.append("))\n");
      append_idx("for ");
      this.asm_.append(tmp_i).append(" := 0; ").append(tmp_i).append(" < len(");
      this.asm_.append(rhs_name).append("); ").append(tmp_i);
      this.asm_.append("++ {\n");
      this.indent_ += 2;
      append_idx("tmp[0] = uint64(");
      this.asm_.append(rhs_name).append("[").append(tmp_i).append("])\n");
      append_idx("encoder.EncodeUint(tmp, ptxt)\n");
      append_idx(lhs.getName());
      this.asm_.append("[").append(tmp_i).append("] = encryptorSk.EncryptNew(ptxt)\n");
      this.indent_ -= 2;
      append_idx("}\n");
    } else if (lhs_type.equals(rhs_type)) {
      // if the destination has the same type as the source.
      append_idx(lhs.getName());
      if (rhs_name.startsWith("resize(")) {
        this.asm_.append(" = make([]");
        this.asm_.append(rhs_type.substring(0, rhs_type.length()-2)).append(", ");
        this.asm_.append( rhs_name.substring(7)).append("\n");
      } else {
        this.asm_.append(" = ");
        this.asm_.append(rhs_name).append("\n");
      }
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
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      String tmp_i = "i_" + tmp_cnt_;
      append_idx(tmp_vec + " := make([]uint64, slots)\n");
      append_idx("for " + tmp_i + " := range " + tmp_vec + " {\n");
      this.indent_ += 2;
      append_idx(tmp_vec + "[" + tmp_i + "] = uint64(1)\n");
      this.indent_ -= 2;
      append_idx("}\n");
      append_idx("encoder.EncodeUint(" + tmp_vec + ", ptxt)\n");
      append_idx("tmp_ = encryptorSk.EncryptNew(ptxt)\n");
      append_idx(id.getName() + " = evaluator.AddNew(");
      this.asm_.append(id.getName()).append(", tmp_)\n");
    } else {
      append_idx(id.getName());
      this.asm_.append("++\n");
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
      tmp_cnt_++;
      String tmp_vec = "tmp_vec_" + tmp_cnt_;
      String tmp_i = "i_" + tmp_cnt_;
      append_idx(tmp_vec + " := make([]uint64, slots)\n");
      append_idx("for " + tmp_i + " := range " + tmp_vec + " {\n");
      this.indent_ += 2;
      append_idx(tmp_vec + "[" + tmp_i + "] = uint64(1)\n");
      this.indent_ -= 2;
      append_idx("}\n");
      append_idx("encoder.EncodeUint(" + tmp_vec + ", ptxt)\n");
      append_idx("tmp_ = encryptorSk.EncryptNew(ptxt)\n");
      append_idx(id.getName() + " = evaluator.SubNew(");
      this.asm_.append(id.getName()).append(", tmp_)\n");
    } else {
      append_idx(id.getName());
      this.asm_.append("++\n");
    }
    return null;
  }

  /**
   * f0 -> Identifier()
   * f1 -> CompoundOperator()
   * f2 -> Expression()
   */
  public Var_t visit(CompoundAssignmentStatement n) throws Exception {
    // TODO
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
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
    }
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
    // TODO

    Var_t id = n.f0.accept(this);
    String id_type = st_.findType(id);
    Var_t idx = n.f2.accept(this);
    String idx_type = st_.findType(idx);
    String op = n.f4.accept(this).getName();
    Var_t rhs = n.f5.accept(this);
    String rhs_type = st_.findType(rhs);
    switch (id_type) {
      case "int[]":
        append_idx(id.getName());
        this.asm_.append("[").append(idx.getName()).append("] ").append(op);
        this.asm_.append(" ").append(rhs.getName());
        break;
      case "EncInt[]":
      default:
        throw new Exception("error in array assignment");
    }
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
        this.asm_.append("[").append(idx.getName()).append("] = uint64(");
        this.asm_.append(rhs.getName()).append(")\n");
        break;
      case "EncInt[]":
        if (rhs_type.equals("EncInt")) {
          append_idx(id.getName());
          this.asm_.append("[").append(idx.getName()).append("] = ");
          this.asm_.append(rhs.getName()).append("\n");
          break;
        } else if (rhs_type.equals("int")) {
          append_idx("tmp[0] = uint64(");
          this.asm_.append(rhs.getName());
          this.asm_.append(")\n");
          append_idx("encoder.EncodeUint(tmp, ptxt)\n");
          append_idx(id.getName() + "[" + idx.getName()+ "] = ");
          this.asm_.append("encryptorSk.EncryptNew(ptxt)\n");
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
        append_idx(id.getName() + " = []uint64{ " + exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" }\n");
        break;
      case "EncInt":
        tmp_cnt_++;
        String tmp_vec = "tmp_vec_" + tmp_cnt_;
        append_idx(tmp_vec + " := []uint64{ " + exp.getName());
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            this.asm_.append(", ").append((n.f4.nodes.get(i).accept(this)).getName());
          }
        }
        this.asm_.append(" }\n");
        append_idx("encoder.EncodeUint(" + tmp_vec + ", ptxt)\n");
        append_idx(id.getName() + " = encryptorSk.EncryptNew(ptxt)\n");
        break;
      case "EncInt[]":
        tmp_cnt_++;
        String exp_var = "tmp_" + tmp_cnt_ + "_";
        if (exp_type.equals("int")) {
          append_idx("tmp[0] = uint64(" + exp.getName() + ")\n");
          append_idx("encoder.EncodeUint(tmp, ptxt)\n");
          append_idx(exp_var + " := encryptorSk.EncryptNew(tmp)\n");
        } else { // exp type is EncInt
          exp_var = exp.getName();
        }
        List<String> inits = new ArrayList<>();
        if (n.f4.present()) {
          for (int i = 0; i < n.f4.size(); i++) {
            String init = (n.f4.nodes.get(i).accept(this)).getName();
            if (exp_type.equals("int")) {
              tmp_cnt_++;
              String tmp_ = "tmp_" + tmp_cnt_ + "_";
              append_idx("tmp[0] = uint64(" + init + ")\n");
              append_idx("encoder.EncodeUint(tmp, ptxt)\n");
              append_idx(tmp_ + " := encryptorSk.EncryptNew(tmp)\n");
              inits.add(tmp_);
            } else { // exp type is EncInt
              inits.add(init);
            }
          }
        }
        append_idx(id.getName());
        this.asm_.append(" = []*bfv.Ciphertext{ ").append(exp_var);
        for (String init : inits) {
          this.asm_.append(", ").append(init);
        }
        this.asm_.append(" }\n");
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
    append_idx(tmp_vec + " := []uint64{ " + exp.getName());
    if (n.f7.present()) {
      for (int i = 0; i < n.f7.size(); i++) {
        this.asm_.append(", ").append((n.f7.nodes.get(i).accept(this)).getName());
      }
    }
    this.asm_.append(" }\n");
    append_idx("encoder.EncodeUint(" + tmp_vec + ", ptxt)\n");
    append_idx(id.getName() + "[" + index.getName() + "] = encryptorSk.EncryptNew(ptxt)\n");
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
        append_idx("fmt.Println(" + expr.getName() + ")\n");
        break;
      case "EncInt":
        append_idx("ptxt = decryptor.DecryptNew(" + expr.getName() + ")\n");
        append_idx("fmt.Println(encoder.DecodeUintNew(ptxt))\n");
        break;
      default:
        throw new Exception("Bad type for print statement");
    }
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
    assert(expr_type.equals("EncInt"));
    Var_t size = n.f4.accept(this);
    String size_type = st_.findType(expr);
    assert(size_type.equals("int"));
    append_idx("ptxt = decryptor.DecryptNew(" + expr.getName() + ")\n");
    String tmp_vec = "tmp_vec_" + (++tmp_cnt_);
    String tmp_i = "i_" + tmp_cnt_;
    append_idx(tmp_vec + " := encoder.DecodeUintNew(ptxt))\n");
    append_idx("for " + tmp_i + " := 0; " + tmp_i + " < " + size.getName());
    this.asm_.append("; ").append(tmp_i).append("++ {\n");
    this.indent_ += 2;
    append_idx("fmt.Println(" + tmp_vec + "[" + tmp_i + "])\n");
    this.indent_ -= 2;
    append_idx("}\n");
    return null;
  }

  /**
   * f0 -> <REDUCE_NOISE>
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   */
  public Var_t visit(ReduceNoiseStatement n) throws Exception {
    return null;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> BinOperator()
   * f2 -> PrimaryExpression()
   */
  public Var_t visit(BinaryExpression n) throws Exception {
    // TODO

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
    } else if (lhs_type.equals("int") && rhs_type.equals("EncInt")) {
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("int")) {
    } else if (lhs_type.equals("EncInt") && rhs_type.equals("EncInt")) {
    }
    throw new Exception("Bad operand types: " + lhs_type + " " + op + " " + rhs_type);
  }

}
