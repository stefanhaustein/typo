package org.kobjects.expressionparser.demo.basic;

import org.kobjects.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Full implementation of <a href="http://goo.gl/kIIPc0">ECMA-55</a> minimal interpreter with
 * some common additions.
 * <p>
 * Example for mixing the expresion parser with "outer" parsing.
 */
public class Basic {

  static final String INVISIBLE_STRING = new String();

  public static void main(String[] args) throws IOException {
    new Basic().runShell();
  }

  static String toString(double d) {
    if (d == (int) d) {
      return String.valueOf((int) d);
    }
    return String.valueOf(d);
  }

  static String toString(Object o) {
    return o instanceof Number ? toString(((Number) o).doubleValue()) : String.valueOf(o);
  }

  static String quote(String s) {
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"':
        case '\'':
        case '\\':
          sb.append('\\').append(c);
          break;
        case '\n':
          sb.append("\\n");
          break;
        default:
          if (c >= ' ') {
            sb.append(c);
          }
      }
    }
    sb.append('"');
    return sb.toString();
  }

  TreeMap<String, Object> variables = new TreeMap<>();
  TreeMap[] arrays = {
      new TreeMap(), new TreeMap(), new TreeMap(), new TreeMap(), new TreeMap(), new TreeMap()
  };
  TreeMap<Integer, List<Statement>> program = new TreeMap<>();
  ExpressionParser<Node> expressionParser = new ExpressionBuilder().parser;
  Exception lastException;
  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  ArrayList<StackEntry> stack = new ArrayList<>();
  TreeMap<String, double[]> forMap = new TreeMap<>();
  TreeMap<String, DefFn> functionDefinitions = new TreeMap<>();

  int currentLine;
  int currentIndex;
  int screenX;
  int nextSubIndex;  // index within next when skipping a for loop; reset in next
  int[] dataPosition = new int[3];
  Statement dataStatement;
  int[] stopped;
  ArrayList<String> symbols = new ArrayList<>();  // Used by the parser
  boolean trace;

  void clear() {
    variables.clear();
    for (TreeMap t : arrays) {
      t.clear();
    }
    variables.put("pi", Math.PI);
    variables.put("tau", 2 * Math.PI);
    Arrays.fill(dataPosition, 0);
    dataStatement = null;
    nextSubIndex = 0;
    forMap.clear();
    stack.clear();
    stopped = null;
    functionDefinitions.clear();
  }

  Statement parseStatement(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.currentValue;
    if (tryConsume(tokenizer, "GO")) {
      name += tokenizer.currentValue;
    } else if (name.equals("?")) {
      name = "PRINT";
    }
    Statement.Type type = null;
    for (Statement.Type t : Statement.Type.values()) {
      if (name.equalsIgnoreCase(t.name())) {
        type = t;
        break;
      }
    }
    if (type == null) {
      type = Statement.Type.LET;
    } else {
      tokenizer.nextToken();
    }
    switch (type) {
      case RUN:  // 0 or 1 param; Default is 0
      case RESTORE:
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            tokenizer.currentValue != ":") {
          return new Statement(this, type, expressionParser.parse(tokenizer));
        }
        return new Statement(this, type);

      case DEF:
      case GOTO:  // Exactly one param
      case GOSUB:
      case LOAD:
        return new Statement(this, type, expressionParser.parse(tokenizer));

      case NEXT:   // Zero of more
        ArrayList<Node> vars = new ArrayList<>();
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(":")) {
          do {
            vars.add(expressionParser.parse(tokenizer));
          } while (tokenizer.tryConsume(","));
        }
        return new Statement(this, type, vars.toArray(new Node[vars.size()]));

      case DATA:  // One or more params
      case DIM:
      case READ: {
        ArrayList<Node> expressions = new ArrayList<>();
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new Statement(this, type, expressions.toArray(new Node[expressions.size()]));
      }

      case FOR: {
        Node assignment = expressionParser.parse(tokenizer);
        if (!(assignment instanceof Operator) || !(assignment.children[0] instanceof Variable)
            || assignment.children[0].children.length != 0
            || !((Operator) assignment).name.equals("=")) {
          throw new RuntimeException("Variable assignment expected after FOR");
        }
        require(tokenizer, "TO");
        Node end = expressionParser.parse(tokenizer);
        if (tryConsume(tokenizer, "STEP")) {
          return new Statement(this, type, new String[]{" = ", " TO ", " STEP "},
              assignment.children[0], assignment.children[1], end,
              expressionParser.parse(tokenizer));
        }
        return new Statement(this, type, new String[]{" = ", " TO "},
            assignment.children[0], assignment.children[1], end);
      }

      case IF:
        Node condition = expressionParser.parse(tokenizer);
        if (!tokenizer.currentValue.equalsIgnoreCase("THEN") &&
            !tokenizer.currentValue.equalsIgnoreCase("GOTO")) {
          throw tokenizer.exception("'THEN expected after IF-condition.'", null);
        }
        tokenizer.nextToken();
        if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
          double target = Double.parseDouble(tokenizer.currentValue);
          tokenizer.nextToken();
          return new Statement(this, type, new String[]{" THEN "}, condition, new Literal(target));
        }
        return new Statement(this, type, new String[]{" THEN"}, condition);

      case INPUT:
      case PRINT:
        List<Node> args = new ArrayList<Node>();
        List<String> delimiter = new ArrayList<String>();
        while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF
            && !tokenizer.currentValue.equals(":")) {
          if (tokenizer.currentValue.equals(",") || tokenizer.currentValue.equals(";")) {
            delimiter.add(tokenizer.currentValue + " ");
            tokenizer.nextToken();
            if (delimiter.size() > args.size()) {
              args.add(new Literal(INVISIBLE_STRING));
            }
          } else {
            args.add(expressionParser.parse(tokenizer));
          }
        }
        return new Statement(this, type, delimiter.toArray(new String[delimiter.size()]),
            args.toArray(new Node[args.size()]));

      case LET: {
        Node assignment = expressionParser.parse(tokenizer);
        if (!(assignment instanceof Operator) || !(assignment.children[0] instanceof Variable)
            || !((Operator) assignment).name.equals("=")) {
          throw tokenizer.exception("Unrecognized statement or illegal assignment " + assignment, null);
        }
        return new Statement(this, type, new String[]{" = "}, assignment.children);
      }
      case ON: {
        List<Node> expressions = new ArrayList<Node>();
        expressions.add(expressionParser.parse(tokenizer));
        String[] kind = new String[1];
        if (tryConsume(tokenizer, "GOTO")) {
          kind[0] = " GOTO ";
        } else if (tryConsume(tokenizer, "GOSUB")) {
          kind[0] = " GOSUB ";
        } else {
          throw tokenizer.exception("GOTO or GOSUB expected.", null);
        }
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new Statement(this, type, kind, expressions.toArray(new Node[expressions.size()]));
      }
      case REM: {
        StringBuilder sb = new StringBuilder();
        while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
          sb.append(tokenizer.leadingWhitespace).append(tokenizer.currentValue);
          tokenizer.nextToken();
        }
        if (sb.length() > 0 && sb.charAt(0) == ' ') {
          sb.deleteCharAt(0);
        }
        return new Statement(this, type, new Variable(Basic.this, sb.toString()));
      }
      default:
        return new Statement(this, type);
    }
  }

  List<Statement> parseStatementList(ExpressionParser.Tokenizer tokenizer) {
    ArrayList<Statement> result = new ArrayList<>();
    Statement statement;
    do {
      while (tokenizer.tryConsume(":")) {
        result.add(new Statement(this, null));
      }
      if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
        break;
      }
      statement = parseStatement(tokenizer);
      result.add(statement);
    } while (statement.type == Statement.Type.IF ? statement.children.length == 1
        : tokenizer.tryConsume(":"));
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
      throw tokenizer.exception("Leftover input.", null);
    }
    return result;
  }

  void require(ExpressionParser.Tokenizer tokenizer, String s) {
    if (!tryConsume(tokenizer, s)) {
      throw tokenizer.exception("Expected: '" + s + "'.", null);
    }
  }

  void runProgram() {
    Map.Entry<Integer, List<Statement>> entry;
    while (null != (entry = program.ceilingEntry(currentLine))) {
      currentLine = entry.getKey();
      runStatements(entry.getValue());
    }
  }

  void runStatements(List<Statement> statements) {
    int line = currentLine;
    while (currentIndex < statements.size()) {
      int index = currentIndex;
      statements.get(index).eval();
      if (currentLine != line) {
        return;  // Goto or similar out of the current line
      }
      if (currentIndex == index) {
        currentIndex++;
      }
    }
    currentIndex = 0;
    currentLine++;
  }

  public void runShell() throws IOException {
    clear();
    symbols.add(":");
    symbols.add(";");
    symbols.add("?");
    for (String s : expressionParser.getSymbols()) {
      symbols.add(s);
    }

    System.out.println("  **** EXPRESSION PARSER BASIC DEMO V1 ****\n");
    System.out.println("  " + (Runtime.getRuntime().totalMemory() / 1024) + "K SYSTEM  "
        + Runtime.getRuntime().freeMemory() + " BASIC BYTES FREE\n\nREADY.");

    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }

      try {
        processInputLine(line);
      } catch (ExpressionParser.ParsingException e) {
        char[] fill = new char[e.position];
        Arrays.fill(fill, ' ');
        System.out.println(new String(fill) + '^');
        System.out.println("?SYNTAX ERROR: " + e.getMessage());
        System.out.println("\nREADY.");
        lastException = e;
      } catch (Exception e) {
        System.out.println("\nERROR in " + currentLine + ':' + currentIndex + ": " + e.getMessage());
        System.out.println("\nREADY.");
        lastException = e;
      }
    }
  }

  void processInputLine(String line) {
    ExpressionParser.Tokenizer tokenizer =
        new ExpressionParser.Tokenizer(new Scanner(line), symbols);
    tokenizer.nextToken();
    switch (tokenizer.currentType) {
      case EOF:
        return;
      case NUMBER:
        int lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
        tokenizer.nextToken();
        if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
          program.remove(lineNumber);
        } else {
          program.put(lineNumber, parseStatementList(tokenizer));
        }
        return;
      default:
        List<Statement> statements = parseStatementList(tokenizer);
        currentLine = -2;
        currentIndex = 0;
        runStatements(statements);
        if (currentLine != -1) {
          runProgram();
        }
    }
  }

  boolean tryConsume(ExpressionParser.Tokenizer tokenizer, String s) {
    if (!tokenizer.currentValue.equalsIgnoreCase(s)) {
      return false;
    }
    tokenizer.nextToken();
    return true;
  }

  String tab(int pos) {
    pos = Math.max(0, pos - 1);
    char[] fill;
    if (pos < screenX) {
      fill = new char[pos + 1];
      Arrays.fill(fill, ' ');
      fill[0] = '\n';
    } else {
      fill = new char[pos - screenX];
      Arrays.fill(fill, ' ');
    }
    return new String(fill);
  }

  void print(String s) {
    int cut = s.lastIndexOf('\n');
    System.out.print(s);
    if (cut == -1) {
      screenX += s.length();
    } else {
      screenX = s.length() - cut - 1;
    }
  }

  static class StackEntry {
    int lineNumber;
    int statementIndex;
    Variable forVariable;
    double step;
    double end;
  }


  /**
   * This class configures and manages the parser and is able to turn the parser callbacks
   * into an expression node tree.
   */
  class ExpressionBuilder extends ExpressionParser.Processor<Node> {
    ExpressionParser<Node> parser = new ExpressionParser<Node>(this);
    {
      parser.addCallBrackets("(", ",", ")");
      parser.addGroupBrackets("(", null, ")");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 7, "^");
      parser.addOperators(ExpressionParser.OperatorType.PREFIX, 6, "-");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 5, "*", "/");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 4, "+", "-");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 3, ">=", "<=", "<>", ">", "<", "=");
      parser.addOperators(ExpressionParser.OperatorType.PREFIX, 2, "not", "NOT");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 1, "and", "AND");
      parser.addOperators(ExpressionParser.OperatorType.INFIX, 0, "or", "OR");
    }

    @Override
    public Node call(String name, String bracket, List<Node> arguments) {
      Node[] children = arguments.toArray(new Node[arguments.size()]);
      for (Builtin.Type builtinId: Builtin.Type.values()) {
        if (name.equalsIgnoreCase(builtinId.name())) {
          String signature = builtinId.signature;
          if (arguments.size() > signature.length() ||
              arguments.size() < builtinId.minParams ) {
            throw new IllegalArgumentException("Parameter count mismatch.");
          }
          for (int i = 0; i < arguments.size(); i++) {
            if (signature.charAt(i) != arguments.get(i).returnType().getSimpleName().charAt(0)) {
              throw new RuntimeException("Parameter number " + i + " type mismatch.");
            }
          }
          return new Builtin(Basic.this, builtinId, children);
        }
      }
      name = name.toLowerCase();
      if (name.startsWith("fn") && name.length() > 2) {
        return new FnCall(Basic.this, name, children);
      }
      for (int i = 0; i < arguments.size(); i++) {
        if (arguments.get(i).returnType() != Double.class) {
          throw new IllegalArgumentException("Numeric array index expected.");
        }
      }
      return new Variable(Basic.this, name, children);
    }

    @Override
    public Node prefixOperator(String name, Node param) {
      if (param.returnType() != Double.class) {
        throw new IllegalArgumentException("Numeric argument expected for '" + name + "'.");
      }
      if (name.equalsIgnoreCase("NOT")) {
        return new Builtin(Basic.this, Builtin.Type.NOT, param);
      }
      if (name.equals("-")) {
        return new Builtin(Basic.this, Builtin.Type.NEG, param);
      }
      if (name.equals("+")) {
        return param;
      }
      return super.prefixOperator(name, param);
    }

    @Override
    public Node infixOperator(String name, Node left, Node right) {
      if ("+<=<>=".indexOf(name) == -1 && (left.returnType() != Double.class ||
          right.returnType() != Double.class)) {
        throw new IllegalArgumentException("Numeric arguments expected for '" + name + "'.");
      }
      return new Operator(name.toLowerCase(), left, right);
    }

    @Override
    public Node group(String bracket, List<Node> args) {
      return new Builtin(Basic.this, null, args.get(0));
    }

    @Override public Node identifier(String name) {
      if (name.equalsIgnoreCase(Builtin.Type.RND.name())) {
        return new Builtin(Basic.this, Builtin.Type.RND);
      }
      name = name.toLowerCase();
      if (name.startsWith("fn") && name.length() > 2) {
        return new FnCall(Basic.this, name);
      }
      return new Variable(Basic.this, name);
    }

    @Override public Node numberLiteral(String value) {
      return new Literal(Double.parseDouble(value));
    }

    @Override
    public Node stringLiteral(String value) {
      return new Literal(ExpressionParser.unquote(value));
    }
  }
}
