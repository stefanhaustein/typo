package net.tidej.expressionparser.demo.cas.tree;

import net.tidej.expressionparser.demo.cas.string2d.String2d;

import java.util.Set;

import static net.tidej.expressionparser.demo.cas.tree.NodeFactory.*;

class Power extends Node {
  public static String2d toString2d(Stringify type, Node base, double exponent) {
    String2d base2d = base.embrace(type, PRECEDENCE_POWER);
    if (type != Stringify.VERBOSE) {
      if (exponent == 1 && type != Stringify.VERBOSE) {
        return base2d;
      }
      if (exponent > 0 && exponent < 10 && exponent == (int) exponent && base2d.height() == 1) {
        return String2d.concat(base2d, "⁰¹²³⁴⁵⁶⁷⁸⁹".charAt((int) exponent));
      }
    }
    return String2d.concat(
        base2d,
        "^",
        Constant.toString(exponent));
  }

  final Node base;
  final Node exponent;

  public Power(Node left, Node right) {
    this.base = left;
    this.exponent = right;
  }

  @Override
  public Node simplify(Set<String> explanation) {
    Node base = this.base.simplify(explanation);
    Node exponent = this.exponent.simplify(explanation);

    if (base.equals(this.base) && exponent.equals(this.exponent)) {
      if (exponent instanceof Constant) {
        // Will turn this into a product, where additional optimization may take place.
        return NodeFactory.powC(base, ((Constant) exponent).value);
      }
      if (base instanceof Constant) {
        double leftValue = ((Constant) base).value;
        if (leftValue == 0) {
          explanation.add("base 0");
          return c(0);
        }
        if (leftValue == 1) {
          explanation.add("base 1");
          return c(1);
        }
      }
      if (base instanceof Power) {
        Power lp = (Power) base;
        return new Power(lp.base, NodeFactory.mul(lp.exponent, exponent));
      }
    }
    return new Power(base, exponent);
  }

  @Override
  public String2d toString2d(Stringify type) {
    return String2d.concat(
        base.embrace(type, PRECEDENCE_POWER),
        "^",
        exponent.embrace(type, PRECEDENCE_POWER));
  }

  @Override
  public int getPrecedence() {
    return PRECEDENCE_POWER;
  }
}
