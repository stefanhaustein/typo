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

  public String toString() {
    return String.valueOf(value);
  }
}
