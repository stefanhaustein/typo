package net.tidej.expressionparser.demo.derive.tree;

import net.tidej.expressionparser.demo.derive.string2d.String2d;

import java.util.Set;

public abstract class Node implements Comparable<Node> {
  public enum Stringify {FLAT, VERBOSE, BLOCK}

  public abstract Node derive(String to, Set<String> explanation);

  public Node simplify(Set<String> explanation) {
    return this;
  }

  public abstract int getPrecedence();

  @Override
  public int compareTo(Node another) {
    return toString().compareTo(another.toString());
  }

  public boolean equals(Object o) {
    return (o instanceof Node) && ((Node) o).toString2d(Stringify.VERBOSE).toString().equals(
        this.toString2d(Stringify.VERBOSE).toString());
  }

  public String toString() {
    return toString2d(Stringify.FLAT).toString();
  }

  public String2d toString2d(Stringify type) {
    return String2d.valueOf(toString());
  }

  public String2d embrace2d(Stringify type, int callerPrecedence) {
    if (callerPrecedence < getPrecedence()) {
      return toString2d(type);
    }
    return String2d.embrace('(', toString2d(type), ')');
  }
}
