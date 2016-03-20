package net.tidej.expressionparser.demo.derive.tree;

import java.util.Set;

public class Constant extends Node {
  public final double value;

  Constant(double value) {
    this.value = value;
  }

  public static String toString(double d) {
    String s = String.valueOf(d);
    return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
  }

  @Override
  public Node derive(String to, Set<String> explanation) {
    return new Constant(0);
  }

  @Override
  public int getPrecedence() {
    return 10;
  }

  @Override
  public void toString(StringBuilder sb, boolean readable) {
    sb.append(value);
    if (sb.length() > 2 && sb.charAt(sb.length() - 2) == '.' && sb.charAt(sb.length() - 1) == '0') {
      sb.setLength(sb.length() - 2);
    }
  }
}
