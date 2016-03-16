package net.tidej.expressionparser.demo.derive.tree;

import net.tidej.expressionparser.ExpressionParser;

import java.util.List;

public class TreeBuilder extends ExpressionParser.Processor<Node> {

  @Override
  public Node infix(String name, Node left, Node right) {
    switch (name.charAt(0)) {
      case '+': return new Sum(left, right);
      case '-': return new Sum(left, new Negation(right));
      case '/': return new Product(left, new Reciprocal(right));
      case '*': return new Product(left, right);
      case '^': return new Power(left, right);
    }
    throw new UnsupportedOperationException("Unsupported infix operator: " + name);
  }

  public Node implicit(Node left, Node right) {
    return infix("*", left, right);
  }

  @Override
  public Node prefix(String name, Node argument) {
    if (name.equals("-")) {
      return new Negation(argument);
    }
    if (name.equals("+")) {
      return argument;
    }
    throw new UnsupportedOperationException("Unsupported prefixOperator operator: " + name);
  }

  @Override
  public Node number(String value) {
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
}
