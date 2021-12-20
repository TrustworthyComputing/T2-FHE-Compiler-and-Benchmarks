package org.twc.terminator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {

  public List<Var_t> vars;
  public Map<String, String> backend_types;
  public Map<String, Boolean> enc_var_type;

  public SymbolTable() {
    this.vars = new ArrayList<>();
    this.backend_types = new HashMap<>();
    this.enc_var_type = new HashMap<>();
    this.enc_var_type.put("EncInt", false);
    this.enc_var_type.put("EncDouble", false);
  }

  public String findType(Var_t var) {
    if (var.getType() != null) return var.getType();
    Var_t v = getVar(var.getName());
    if (v == null) return "";
    return v.getType();
  }

  public Var_t getVar(String varname) {
    for (Var_t var : vars) {
      if (var.getName().equals(varname)) {
        return var;
      }
    }
    return null;
  }

  public boolean addVar(Var_t var) {
    if (getVar(var.getName()) != null) {
      return true;
    }
    vars.add(var);
    return false;
  }

  public void printMethod() {
    System.out.println("Symbol Table:");
    for (Var_t v : vars) {
      System.out.print("\t");
      v.printVar();
      System.out.println();
    }
  }

}
