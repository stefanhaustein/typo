package net.tidej.expressionparser.demo.derive.tree;

class Negation extends Node {
  final Node param;

  Negation(Node param) {
    this.param = param;
  }

  public Node simplify() {
    Node param = this.param.simplify();
    if (param instanceof Constant) {
      return new Constant(-((Constant) param).value);
    }
    if (param instanceof Negation) {
      return ((Negation) param).param;
    }
    if (param instanceof Product && ((Product) param).factors[0] instanceof Constant) {
      Node[] factors = ((Product) param).factors.clone();
      factors[0] = new Constant(-((Constant) factors[0]).value);
      return new Product(factors);
    }
    return new Negation(param);
  }

  public Node derive(String to) {
    return new Negation(param.derive(to));
  }

  @Override
  public String toString() {
    return "(-" + param + ")";
  }
}
