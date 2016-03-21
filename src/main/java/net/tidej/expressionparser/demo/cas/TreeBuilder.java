package net.tidej.expressionparser.demo.cas;

import net.tidej.expressionparser.ExpressionParser;
import net.tidej.expressionparser.demo.cas.tree.Node;
import net.tidej.expressionparser.demo.cas.tree.NodeFactory;
import net.tidej.expressionparser.demo.cas.tree.UnaryFunction;
import net.tidej.expressionparser.demo.cas.tree.Variable;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TreeBuilder extends ExpressionParser.Processor<Node> {

  @Override
  public Node infixOperator(String name, Node left, Node right) {
    switch (name.charAt(0)) {
      case '+': return NodeFactory.add(left, right);
      case '-':
      case '−': return NodeFactory.sub(left, right);
      case '/':
      case ':':
      case '÷': return NodeFactory.div(left, right);
      case '*':
      case '×':
      case '⋅': return NodeFactory.mul(left, right);
      case '^': return NodeFactory.pow(left, right);
    }
    throw new UnsupportedOperationException("Unsupported infix operator: " + name);
  }

  public Node implicitOperator(boolean strong, Node left, Node right) {
    return infixOperator("⋅", left, right);
  }

  @Override
  public Node prefixOperator(String name, Node argument) {
    if (name.equals("-") || name.equals("−")) {
      return NodeFactory.neg(argument);
    }
    if (name.equals("+")) {
      return argument;
    }
    return NodeFactory.f(name, argument);
  }

  @Override
  public Node numberLiteral(String value) {
    return NodeFactory.c(Double.parseDouble(value));
  }

  @Override
  public Node identifier(String name) {
    return NodeFactory.var(name);
  }

  @Override
  public Node group(String paren, List<Node> elements) {
    return elements.get(0);
  }

  @Override
  public Node call(String identifier, String bracket, List<Node> arguments) {
    if (identifier.equals("cas")) {
      if (arguments.size() != 2) {
        throw new IllegalArgumentException("Two parameters expected for cas.");
      }
      if (!(arguments.get(1) instanceof Variable)) {
        throw new IllegalArgumentException("Second cas parameter must be a variable.");
      }
      return NodeFactory.derive(arguments.get(0), arguments.get(1).toString());
    }
    return super.call(identifier, bracket, arguments);
  }

  @Override
  public Node apply(Node base, String bracket, List<Node> arguments) {
    throw new UnsupportedOperationException();
  }

  public static ExpressionParser<Node> createParser() {
    ExpressionParser<Node> parser = new ExpressionParser<Node>(new TreeBuilder()) {
      @Override
      public Node parse(String expression) {
        Tokenizer tokenizer = new ExpressionTokenizer(new Scanner(expression), this.getSymbols());
        tokenizer.nextToken();
        Node result = parse(tokenizer);
        if (tokenizer.currentType != Tokenizer.TokenType.EOF) {
          throw tokenizer.exception("EOF expected.", null);
        }
        return result;
      }
    };
    parser.addCallBrackets("(", ",", ")");
    parser.addGroupBrackets(6, "(", null, ")");
    parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, Node.PRECEDENCE_POWER, "^");
    parser.addOperators(ExpressionParser.OperatorType.PREFIX, Node.PRECEDENCE_SIGNUM, "+", "-", "−");
    parser.setImplicitOperatorPrecedence(true, Node.PRECEDENCE_IMPLICIT_MULTIPLICATION);
    for (String name : UnaryFunction.DEFINITIONS.keySet()) {
      parser.addOperators(
          ExpressionParser.OperatorType.PREFIX, Node.PRECEDENCE_UNARY_FUNCTION, name);
    }
    parser.addOperators(ExpressionParser.OperatorType.INFIX, Node.PRECEDENCE_MULTIPLICATIVE,
        "*", "×", "⋅", "/", ":", "÷");
    parser.addOperators(ExpressionParser.OperatorType.INFIX, Node.PRECEDENCE_ADDITIVE,
        "+", "-", "−");
    return parser;
  }

  static class ExpressionTokenizer extends ExpressionParser.Tokenizer {
    static final Pattern EXPONENT_PATTERN = Pattern.compile("\\G\\s*[⁰¹²³⁴⁵⁶⁷⁸⁹]+");

    String pendingExponent;

    public ExpressionTokenizer(Scanner scanner, Iterable<String> symbols) {
      super(scanner, symbols);
    }

    @Override
    public void nextToken() {
      if (pendingExponent != null) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pendingExponent.length(); i++) {
          sb.append("⁰¹²³⁴⁵⁶⁷⁸⁹".indexOf(pendingExponent.charAt(i)));
        }
        leadingWhitespace = false;
        currentType = TokenType.NUMBER;
        currentValue = sb.toString();
        pendingExponent = null;
        return;
      }
      pendingExponent = scanner.findWithinHorizon(EXPONENT_PATTERN, 0);
      if (pendingExponent != null) {
        leadingWhitespace = pendingExponent.charAt(0) <= ' ';
        if (leadingWhitespace) {
          pendingExponent = pendingExponent.trim();
        }
        currentType = TokenType.SYMBOL;
        currentValue = "^";
        return;
      }
      super.nextToken();
    }
  }

}
