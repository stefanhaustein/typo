package net.tidej.expressionparser.demo.derive.tree;

public class Reciprocal extends Node {
  final Node param;

  public Reciprocal(Node param) {
    this.param = param;
  }

  @Override
  public Node derive(String to) {
    return new Negation(new Product(
        param.derive(to),
        new Reciprocal(new Power(param, new Constant(2)))));
  }

  @Override
  public Node simplify() {
    Node param = this.param.simplify();
    if (param instanceof Constant) {
      return new Constant(1/((Constant) param).value);
    }
    return new Reciprocal(param.simplify());
  }

  @Override
  public String toString() {
    return "(1/" + param + ")";
  }
}
