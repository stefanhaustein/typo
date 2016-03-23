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

// Missing compared to minimal basic
// http://www.ecma-international.org/publications/files/ECMA-ST-WITHDRAWN/ECMA-55,%201st%20Edition,%20January%201978.pdf:
//
// - DEF FN
// - FOR ... NEXT
// - READ / DATA / RESTORE
// - ON ... GOSUB
//


public class Basic {

  enum StatementType {
    CLEAR, CONTINUE, DATA, DIM, DEF, DUMP, END, FOR, GOTO, GOSUB, IF, INPUT, LET, LIST, LOAD,
    NEXT, ON, PRINT, READ, REM, RESTORE, RETURN, RUN, STOP}

  enum BuiltinType {
    // N: Required number, n: optional number, S: Required string, s: optional string.
    ABS("N"), COS("N"), SGN("N"), SIN("N"), NEG("N"), NOT("N"), RND("");
    String signature;
    BuiltinType(String parameters) {
      this.signature = parameters;
    }
  }

  public static void main(String[] args) throws IOException {
    new Basic().runShell();
  }

  static String quote(String s) {
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

  TreeMap<Integer,TreeMap<String, Object>> variables = new TreeMap<>();
  TreeMap<Integer, List<Statement>> program = new TreeMap<>();
  ExpressionParser<Node> expressionParser = new ExpressionBuilder().parser;
  Exception lastException;
  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  ArrayList<int[]> stack = new ArrayList<>();

  int currentLine;
  int currentIndex;
  int[] dataPosition = new int[3];
  Statement dataStatement;
  int[] stopped;

  void clear() {
    variables.clear();
    getVariables(0).put("pi", Math.PI);
    getVariables(0).put("tau", 2 * Math.PI);
    Arrays.fill(dataPosition, 0);
    stopped = null;
  }

  TreeMap<String,Object> getVariables(int dimension) {
    TreeMap<String,Object> result = variables.get(dimension);
    if (result == null) {
      result = new TreeMap<>();
      variables.put(dimension, result);
    }
    return result;
  }

  Statement parseStatement(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.currentValue;
    if (name.equalsIgnoreCase("go")) {
      tokenizer.nextToken();
      name += tokenizer.currentValue;
    } else if (name.equals("?")) {
      name = "PRINT";
    }
    StatementType type = null;
    for (StatementType t : StatementType.values()) {
      if (name.equalsIgnoreCase(t.name())) {
        type = t;
        break;
      }
    }
    if (type == null) {
      type = StatementType.LET;
    } else {
      tokenizer.nextToken();
    }
    switch (type) {
      case DATA: // Any number of params
      case DIM:
      case READ: {
        ArrayList<Node> vars = new ArrayList<>();
        do {
          vars.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new Statement(type, vars.toArray(new Node[vars.size()]));
      }
      case DEF:
      case FOR:
        throw new RuntimeException("NYI");

      case GOTO:
      case GOSUB:
        return new Statement(type, expressionParser.parse(tokenizer));

      case IF:
        Node condition = expressionParser.parse(tokenizer);
        if (!tokenizer.currentValue.equalsIgnoreCase("then") &&
            !tokenizer.currentValue.equalsIgnoreCase("goto")) {
          throw tokenizer.exception("'THEN or GOTO expected after IF-condition.'", null);
        }
        tokenizer.nextToken();
        if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
          double target = Double.parseDouble(tokenizer.currentValue);
          tokenizer.nextToken();
          return new Statement(type, new String[]{" THEN "}, condition, new Literal(target));
        }
        return new Statement(type, new String[]{" THEN"}, condition);

      case LET: {
        Node op = expressionParser.parse(tokenizer);
        if (!(op instanceof InfixOperator) || !(op.children[0] instanceof Variable)
            || !((InfixOperator) op).name.equals("=")) {
          throw tokenizer.exception("Unrecognized statement or illegal assignment " + op, null);
        }
        return new Statement(type, new String[]{" = "}, op.children);
      }
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
              args.add(new Literal(""));
            }
          } else {
            args.add(expressionParser.parse(tokenizer));
          }
        }
        return new Statement(type, delimiter.toArray(new String[delimiter.size()]),
            args.toArray(new Node[args.size()]));

