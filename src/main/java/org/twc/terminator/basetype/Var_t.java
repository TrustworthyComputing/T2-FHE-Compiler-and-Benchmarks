package org.twc.terminator.basetype;

import java.util.List;

public class Var_t {

  private String type_;
  private String name_;
  private String temp_;
  private List<Var_t> vars_;

  public Var_t(String type, String name) {
    this.type_ = type;
    this.name_ = name;
    this.temp_ = null;
  }

  public Var_t(List<Var_t> vars) {
    assert (vars != null);
    vars_ = vars;
    this.type_ = vars_.get(0).type_;
    this.name_ = null;
    this.temp_ = null;
  }

  public String getName() { return this.name_; }

  public void setName(String name) { this.name_ = name; }

  public String getType() { return this.type_; }

  public void setType(String type) { this.type_ = type; }

  public String getTemp() { return this.temp_; }

  public void setTemp(String temp) { this.temp_ = temp; }

  public List<Var_t> getVarList() { return this.vars_; }

  public void printVar() {
    System.out.print(this.type_ + " " + this.getName());
  }

  public void printVarDetails() {
    System.out.println("Type: " + this.type_ + "\nName: " + this.getName() +
                       "\nTemp: " + this.temp_ + "\n");
  }

}
