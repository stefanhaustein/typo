package org.kobjects.typo.io;

public class CodePrinter {
  private String indent = "";
  StringBuilder sb = new StringBuilder();

  public void indent() {
    indent += "  ";
  }

  public void outdent() {
    indent = indent.substring(0, indent.length() - 2);
  }

  public CodePrinter append(char c) {
    sb.append(c);
    return this;
  }

  public CodePrinter append(Object o) {
    sb.append(String.valueOf(o));
    return this;
  }

  public void newLine() {
    sb.append("\n");
    sb.append(indent);
  }

  public static String toString(Printable p) {
    CodePrinter cp = new CodePrinter();
    p.print(cp);
    return cp.toString();
  }

  public String toString() {
    return sb.toString();
  }
}
