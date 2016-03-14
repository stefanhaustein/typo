package net.tidej.expressionparser.demo.derive.tree;

public class Quotient extends Node {
  final Node dividend;
  final Node divisor;

  public Quotient(Node left, Node right) {
    this.dividend = left;
    this.divisor = right;
  }

  @Override
  public Node derive(String to) {
    Node dividend = new Sum(
        new Product(this.dividend, divisor.derive(to)),
        new Product(this.dividend.derive(to), divisor));
    Node divisor = new Power(this.divisor, new Constant(2));
    return new Quotient(dividend, divisor);
  }

  @Override
  public Node simplify() {
    Node dividend = this.dividend.simplify();
    Node divisor = this.divisor.simplify();
    double dividendValue = (dividend instanceof Constant) ? ((Constant) dividend).value : Double.NaN;
    double divisorValue = (divisor instanceof Constant) ? ((Constant) divisor).value : Double.NaN;
    if (!Double.isNaN(dividendValue) && !Double.isNaN(divisorValue)) {
      return new Constant(dividendValue / divisorValue);
    }
    if (dividendValue == 0) {
      return new Constant(0);
    }
    if (divisorValue == 1) {
      return dividend;
    }

    if (!Double.isNaN(divisorValue)) {
      return new Product(new Constant(1/divisorValue), dividend);
    }

    if (dividend.toString().equals(divisor.toString())) {
      return new Constant(1);
    }

    if (dividend instanceof Quotient) {
      // (a/b)/c -> a/(b*c)
      Quotient ab = (Quotient) dividend;
      return new Quotient(ab.dividend, new Product(ab.divisor, divisor).simplify());
    }

    if (divisor instanceof Quotient) {
      // a/(b/c) -> (a*c)/b;
      Quotient bc = (Quotient) divisor;
      return new Quotient(new Product(dividend, bc.divisor), bc.dividend);
    }

    return new Quotient(dividend, divisor);
  }

  public String toString() {
    return "(" + dividend + " / " + divisor + ")";
  }
}
