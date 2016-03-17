package net.tidej.expressionparser.demo.derive.tree;

public class Power extends Node {
  final Node left;
  final Node right;

  public Power(Node left, Node right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public Node derive(String to) {
    if (right instanceof Constant) {
      double rightVal = ((Constant) right).value;
      if (left.toString().equals(to)) {
        if (rightVal == 0) {
          return new Constant(0);
        }
        return new Product(new Constant(rightVal),
            new Power(left, new Constant(rightVal - 1)));
      }
    }
    Node s1 = new Product(left.derive(to), new Product(left, new Reciprocal(right)));
    Node s2 = new Product(right.derive(to), new Function("ln", left));
    Node sum = new Sum(s1, s2);
    return new Product(this, sum);
  }

  @Override
  public Node simplify() {
    Node left = this.left.simplify();
    Node right = this.right.simplify();
    double leftValue = (left instanceof Constant) ? ((Constant) left).value : Double.NaN;
    double rightValue = (right instanceof Constant) ? ((Constant) right).value : Double.NaN;
    if (!Double.isNaN(leftValue) && !Double.isNaN(rightValue)) {
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
    return new Power(left, right);
  }

  public String toString() {
    return left.toString(getPrecedence()) + "^" + right.toString(getPrecedence());
  }

  public int getChildCount() {
    return 1;
  }

  public Node getChild(int index) {
    return index == 0 ? left : right;
  }

  @Override
  public int getPrecedence() {
    return 4;
  }
}
