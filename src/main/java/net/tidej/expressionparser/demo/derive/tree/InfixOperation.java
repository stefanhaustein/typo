package net.tidej.expressionparser.demo.derive.tree;

public class InfixOperation extends Node {
  final String name;
  final Node left;
  final Node right;

  public InfixOperation(String name, Node left, Node right) {
    this.name = name;
    this.left = left;
    this.right = right;
  }

  @Override
  public Node derive(String to) {
    switch (name.charAt(0)) {
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
  public Node simplify() {
    Node left = this.left.simplify();
    Node right = this.right.simplify();
    double leftValue = (left instanceof Constant) ? ((Constant) left).value : Double.NaN;
    double rightValue = (right instanceof Constant) ? ((Constant) right).value : Double.NaN;
    boolean allConstant = !Double.isNaN(leftValue) && !Double.isNaN(rightValue);
    switch (name.charAt(0)) {
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
