package net.tidej.expressionparser.demo.derive.tree;

import java.util.Set;

public class Variable extends Node {
  private final String name;

  Variable(String name) {
    this.name = name;
  }

  @Override
  public int getPrecedence() {
    return PRECEDENCE_PRIMARY;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Node substitute(String name, Node replacement) {
    return this.name.equals(name) ? replacement : this;
  }
}
