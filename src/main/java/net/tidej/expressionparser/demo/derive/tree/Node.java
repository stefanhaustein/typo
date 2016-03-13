package net.tidej.expressionparser.demo.derive.tree;

public abstract class Node {
  public abstract Node derive(String to);

  public Node simplify() {
    return this;
  }
}
