package net.tidej.expressionparser.demo.derive.tree;

import java.util.Set;

public class Variable extends Node {
  private final String name;

  Variable(String name) {
    this.name = name;
  }

  @Override
  public Node derive(String to, Set<String> explanations) {
    return new Constant(to.equals(name) ? 1 : 0);
  }

  @Override
  public int getPrecedence() {
    return 10;
  }

  @Override
  public void toString(StringBuilder sb, boolean verbose) {
    sb.append(name);
  }
}
