package org.kobjects.expressionparser.demo.basic;

import org.kobjects.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Basic {
  public static void main(String[] args) throws IOException {
    new Basic().runShell();
  }

  TreeMap<String, Object> variables = new TreeMap<>();
  TreeMap<Integer, List<Statement>> program = new TreeMap<>();
  ExpressionParser<Node> expressionParser = new ExpressionBuilder().parser;
  Exception lastException;
  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

  void clear() {
    variables.clear();
    variables.put("pi", Math.PI);
    variables.put("tau", 2 * Math.PI);
  }

  List<Statement> parseStatements(ExpressionParser.Tokenizer tokenizer) {
    ArrayList<Statement> result = new ArrayList<>();
    do {
      while (tokenizer.tryConsume(":")) {
        result.add(new Statement(""));
      }
      String name = tokenizer.currentValue.toLowerCase();
      tokenizer.nextToken();
      if (name.equals("goto")) {
        result.add(new Statement("goto", expressionParser.parse(tokenizer)));
      } else if (name.equals("clear") || name.equals("dump") || name.equals("list")
          || name.equals("run")) {
        result.add(new Statement(name));
      } else if (name.equals("if")) {
        Node condition = expressionParser.parse(tokenizer);
        if (!tokenizer.currentValue.equalsIgnoreCase("then")) {
          throw tokenizer.exception("'then expected after if-condition.'", null);
        }
        result.add(new Statement("if", condition));
        tokenizer.currentValue = ":";  // Hack
      } else if (name.equals("print") || name.equals("?") || name.equals("input")) {
        List<Node> args = new ArrayList<Node>();
        String suffix = "";
        while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF
            && !tokenizer.currentValue.equals(":")) {
          suffix = "";
          args.add(expressionParser.parse(tokenizer));
          while (tokenizer.tryConsume(",") || tokenizer.tryConsume(";")) {
            suffix = ";";
          }
        }
        result.add(new Statement(name.equals("?") ? "print" : name, "; ", suffix,
            args.toArray(new Node[args.size()])));
      } else if (name.equals("rem")) {
        StringBuilder sb = new StringBuilder();
        while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
          sb.append(tokenizer.leadingWhitespace).append(tokenizer.currentValue);
          tokenizer.nextToken();
        }
        if (sb.length() > 0 && sb.charAt(0) == ' ') {
          sb.deleteCharAt(0);
        }
        result.add(new Statement("rem", new VariableNode(sb.toString())));
      } else {
        if (name.equals("let")) {
          tokenizer.nextToken();
          name = tokenizer.currentValue;
        }
        if (!tokenizer.tryConsume("=")) {
          throw tokenizer.exception("Unrecognized statement: " + name, null);
        }
        result.add(new Statement("let", " = ", "", new VariableNode(name), expressionParser.parse(tokenizer)));
      }
    } while (tokenizer.tryConsume(":"));
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
      throw tokenizer.exception("Leftover input.", null);
    }
    return result;
  }

  void runProgram(int currentLine) {
    while (true) {
      Integer key = program.ceilingKey(currentLine);
      if (key == null) {
        return;
      }
      List<Statement> line = program.get(key);
      currentLine = key + 1;
      Object go = runStatements(line);
      if (go instanceof Number) {
        currentLine = ((Number) go).intValue();
      }
    }
  }

  Object runStatements(List<Statement> statements) {
    for (Statement statement: statements) {
      Object o = statement.eval();
      if (o != null) {
        return o;
      }
    }
    return null;
  }

  public void runShell() throws IOException {
    clear();
    ArrayList<String> symbols = new ArrayList<>();
    symbols.add(":");
    symbols.add(";");
    symbols.add("?");
    for (String s: expressionParser.getSymbols()) {
      symbols.add(s);
    }

    System.out.println("  **** EXPRESSION PARSER BASIC DEMO V1 ****\n");
    System.out.println("  " + (Runtime.getRuntime().totalMemory() / 1024) + "K SYSTEM  "
        + Runtime.getRuntime().freeMemory() + " BASIC BYTES FREE\n\nREADY.");

    while(true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }

      try {
        ExpressionParser.Tokenizer tokenizer =
            new ExpressionParser.Tokenizer(new Scanner(line), symbols);
        tokenizer.nextToken();
        if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
          continue;
        } else if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
          int lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
          tokenizer.nextToken();
          if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
            program.remove(lineNumber);
          } else {
            program.put(lineNumber, parseStatements(tokenizer));
          }
          continue;
        } else {
          List<Statement> statements = parseStatements(tokenizer);
          Object go = runStatements(statements);
          if (go instanceof Number) {
            runProgram(((Number) go).intValue());
          }
        }
      } catch (ExpressionParser.ParsingException e) {
        System.out.print("?SYNTAX ERROR: " + e.getMessage());
        lastException = e;
      } catch (Exception e) {
        System.out.print("ERROR: " + e.getMessage());
        lastException = e;
      }
      System.out.println("\nREADY.");
    }
  }

  interface Node {
    Object eval();
  }

  class Statement implements Node {
    Node[] children;
    String name;
    String separator;
    String suffix;

    Statement(String name, String separator, String suffix, Node... children) {
      this.name = name;
      this.separator = separator;
      this.suffix = suffix;
      this.children = children;
    }

    Statement(String name, Node... children) {
      this(name, "", "", children);
    }

    /**
     * Here, the return value is used to indicate where to go.
     */
    public Object eval() {
      if (name.equals("clear")) {
        clear();
      } else if (name.equals("dump")) {
        if (lastException != null) {
          lastException.printStackTrace();
          lastException = null;
        } else {
          System.out.println("\n" + variables);
        }
      } else if (name.equals("goto")) {
        return children[0].eval();
      } else if (name.equals("if")) {
        if (children[0].eval().equals(0.0)) {
          return Boolean.FALSE;
        }
      } else if (name.equals("let")) {
        Object value = children[1].eval();
        String varName = children[0].toString();
        if (varName.endsWith("$")) {
          value = String.valueOf(value);
        } else if (!(value instanceof Double)) {
          throw new RuntimeException("Trying to assign string '" + value + "' to a number variable.");
        }
        variables.put(varName, value);
      } else if (name.equals("list")) {
        System.out.println();
        for (Map.Entry<Integer,List<Statement>> entry : program.entrySet()) {
          System.out.print(entry.getKey());
          List<Statement> line = entry.getValue();
          String previous = "";
          for (int i = 0; i < line.size(); i++) {
            System.out.print(i == 0 ? " "
                : previous.equals("if") ? " THEN "
                : previous.isEmpty() ? ": " : " : ");
            System.out.print(line.get(i));
            previous = line.get(i).name;
          }
          System.out.println();
        }
      } else if (name.equals("print") || name.equals("input")) {
        for (Node child : children) {
          if (name.equals("input") && child instanceof VariableNode) {
            VariableNode variable = (VariableNode) child;
            Object value;
            try {
              value = reader.readLine();
              if (!variable.name.endsWith("$")) {
                value = Double.parseDouble((String) value);
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            } catch (NumberFormatException e) {
              value = Double.NaN;
            }
            variables.put(variable.name, value);
          } else {
            System.out.print(child.eval());
          }
        }
        if (suffix.isEmpty()) {
          System.out.println();
        }
      } else if (name.equals("run")) {
        clear();
        return 0;
      } else if (!name.isEmpty()) {
        throw new RuntimeException("Unimplemented statement: " + name);
      }
      return null;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(name.toUpperCase());
      if (children.length > 0) {
        sb.append(' ');
        sb.append(children[0]);
        for (int i = 1; i < children.length; i++) {
          sb.append(separator);
          sb.append(children[i]);
        }
      }
      sb.append(suffix);
      return sb.toString();
    }
  }

  static class LiteralNode implements Node {
    Object value;

    LiteralNode(Object value) { this.value = value; }
    @Override public Object eval() { return value; }

    @Override
    public String toString() {
      if (!(value instanceof String)) {
        String s = String.valueOf(value);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
      }
      String s = (String) value;
      StringBuilder sb = new StringBuilder("\"");
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        switch (c) {
          case '"':
          case '\'':
          case '\\': sb.append('\\').append(c); break;
          case '\n': sb.append("\\n"); break;
          default:
            if (c >= ' ') {
              sb.append(c);
            }
        }
      }
      sb.append('"');
      return sb.toString();
    }
  }

  // Not static for access to the variables.
  class VariableNode implements Node {
    String name;
    VariableNode(String name) { this.name = name; }

    public Object eval() {
      Object result = variables.get(name);
      if (result == null) {
        throw new RuntimeException("Undefined variable: " + name);
      }
      return result;
    }

    public String toString() { return name; }
  }

  static class GroupNode implements Node {
    final Node child;
    GroupNode(Node child) { this.child = child; }
    public Object eval() { return child.eval(); }
    public String toString() { return "(" + child + ")"; }
  }

  static class PrefixNode implements Node {
    final String name;
    final Node param;
    PrefixNode(String name, Node param) {
      this.name = name;
      this.param = param;
    }

    @Override
    public Object eval() {
      Object pVal = param.eval();
      switch(name.charAt(0)) {
        case '-': return -((Double) pVal);
        case 'n': return Double.valueOf(~((Double) pVal).intValue());
        default: throw new RuntimeException("Usupported unary: " + name);
      }
    }

    public String toString() {
      return (name.equals("-") ? "-"  : (name + " ")) + param.toString();
    }
  }

  static class FunctionNode implements Node {
    final Method method;
    final Node[] children;

    FunctionNode(Method method, Node... children) {
      this.method = method;
      this.children = children;
    }

    @Override
    public Object eval() {
      Object[] args = new Object[children.length];
      for (int i = 0; i < args.length; i++) {
        args[i] = children[i].eval();
      }
      try {
        return method.invoke(null, args);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public String toString() {
      StringBuilder sb = new StringBuilder(method.getName());
      sb.append('(');
      for (int i = 0; i < children.length; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(children[i].toString());
      }
      sb.append(')');
      return sb.toString();
    }
  }

  static class InfixNode implements Node {
    final String name;
    final Node left;
    final Node right;

    public InfixNode(String name, Node left, Node right) {
      this.name = name;
      this.left = left;
      this.right = right;
    }

    public Object eval() {
      Object lVal = left.eval();
      Object rVal = right.eval();
      boolean numbers = (lVal instanceof Double) && (rVal instanceof Double);
      if (!numbers) {
        lVal = String.valueOf(lVal);
        rVal = String.valueOf(rVal);
      }
      if ("<=>".indexOf(name.charAt(0)) != -1) {
        int cmp = (((Comparable) lVal).compareTo(rVal));
        return (cmp == 0 ? name.contains("=") : cmp < 0 ? name.contains("<") : name.contains(">"))
            ? -1.0 : 0.0;
      }
      if (!numbers) {
        if (!name.equals("+")) {
          throw new IllegalArgumentException("Numbers arguments expected for operator " + name);
        }
        return "" + lVal + rVal;
      }
      double l = (Double) lVal;
      double r = (Double) rVal;
      switch (name.charAt(0)) {
        case 'a': return Double.valueOf(((int) l) & ((int) r));
        case 'o': return Double.valueOf(((int) l) | ((int) r));
        case '^': return Math.pow(l, r);
        case '+': return l + r;
        case '-': return l - r;
        case '/': return l / r;
        case '*': return l * r;
        default:  throw new RuntimeException("Unsupported operator " + name);
      }
    }

    public String toString() {
      return left.toString() + ' ' + name + ' ' + right.toString();
    }
  }

  class ExpressionBuilder extends ExpressionParser.Processor<Node> {
    ExpressionParser<Node> parser = new ExpressionParser<Node>(this);
    {
      parser.addCallBrackets("(", ",", ")");
      parser.addGroupBrackets("(", null, ")");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 7, "^");
      parser.addOperators(ExpressionParser.OperatorType.PREFIX, 6, "-");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 5, "*", "/");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 4, "+", "-");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 3, "=", "<=", "<", "<>", ">", ">=");
      parser.addOperators(ExpressionParser.OperatorType.PREFIX, 2, "not", "NOT");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 1, "and", "AND");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 0, "or", "OR");
    }

    @Override
    public Node call(String name, String bracket, List<Node> arguments) {
      try {
        return new FunctionNode(Math.class.getMethod(name.toLowerCase(), new Class[]{Double.TYPE}),
            arguments.toArray(new Node[arguments.size()]));
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Node prefixOperator(String name, Node param) {
      return new PrefixNode(name.toLowerCase(), param);
    }

    @Override
    public Node infixOperator(String name, Node left, Node right) {
      return new InfixNode(name.toLowerCase(), left, right);
    }

    @Override
    public Node group(String bracket, List<Node> args) {
      return new GroupNode(args.get(0));
    }

    @Override public Node identifier(String name) {
      return new VariableNode(name.toLowerCase());
    }

    @Override public Node numberLiteral(String value) {
      return new LiteralNode(Double.parseDouble(value));
    }

    @Override
    public Node stringLiteral(String value) {
      return new LiteralNode(ExpressionParser.unquote(value));
    }
  }
}
