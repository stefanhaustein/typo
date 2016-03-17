package net.tidej.expressionparser.demo.derive.tree;

public abstract class Node {
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

  public String toString(int callerPrecedence) {
    return callerPrecedence >= getPrecedence() ? "(" + toString() + ")" : toString();
  }
}
