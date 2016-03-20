package net.tidej.expressionparser.demo.derive.tree;

import java.util.Set;

public abstract class Node implements Comparable<Node> {
  public abstract Node derive(String to, Set<String> explanation);

  public Node simplify(Set<String> explanation) {
    return this;
  }

  public abstract int getPrecedence();

  @Override
  public int compareTo(Node another) {
    return toString().compareTo(another.toString());
  }

  public void embrace(StringBuilder sb, boolean verbose, int callerPrecedence) {
    if (callerPrecedence >= getPrecedence() || verbose) {
      sb.append('(');
      toString(sb, verbose);
      sb.append(')');
    } else {
      toString(sb, verbose);
    }
  }

  public boolean equals(Object o) {
    return (o instanceof Node) && ((Node) o).toString(true).equals(this.toString(true));
  }

  public String toString() {
    return toString(false);
  }

  public String toString(boolean verbose) {
    StringBuilder sb = new StringBuilder();
    toString(sb, verbose);
    return sb.toString();
  }

  public abstract void toString(StringBuilder sb, boolean verbose);
}
