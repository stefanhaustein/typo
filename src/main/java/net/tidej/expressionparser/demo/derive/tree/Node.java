package net.tidej.expressionparser.demo.derive.tree;

public abstract class Node implements Comparable<Node> {
  public abstract Node derive(String to);

  public Node simplify() {
    return this;
  }

  public int getChildCount() {
    return 0;
  }

  public Node getChild(int index) {
    return null;
  }

  public abstract int getPrecedence();

  @Override
  public int compareTo(Node another) {
    return toString().compareTo(another.toString());
  }

  public String toString(int callerPrecedence) {
    return callerPrecedence >= getPrecedence() ? "(" + toString() + ")" : toString();
  }

  public boolean equals(Object o) {
    return (o instanceof Node) && o.toString().equals(this.toString());
  }
}
