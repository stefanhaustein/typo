package net.tidej.expressionparser.demo.derive.tree;

import net.tidej.expressionparser.ExpressionParser;

import java.util.List;

public class TreeBuilder extends ExpressionParser.Processor<Node> {

  @Override
  public Node infixOperator(String name, Node left, Node right) {
    switch (name.charAt(0)) {
      case '+': return NodeFactory.add(left, right);
      case '-': return NodeFactory.sub(left, right);
      case '/': return NodeFactory.div(left, right);
      case '*': return NodeFactory.mul(left, right);
      case '^': return NodeFactory.pow(left, right);
    }
    throw new UnsupportedOperationException("Unsupported infix operator: " + name);
  }

  public Node implicitOperator(boolean strong, Node left, Node right) {
    return infixOperator("*", left, right);
  }

  @Override
  public Node prefixOperator(String name, Node argument) {
    if (name.equals("-")) {
      return NodeFactory.neg(argument);
    }
    if (name.equals("+")) {
      return argument;
    }
    throw new UnsupportedOperationException("Unsupported prefixOperator operator: " + name);
  }

  @Override
  public Node numberLiteral(String value) {
    return new Constant(Double.parseDouble(value));
  }

  @Override
  public Node identifier(String name) {
    return new Variable(name);
  }

  @Override
  public Node group(String paren, List<Node> elements) {
    return elements.get(0);
  }

  @Override
  public Node call(String identifier, String bracket, List<Node> arguments) {
    if (arguments.size() != 1) {
      throw new IllegalArgumentException(identifier + ": " + arguments);
    }
    return new Function(identifier, arguments.get(0));
  }

  @Override
  public Node apply(Node base, String bracket, List<Node> arguments) {
    throw new UnsupportedOperationException();
  }

  public static ExpressionParser<Node> createParser() {
    ExpressionParser<Node> parser = new ExpressionParser<Node>(new TreeBuilder());
    parser.addCallBrackets("(", null, ")");
    parser.addGroupBrackets(5, "(", null, ")");
    parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, 4, "^");
    parser.addOperators(ExpressionParser.OperatorType.PREFIX, 3, "+", "-");
    parser.setImplicitOperatorPrecedence(true, 2);
    parser.addOperators(ExpressionParser.OperatorType.INFIX, 1, "*", "/");
    parser.addOperators(ExpressionParser.OperatorType.INFIX, 0, "+", "-");
    return parser;
  }
}
