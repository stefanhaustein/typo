package net.tidej.expressionparser.demo.derive.tree;

public class Variable extends Node {
  private final String name;

  public Variable(String name) {
    this.name = name;
  }

  @Override
  public Node derive(String to) {
    return new Constant(to.equals(name) ? 1 : 0);
  }

  public String toString() {
    return name;
  }
}