      case NEXT:
      case RUN:
      case RESTORE:
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            tokenizer.currentValue != ":") {
          return new Statement(type, expressionParser.parse(tokenizer));
        }
        return new Statement(type);

      case REM: {
        StringBuilder sb = new StringBuilder();
        while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
          sb.append(tokenizer.leadingWhitespace).append(tokenizer.currentValue);
          tokenizer.nextToken();
        }
        if (sb.length() > 0 && sb.charAt(0) == ' ') {
          sb.deleteCharAt(0);
        }
        return new Statement(type, new Variable(sb.toString()));
      }
      default:
        return new Statement(type);
    }
  }

  List<Statement> parseStatementList(ExpressionParser.Tokenizer tokenizer) {
    ArrayList<Statement> result = new ArrayList<>();
    Statement statement;
    do {
      while (tokenizer.tryConsume(":")) {
        result.add(new Statement(null));
      }
      if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
        break;
      }
      statement = parseStatement(tokenizer);
      result.add(statement);
    } while (statement.type == StatementType.IF ? statement.children.length == 1
        : tokenizer.tryConsume(":"));
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
      throw tokenizer.exception("Leftover input.", null);
    }
    return result;
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
            program.put(lineNumber, parseStatementList(tokenizer));
          }
          continue;
        } else {
          List<Statement> statements = parseStatementList(tokenizer);
          currentLine = -2;
          currentIndex = 0;
          runStatements(statements);
          if (currentLine != -1) {
            runProgram();
          }
        }
      } catch (ExpressionParser.ParsingException e) {
        char[] fill = new char[e.position];
        Arrays.fill(fill, ' ');
        System.out.println(new String(fill) + '^');
        System.out.println("?SYNTAX ERROR: " + e.getMessage());
        lastException = e;
      } catch (Exception e) {
        System.out.println("\nERROR in " + currentLine + ':' + currentIndex + ": " + e.getMessage());
        lastException = e;
      }
      System.out.println("\nREADY.");
    }
  }


  abstract static class Node {
    Node[] children;

    public Node(Node... children) {
      this.children = children;
    }

    abstract Object eval();

    String evalString(int i) {
      return String.valueOf(children[i].eval());
    }

    double evalDouble(int i) {
      Object o = children[i].eval();
      if (!(o instanceof Number)) {
        throw new RuntimeException("Number expected in " + this.toString());
      }
      return ((Number) o).doubleValue();
    }

    public String toString() {
      if (children.length == 0) {
        return "";
      } else if (children.length == 1) {
        return children[0].toString();
      } else {
        StringBuilder sb = new StringBuilder(children[0].toString());
        for (int i = 1; i < children.length; i++) {
          sb.append(", ");
          sb.append(children[i]);
        }
        return sb.toString();
      }
    }
  }


  class Statement extends Node {
    StatementType type;
    String[] delimiter;

    Statement(StatementType type, String[] delimiter, Node... children) {
      super(children);
      this.type = type;
      this.delimiter = delimiter;
    }

    Statement(StatementType type, Node... children) {
      this(type, null, children);
    }

    /**
     * Here, the return value is used to indicate where to go.
     */
    public Object eval() {
      if (type == null) {
        return null;
      }
      switch (type) {
        case CONTINUE:
          if (stopped == null) {
            throw new RuntimeException("Not stopped.");
          }
          currentLine = stopped[0];
          currentIndex = stopped[1] + 1;
          break;

        case CLEAR:
          clear();
          break;

        case DATA:
        case DIM:
          // We just do dynamic expansion as needed.
          break;

        case DUMP:
          if (lastException != null) {
            lastException.printStackTrace();
            lastException = null;
          } else {
            System.out.println("\n" + variables);
          }
          break;

        case END:
          currentLine = Integer.MAX_VALUE;
          currentIndex = 0;
          break;

        case GOSUB:
          stack.add(new int[] {currentLine, currentIndex});
        case GOTO:
          currentLine = (int) evalDouble(0);
          currentIndex = 0;
          break;

        case IF:
          if (evalDouble(0) == 0.0) {
            if (children.length == 1) {
              currentLine++;
            } else {
              currentLine = (int) evalDouble(1);
            }
            currentIndex = 0;
          }
          break;

        case LET: {
          ((Variable) children[0]).set(children[1].eval());
          break;
        }
        case LIST:
          list();
          break;

        case INPUT:
        case PRINT:
          prinput();
          break;
        case READ:
          for (int i = 0; i < children.length; i++) {
            while (dataStatement == null || dataPosition[2] >= dataStatement.children.length) {
              dataPosition[2] = 0;
              if (dataStatement != null) {
                dataPosition[1]++;
              }
              dataStatement = find(StatementType.DATA, null, dataPosition);
              if (dataStatement == null) {
                throw new RuntimeException("Out of data.");
              }
            }
            ((Variable) children[i]).set(dataStatement.children[dataPosition[2]++].eval());
          }
          break;

        case RESTORE:
          dataStatement = null;
          Arrays.fill(dataPosition, 0);
          if (children.length > 0) {
            dataPosition[0] = (int) evalDouble(0);
          }
          break;

        case RETURN: {
          if (stack.isEmpty()) {
            throw new RuntimeException("RETURN without GOSUB");
          }
          int[] address = stack.remove(stack.size() - 1);
          currentLine = address[0];
          currentIndex = address[1] + 1;
          break;
        }
        case RUN:
          clear();
          currentLine = children.length == 0 ? 0 : (int) evalDouble(0);
          currentIndex = 0;
          break;

        case STOP:
          stopped = new int[] {currentLine, currentIndex};
          System.out.println("\nSTOPPED in " + currentLine + ":" + currentIndex);
          currentLine = Integer.MAX_VALUE;
          currentIndex = 0;
          break;

        default:
         throw new RuntimeException("Unimplemented statement: " + type);
      }
      return null;
    }

    Statement find(StatementType type, String name, int[] position) {
      Map.Entry<Integer,List<Statement>> entry;
      while (null != (entry = program.ceilingEntry(position[0]))) {
        position[0] = entry.getKey();
        List<Statement> list = entry.getValue();
        while (position[1] < list.size()) {
          Statement statement = list.get(position[1]);
          if (statement.type == type) {
            return statement;
          }
          position[1]++;
        }
        position[0]++;
        position[1] = 0;
      }
      return null;
    }

    void list() {
      System.out.println();
      for (Map.Entry<Integer,List<Statement>> entry : program.entrySet()) {
        System.out.print(entry.getKey());
        List<Statement> line = entry.getValue();
        for (int i = 0; i < line.size(); i++) {
          System.out.print(i == 0 || line.get(i - 1).type == StatementType.IF ? "" : " :");
          System.out.print(line.get(i));
        }
        System.out.println();
      }
    }

    void prinput() {
      for (int i = 0; i < children.length; i++) {
        Node child = children[i];
        if (type == StatementType.INPUT && child instanceof Variable) {
          Variable variable = (Variable) child;
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
          variable.set(value);
        } else {
          System.out.print(child.eval());
        }
        if (i < delimiter.length && delimiter[i].equals(", ")) {
          System.out.print("\t");
        }
      }
      if (delimiter.length < children.length) {
        System.out.println();
      }
    }

    @Override
    public String toString() {
      if (type == null) {
        return "";
      }
      StringBuilder sb = new StringBuilder(" ");
      sb.append(type.name());
      if (children.length > 0) {
        sb.append(' ');
        sb.append(children[0]);
        for (int i = 1; i < children.length; i++) {
          sb.append(delimiter == null ? ", " : delimiter[i - 1]);
          sb.append(children[i]);
        }
      }
      if (delimiter != null && delimiter.length == children.length) {
        sb.append(delimiter[children.length - 1]);
      }
      return sb.toString();
    }
  }

  static class Literal extends Node {
    Object value;

    Literal(Object value) {
      super((Node[]) null);
      this.value = value;
    }

    @Override public Object eval() { return value; }

    @Override
    public String toString() {
      if (value instanceof String) {
        return quote((String) value);
      }
      String s = String.valueOf(value);
      return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }
  }

  // Not static for access to the variables.
  class Variable extends Node {
    final String name;

    Variable(String name, Node... children) {
      super(children);
      this.name = name;
    }

    void set(Object value) {
      if (name.endsWith("$")) {
        value = String.valueOf(value);
      } else if (!(value instanceof Double)) {
        throw new RuntimeException("Cannot assign string to number variable " + name);
      }
      if (children.length == 0) {
        getVariables(0).put(name, value);
        return;
      }
      TreeMap<Integer,Object> target = (TreeMap<Integer,Object>)
          getVariables(children.length).get(name);
      if (target == null) {
        target = new TreeMap<>();
        getVariables(children.length).put(name, target);
      }
      for (int i = 0; i < children.length - 2; i++) {
        int index = (int) evalDouble(i);
        TreeMap<Integer,Object> sub = (TreeMap<Integer,Object>) target.get(index);
        if (sub == null) {
          sub = new TreeMap<>();
          target.put(index, sub);
        }
        target = sub;
      }
      target.put((int) evalDouble(children.length - 1), value);
    }

    public Object eval() {
      Object result;
      if (children.length == 0) {
        result = getVariables(0).get(name);
      } else {
        TreeMap<Integer,Object> arr =
            (TreeMap<Integer, Object>) getVariables(children.length).get(name);
        for (int i = 0; i < children.length - 2 && arr != null; i++) {
          arr = (TreeMap<Integer, Object>) arr.get((int) evalDouble(i));
        }
        result = arr == null ? null : arr.get((int) evalDouble(children.length - 1));
      }
      return result == null ? name.endsWith("$") ? "" : 0.0 : result;
    }

    public String toString() {
      return children.length == 0 ? name : name + "(" + super.toString() + ")";
    }
  }

  static class Builtin extends Node {
    final BuiltinType id;

    Builtin(BuiltinType id, Node... args) {
      super(args);
      this.id = id;
    }

    public Object eval() {
      if (id == null) {
        return children[0].eval();  // Grouping ().
      }
      switch (id) {
        case ABS: return Math.abs(evalDouble(0));
        case COS: return Math.cos(evalDouble(0));
        case NEG: return -evalDouble(0);
        case NOT: return Double.valueOf(~((int) evalDouble(0)));
        case SGN: return Math.signum(evalDouble(0));
        case SIN: return Math.sin(evalDouble(0));
        case RND: return Math.random();
        default:
          throw new IllegalArgumentException("NYI: " + id);
      }
    }

    public String toString() {
      if (id == null) {
        return children[0].toString();
      } else if (id == BuiltinType.NEG) {
        return "-" + children[0];
      } else if (id == BuiltinType.NOT) {
        return "NOT " + children[0];
      } else {
        return id.toString().toLowerCase() + "(" + super.toString() + ")";
      }
    }
  }

  static class InfixOperator extends Node {
    final String name;

    public InfixOperator(String name, Node left, Node right) {
      super(left, right);
      this.name = name;
    }

    public Object eval() {
      Object lVal = children[0].eval();
      Object rVal = children[1].eval();
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

    @Override
    public String toString() {
      return children[0].toString() + ' ' + name + ' ' + children[1].toString();
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
      Node[] children = arguments.toArray(new Node[arguments.size()]);
      for (BuiltinType builtinId: BuiltinType.values()) {
        if (name.equalsIgnoreCase(builtinId.name())) {
          if (children.length != builtinId.signature.length()) {
            throw new IllegalArgumentException("Expected " + builtinId.signature.length()
                + " arguments for " + name + "(), but got " + children.length);
          }
          return new Builtin(builtinId, children);
        }
      }
      return new Variable(name, children);
    }

    @Override
    public Node prefixOperator(String name, Node param) {
      if (name.equalsIgnoreCase("NOT")) {
        return new Builtin(BuiltinType.NOT, param);
      }
      if (name.equals("-")) {
        return new Builtin(BuiltinType.NEG, param);
      }
      if (name.equals("+")) {
        return param;
      }
      return super.prefixOperator(name, param);
    }

    @Override
    public Node infixOperator(String name, Node left, Node right) {
      return new InfixOperator(name.toLowerCase(), left, right);
    }

    @Override
    public Node group(String bracket, List<Node> args) {
      return new Builtin(null, args.get(0));
    }

    @Override public Node identifier(String name) {
      if (name.equals("RND")) {
        return new Builtin(BuiltinType.RND);
      }
      return new Variable(name.toLowerCase());
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
