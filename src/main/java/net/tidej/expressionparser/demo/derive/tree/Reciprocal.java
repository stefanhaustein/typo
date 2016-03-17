package net.tidej.expressionparser.demo.derive.tree;

import static net.tidej.expressionparser.demo.derive.tree.Factory.div;
import static net.tidej.expressionparser.demo.derive.tree.Factory.neg;
import static net.tidej.expressionparser.demo.derive.tree.Factory.pow;
import static net.tidej.expressionparser.demo.derive.tree.Factory.rez;

public class Reciprocal extends Node {
  final Node param;

  public Reciprocal(Node param) {
    this.param = param;
  }

  @Override
  public Node derive(String to) {
    return neg(div(param.derive(to), pow(param, new Constant(2))));
  }

  @Override
  public Node simplify() {
    Node param = this.param.simplify();
    if (param instanceof Constant) {
      return new Constant(1/((Constant) param).value);
    }
    return rez(param.simplify());
  }

  @Override
  public String toString() {
    return "1/" + param.toString(getPrecedence());
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  public Node getChild(int index) {
    return param;
  }

  @Override
  public int getPrecedence() {
    return 1;
  }
}
