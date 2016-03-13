package net.tidej.expressionparser.demo;

import net.tidej.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Derive {

  static abstract class Node {
    abstract Node derive(String to);
    Node simplify() {
      return this;
    }
  }

  static class PrefixOperation extends Node {
    final String name;
    final Node param;
    PrefixOperation(String name, Node param) {
      this.name = name;
      this.param = param;
    }

    public Node simplify() {
      Node param = this.param.simplify();
      if (name.equals("+")) {
        return param;
      }
      if (name.equals("-") && param instanceof Constant) {
        return new Constant(-((Constant) param).value);
      }
      return new PrefixOperation(name, param);
    }

    public Node derive(String to) {
      return new PrefixOperation(name, param.derive(to));
    }

    @Override
    public String toString() {
      return name + param;
    }
  }

  static class Function extends Node {
    final String name;
    final Node param;

    Function(String name, Node param) {
      this.name = name;
      this.param = param;
    }

    public Node simplify() {
      Node param = this.param.simplify();
      boolean isConst = param instanceof Constant;
      double paramVal = isConst ? ((Constant) param).value : Double.NaN;
      if (name.equals("log")) {
        if (isConst) {
          return new Constant(Math.log(paramVal));
        }
      }
      if (name.equals("exp")) {
        if (isConst) {
          return new Constant(Math.exp(paramVal));
        }
      }
      return new Function(name, param);
    }

    public Node derive(String to) {
      if (name.equals("log")) {
        return new InfixOperation("/", param.derive(to), param);
      }
      if (name.equals("exp")) {
        return new InfixOperation("*", new Function("exp", param), param.derive(to));
      }
      throw new RuntimeException();
    }

    public String toString() {
      return name + "(" + param + ")";
    }
  }

  static class InfixOperation extends Node {
    final String name;
    final Node left;
    final Node right;

    InfixOperation(String name, Node left, Node right) {
      this.name = name;
      this.left = left;
      this.right = right;
    }

    @Override
    Node derive(String to) {
      switch(name.charAt(0)) {
        case '+':
        case '-':
          return new InfixOperation(name, left.derive(to), right.derive(to));
        case '*':
          return new InfixOperation("+",
              new InfixOperation("*", left, right.derive(to)),
              new InfixOperation("*", left.derive(to), right));
        case '/': {
          Node dividend = new InfixOperation("+",
              new InfixOperation("*", left, right.derive(to)),
              new InfixOperation("*", left.derive(to), right));
          Node divisor = new InfixOperation("^", right, new Constant(2));
          return new InfixOperation("/", dividend, divisor);
        }
        case '^': {
          if (right instanceof Constant) {
            double rightVal = ((Constant) right).value;
            if (left.toString().equals(to)) {
              if (rightVal == 0) {
                return new Constant(0);
              }
              return new InfixOperation("*", new Constant(rightVal),
                  new InfixOperation("^", left, new Constant(rightVal - 1)));
            }
          }
          Node s1 = new InfixOperation("*", left.derive(to), new InfixOperation("/", left, right));
          Node s2 = new InfixOperation("*", right.derive(to), new Function("ln", left));
          Node sum = new InfixOperation("+", s1, s2);
          return new InfixOperation("*", this, sum);
        }
        default:
          throw new UnsupportedOperationException("Can't derive " + name);
      }
    }

    @Override
    Node simplify() {
      Node left = this.left.simplify();
      Node right = this.right.simplify();
      double leftValue = (left instanceof Constant) ? ((Constant) left).value : Double.NaN;
      double rightValue = (right instanceof Constant) ? ((Constant) right).value : Double.NaN;
      boolean allConstant = !Double.isNaN(leftValue) && !Double.isNaN(rightValue);
      switch(name.charAt(0)) {
        case '+':
          if (allConstant) {
            return new Constant(leftValue + rightValue);
          }
          if (leftValue == 0) {
            return right;
          }
          if (rightValue == 0) {
            return left;
          }
          break;
        case '-':
          if (allConstant) {
            return new Constant(leftValue - rightValue);
          }
          if (leftValue == 0) {
            return new PrefixOperation("-", right);
          }
          if (rightValue == 0) {
            return new Constant(leftValue);
          }
          break;
        case '*':
          if (allConstant) {
            return new Constant(leftValue * rightValue);
          }
          if (leftValue == 0 || rightValue == 0) {
            return new Constant(0);
          }
          if (leftValue == 1) {
            return right;
          }
          if (rightValue == 1) {
            return left;
          }
          if (!Double.isNaN(rightValue)) {
            return new InfixOperation("*", right, left);
          }
          if (left.toString().equals(right.toString())) {
            return new InfixOperation("^", left, new Constant(2));
          }
          break;
        case '/':
          if (allConstant) {
            return new Constant(leftValue / rightValue);
          }
          if (leftValue == 0) {
            return new Constant(0);
          }
          if (rightValue == 1) {
            return left;
          }
          if (left.toString().equals(right.toString())) {
            return new Constant(1);
          }
          break;
        case '^':
          if (allConstant) {
            return new Constant(Math.pow(leftValue, rightValue));
          }
          if (leftValue == 0) {
            return new Constant(0);
          }
          if (leftValue == 1) {
            return new Constant(1);
          }
          if (rightValue == 1) {
            return left;
          }
          if (rightValue == 0) {
            return new Constant(1);
          }
          break;
      }
      return new InfixOperation(name, left, right);
    }

    public String toString() {
      return "(" + left + " " + name + " " + right + ")";
    }
  }

  static class Constant extends Node {
    double value;
    Constant(double value) {
      this.value = value;
    }

    @Override
    Node derive(String to) {
      return new Constant(0);
    }

    public String toString() {
      return String.valueOf(value);
    }
  }

  static class Variable extends Node {
    String name;
    Variable(String name) {
      this.name = name;
    }

    @Override
    Node derive(String to) {
      return new Constant(to.equals(name) ? 1 : 0);
    }

    public String toString() {
      return name;
    }
  }


  static class TreeBuilder implements ExpressionParser.Processor<Node> {

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

  public static void main(String[] args) throws IOException {
    ExpressionParser<Node> parser = new ExpressionParser<Node>(new TreeBuilder());
    parser.addCallBrackets("(", null, ")");
    parser.addGroupBrackets(0, "(", null, ")");
    parser.addInfixRtlOperators(1, "^");
    parser.addPrefixOperators(2, "+", "-");
    parser.addInfixOperators(3, "*", "/");
    parser.addInfixOperators(4, "+", "-");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.print("Expression?   ");
      String input = reader.readLine();
      if (input == null || input.isEmpty()) {
        break;
      }
      try {
        Node parsed = parser.parse(input);
        System.out.println("Parsed:       " + parsed);
        Node simplified = parsed.simplify();
        System.out.println("Simplified:   " + simplified);
        Node derived = simplified.derive("x");
        System.out.println("Derived to x: " + derived);
        System.out.println("Simplified:   " + derived.simplify());
      } catch (Exception e) {
        System.out.println("Error:     " + e.getMessage());
      }
    }
  }
}
