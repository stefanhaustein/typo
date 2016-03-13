package net.tidej.expressionparser.demo.derive.tree;

class PrefixOperation extends Node {
  final String name;
  final Node param;

  PrefixOperation(String name, Node param) {
    this.name = name;
    this.param = param;
  }

  public Node simplify() {
    Node param = this.param.simplify();
    if (name.equals("+")) {
      return param;
    }
    if (name.equals("-") && param instanceof Constant) {
      return new Constant(-((Constant) param).value);
    }
    return new PrefixOperation(name, param);
  }

  public Node derive(String to) {
    return new PrefixOperation(name, param.derive(to));
  }

  @Override
  public String toString() {
    return name + param;
  }
}
