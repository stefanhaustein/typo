package net.tidej.expressionparser.demo.derive.tree;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class Product extends Node {
  final double c;
  final QuantifiedSet<Node> factors;

  /**
   * Flatten on construction so this "trivial" operation doesn't show up in simplification.
   */
  Product(double c, QuantifiedSet<Node> factors) {
    QuantifiedSet.Mutable<Node> builder = new QuantifiedSet.Mutable<>(false);
    for (Map.Entry<Node,Double> entry : factors.entries()) {
      if (entry.getKey() instanceof Product) {
        Product subProduct = (Product) entry.getKey();
        double exponent = entry.getValue();
        if (subProduct.c != 1) {
          builder.add(exponent, new Constant(subProduct.c));
        }
        for (Map.Entry<Node,Double> subEntry : subProduct.factors.entries()) {
          builder.add(subEntry.getValue() * exponent, subEntry.getKey());
        }
      } else {
        builder.add(entry);
      }
    }
    this.c = c;
    this.factors = builder;
  }

  public static Node factorNode(Map.Entry<Node, Double> entry) {
    return NodeFactory.powC(entry.getKey(), entry.getValue());
  }

  @Override
  public Node derive(String to, Set<String> explanation) {
    if (factors.size() == 0) {
      return NodeFactory.c(0);
    }

    Iterator<Map.Entry<Node, Double>> i = factors.entries().iterator();
    if (c != 1) {
      explanation.add("constant factor rule");
      return NodeFactory.cMul(c, NodeFactory.derive(new Product(1, QuantifiedSet.of(i)), to));
    }
    if (factors.size() == 1) {
      Map.Entry<Node, Double> entry = i.next();
      double exponent = entry.getValue();
      Node base = entry.getKey();
      if (exponent == 1) {
        return base.derive(to, explanation);
      }
      if (exponent == -1) {
        explanation.add("reciprocal rule");
        return NodeFactory.div(
            NodeFactory.cMul(-1, NodeFactory.derive(base, to)),
            NodeFactory.powC(base, 2));
      }
      if (base.toString().equals(to)) {
        explanation.add("power rule");
        return NodeFactory.cMul(exponent, NodeFactory.powC(base, exponent - 1));
      }
      return new Power(entry.getKey(), new Constant(entry.getValue())).derive(to, explanation);
    }

    explanation.add("product rule");
    Node left = factorNode(i.next());
    Node right = factors.size() == 2 ? factorNode(i.next())
        : new Product(1, QuantifiedSet.of(i));

    return NodeFactory.add(
        NodeFactory.mul(left, NodeFactory.derive(right, to)),
        NodeFactory.mul(NodeFactory.derive(left, to), right));
  }

  @Override
  public Node simplify(Set<String> explanation) {
    QuantifiedSet.Mutable<Node> simplified = new QuantifiedSet.Mutable<>(false);

    double cc = c;
    boolean changed = false;
    for (Map.Entry<Node, Double> entry: factors.entries()) {
      Node node = entry.getKey().simplify(explanation);
      changed = changed || !node.equals(entry.getKey());
      simplified.add(entry.getValue(), node);
    }
    if (!changed) {
      // See what we can inline / de-duplicate
      simplified = new QuantifiedSet.Mutable<>(true);
      for (Map.Entry<Node, Double> entry : factors.entries()) {
        Node node = entry.getKey();
        double exponent = entry.getValue();
        if (node instanceof Power && ((Power) node).exponent instanceof Constant) {
          simplified.add(exponent * ((Constant) ((Power) node).exponent).value, ((Power) node).base);
        } else if (node instanceof Constant) {
          cc *= Math.pow(((Constant) node).value, exponent);
        } else if ((node instanceof Sum) && ((Sum) node).c == 0 && ((Sum) node).summands.size() == 1) {
          Map.Entry<Node,Double> summand = ((Sum) node).summands.entries().iterator().next();
          cc *= summand.getValue();
          simplified.add(1, summand.getKey());
        } else {
          simplified.add(exponent, node);
        }
      }

      // Just a constant
      if (cc == 0 || simplified.size() == 0) {
        return new Constant(cc);
      }

      // Constant factor only?
      if (simplified.size() == 1) {
        Map.Entry<Node, Double> entry = simplified.entries().iterator().next();
        if (entry.getValue() == 1.0) {
          if (cc == 1.0) {
            return entry.getKey();
          }
          QuantifiedSet.Mutable<Node> builder = new QuantifiedSet.Mutable<>(false);
          builder.add(cc, entry.getKey());
        }
      }
    }
    return new Product(cc, simplified);
  }

  public void toString(StringBuilder sb, boolean verbose) {
    int l0 = sb.length();
    if (c != 1 || factors.size() == 0 || verbose) {
      sb.append(Constant.toString(c));
    }
    if (factors.size() == 0 && verbose) {
      sb.append("⋅1");
    } else {
      for (Map.Entry<Node,Double> entry: factors.entries()) {
        double power = entry.getValue();
        Node node = entry.getKey();
        sb.append(power >= 0
            ? (sb.length() > l0 ? "⋅" : "")
            : (sb.length() > l0 ? "/" : "1/"));
        double abs = Math.abs(power);
        if (abs == 1 && !verbose) {
          node.embrace(sb, verbose, getPrecedence());
        } else {
          node.embrace(sb, verbose, 5);
          sb.append('^');
          sb.append(Constant.toString(abs));
        }
      }
    }
  }

  @Override
  public int getPrecedence() {
    return 1;
  }
}
