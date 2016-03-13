package net.tidej.expressionparser.demo.derive.tree;

public class Function extends Node {
  final String name;
  final Node param;

  public Function(String name, Node param) {
    this.name = name;
    this.param = param;
  }

  public Node simplify() {
    Node param = this.param.simplify();
    boolean isConst = param instanceof Constant;
    double paramVal = isConst ? ((Constant) param).value : Double.NaN;
    if (name.equals("log")) {
      if (isConst) {
        return new Constant(Math.log(paramVal));
      }
    }
    if (name.equals("exp")) {
      if (isConst) {
        return new Constant(Math.exp(paramVal));
      }
    }
    return new Function(name, param);
  }

  public Node derive(String to) {
    if (name.equals("log")) {
      return new InfixOperation("/", param.derive(to), param);
    }
    if (name.equals("exp")) {
      return new InfixOperation("*", new Function("exp", param), param.derive(to));
    }
    throw new RuntimeException("Don't know how to derive '" + name + "'");
  }

  public String toString() {
    return name + "(" + param + ")";
  }
}
