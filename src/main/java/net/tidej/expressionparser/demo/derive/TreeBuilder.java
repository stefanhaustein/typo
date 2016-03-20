package net.tidej.expressionparser.demo.derive;

import net.tidej.expressionparser.ExpressionParser;
import net.tidej.expressionparser.demo.derive.tree.Constant;
import net.tidej.expressionparser.demo.derive.tree.Node;
import net.tidej.expressionparser.demo.derive.tree.NodeFactory;
import net.tidej.expressionparser.demo.derive.tree.Variable;

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
    throw new UnsupportedOperationException("Unsupported prefixOperator operator: " + name);
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
    return NodeFactory.f(identifier, arguments.toArray(new Node[arguments.size()]));
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
    parser.addGroupBrackets(5, "(", null, ")");
    parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, 4, "^");
    parser.addOperators(ExpressionParser.OperatorType.PREFIX, 3, "+", "-", "−");
    parser.setImplicitOperatorPrecedence(true, 2);
    parser.addOperators(ExpressionParser.OperatorType.INFIX, 1, "*", "×", "⋅", "/", ":", "÷");
    parser.addOperators(ExpressionParser.OperatorType.INFIX, 0, "+", "-", "−");
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
