package org.kobjects.expressionparser.demo.basic;

import org.kobjects.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

// Missing compared to minimal basic
// http://www.ecma-international.org/publications/files/ECMA-ST-WITHDRAWN/ECMA-55,%201st%20Edition,%20January%201978.pdf:
//
// - Arrays
// - DEF FN
// - FOR ... NEXT
// - READ / DATA / RESTORE
// - ON ... GOSUB
// - GOSUB / RETURN
// - END
// - STOP
//
// TODO (Simplify):
//
// - Use an enum for builtins, too?
// - Use evalDouble(x), evalString(n) (evalInt(), evalBool)
//     case SIN: return Math.sin(evalDouble(0));
// - Add children to variables for index access / def fn evaluation


public class Basic {
  public static void main(String[] args) throws IOException {
    new Basic().runShell();
  }

  enum StatementType {
    CLEAR, CONTINUE, DATA, DIM, DEF, DUMP, END, FOR, GOTO, GOSUB, IF, INPUT, LET, LIST, NEXT,
    ON, PRINT, READ, REM, RETURN, RUN, STOP}

  TreeMap<String, Object> variables = new TreeMap<>();
  TreeMap<Integer, List<Statement>> program = new TreeMap<>();
  ExpressionParser<Node> expressionParser = new ExpressionBuilder().parser;
  Exception lastException;
  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  Map<String, Method> builtins = new LinkedHashMap<>();

  Basic() {
    registerBuiltins(Math.class, "atan", "atn", "floor", "int", "signum", "sgn", "sqrt", "sqr");
  }

  void clear() {
    variables.clear();
    variables.put("pi", Math.PI);
    variables.put("tau", 2 * Math.PI);
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
    for (StatementType st : StatementType.values()) {
      if (name.equalsIgnoreCase(st.name())) {
        type = st;
        break;
      }
    }
    if (type == null) {
      type = StatementType.LET;
    } else {
      tokenizer.nextToken();
      name = tokenizer.currentValue;
    }
    switch (type) {
      case GOTO:
      case GOSUB:
        return new Statement(type, expressionParser.parse(tokenizer));

      case IF:
        Node condition = expressionParser.parse(tokenizer);
        if (!tokenizer.currentValue.equalsIgnoreCase("then")) {
          throw tokenizer.exception("'then expected after if-condition.'", null);
        }
        tokenizer.currentValue = ":";  // Hack
        return new Statement(type, condition);

      case LET:
        tokenizer.nextToken();
        if (!tokenizer.tryConsume("=")) {
          throw tokenizer.exception("Unrecognized statement: " + name, null);
        }
        return new Statement(type, " = ", "", new VariableNode(name), expressionParser.parse(tokenizer));

      case INPUT:
      case PRINT:
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
        return new Statement(type, "; ", suffix, args.toArray(new Node[args.size()]));

      case REM: {
        StringBuilder sb = new StringBuilder();
        while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
          sb.append(tokenizer.leadingWhitespace).append(tokenizer.currentValue);
          tokenizer.nextToken();
        }
        if (sb.length() > 0 && sb.charAt(0) == ' ') {
          sb.deleteCharAt(0);
        }
        return new Statement(type, new VariableNode(sb.toString()));
      }
      default:
        return new Statement(type);
    }
  }

  // Should this just be a statement or Node, too? With ':' as child separator and no name?
  List<Statement> parseStatementList(ExpressionParser.Tokenizer tokenizer) {
    ArrayList<Statement> result = new ArrayList<>();
    do {
      while (tokenizer.tryConsume(":")) {
        result.add(new Statement(null));
      }
      if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
        break;
      }
      result.add(parseStatement(tokenizer));
    } while (tokenizer.tryConsume(":"));
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
      throw tokenizer.exception("Leftover input.", null);
    }
    return result;
  }

  void registerBuiltins(Class clazz, String... rename) {
    methods:
    for (Method method : clazz.getMethods()) {
      if (!Modifier.isStatic(method.getModifiers())
          || !Modifier.isPublic(method.getModifiers())) {
        continue;
      }
      String name = method.getName();
      if (method.getReturnType() == String.class) {
        name += '$';
      } else if (method.getReturnType() != Double.TYPE) {
        continue;
      }
      for (Class<?> parameterType : method.getParameterTypes()) {
        if (parameterType != Double.TYPE && parameterType != String.class) {
          continue methods;
        }
      }
      for (int i = 0; i < rename.length; i+= 2) {
        if (name.equals(rename[i])) {
          name = rename[i + 1];
          break;
        }
      }
      if (name != null) {
        builtins.put(name, method);
      }
    }
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
            program.put(lineNumber, parseStatementList(tokenizer));
          }
          continue;
        } else {
          List<Statement> statements = parseStatementList(tokenizer);
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
    StatementType type;
    String separator;
    String suffix;

    Statement(StatementType type, String separator, String suffix, Node... children) {
      this.type = type;
      this.separator = separator;
      this.suffix = suffix;
      this.children = children;
    }

    Statement(StatementType type, Node... children) {
      this(type, "", "", children);
    }

    /**
     * Here, the return value is used to indicate where to go.
     */
    public Object eval() {
      if (type == null) {
        return null;
      }
      switch (type) {
        case CLEAR:
          clear();
          break;

        case DUMP:
          if (lastException != null) {
            lastException.printStackTrace();
            lastException = null;
          } else {
            System.out.println("\n" + variables);
          }
          break;

        case GOTO:
          return children[0].eval();

        case IF:
          return children[0].eval().equals(0.0) ? Boolean.FALSE : null;

        case LET: {
          Object value = children[1].eval();
          String varName = children[0].toString();
          if (varName.endsWith("$")) {
            value = String.valueOf(value);
          } else if (!(value instanceof Double)) {
            throw new RuntimeException("Trying to assign string '" + value + "' to a number variable.");
          }
          variables.put(varName, value);
          break;
        }
        case LIST:
          System.out.println();
          for (Map.Entry<Integer,List<Statement>> entry : program.entrySet()) {
            System.out.print(entry.getKey());
            List<Statement> line = entry.getValue();
            StatementType previous = null;
            for (int i = 0; i < line.size(); i++) {
              System.out.print(i == 0 ? " "
                  : previous == StatementType.IF ? " THEN "
                  : previous == null ? ": " : " : ");
              System.out.print(line.get(i));
              previous = line.get(i).type;
            }
            System.out.println();
          }
          break;

        case INPUT:
        case PRINT:
          for (Node child : children) {
            if (type == StatementType.INPUT && child instanceof VariableNode) {
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
          break;

        case RUN:
          clear();
          return 0;

        default:
         throw new RuntimeException("Unimplemented statement: " + type);
      }
      return null;
    }

    @Override
    public String toString() {
      if (type == null) {
        return "";
      }
      StringBuilder sb = new StringBuilder(type.name());
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
        throw new RuntimeException("Undefined variable: '" + name + "'");
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
      Method method = builtins.get(name.toLowerCase());
      if (method == null) {
        throw new IllegalArgumentException("Function '" + name + "' not found.");
      }
      if (method.getParameterTypes().length != arguments.size()) {
        throw new IllegalArgumentException("Expected: " + method.getParameterTypes().length
            + " parameters; provided: " + arguments.size());
      }
      return new FunctionNode(method, arguments.toArray(new Node[arguments.size()]));
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
