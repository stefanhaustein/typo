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
      return new Product(param.derive(to), new Reciprocal(param));
    }
    if (name.equals("exp")) {
      return new Product(new Function("exp", param), param.derive(to));
    }
    throw new RuntimeException("Don't know how to derive '" + name + "'");
  }

  public String toString() {
    return name + param.toString(getPrecedence());
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Node getChild(int index) {
    return param;
  }

  @Override
  public int getPrecedence() {
    return 10;
  }
}
