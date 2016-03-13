package net.tidej.expressionparser.demo.derive.tree;

import net.tidej.expressionparser.ExpressionParser;

import java.util.List;

public class TreeBuilder implements ExpressionParser.Processor<Node> {

  @Override
  public Node infix(String name, Node left, Node right) {
    return new InfixOperation(name, left, right);
  }

  @Override
  public Node suffix(String name, Node argument) {
    throw new UnsupportedOperationException(name);
  }

  @Override
  public Node prefix(String name, Node argument) {
    return new PrefixOperation(name, argument);
  }

  @Override
  public Node number(String value) {
    return new Constant(Double.parseDouble(value));
  }

  @Override
  public Node string(char quote, String value) {
    throw new UnsupportedOperationException();
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
