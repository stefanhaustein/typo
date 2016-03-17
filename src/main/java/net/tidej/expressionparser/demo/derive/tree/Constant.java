package net.tidej.expressionparser.demo.derive.tree;

public class Constant extends Node {
  final double value;

  Constant(double value) {
    this.value = value;
  }

  @Override
  public Node derive(String to) {
    return new Constant(0);
  }

  @Override
  public int getPrecedence() {
    return 10;
  }

  public String toString() {
    String s = String.valueOf(value);
    return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
  }


}
