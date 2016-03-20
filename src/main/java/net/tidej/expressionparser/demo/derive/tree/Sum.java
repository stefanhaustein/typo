package net.tidej.expressionparser.demo.derive.tree;

import net.tidej.expressionparser.demo.derive.string2d.String2d;

import java.util.Map;
import java.util.Set;

class Sum extends Node {
  final double c;
  final QuantifiedSet<Node> summands;

  /**
   * Flatten on construction so this "trivial" operation doesn't show up in simplification.
   */
  Sum(double c, QuantifiedSet<Node> summands) {
    QuantifiedSet.Mutable<Node> builder = new QuantifiedSet.Mutable<>(false);
    for (Map.Entry<Node,Double> entry : summands.entries()) {
      if (entry.getKey() instanceof Sum) {
        Sum subSum = (Sum) entry.getKey();
        double factor = entry.getValue();
        if (subSum.c != 0) {
          builder.add(factor, new Constant(subSum.c));
        }
        for (Map.Entry<Node,Double> subEntry : subSum.summands.entries()) {
          builder.add(factor * subEntry.getValue(), subEntry.getKey());
        }
      } else {
        builder.add(entry);
      }
    }
    this.c = c;
    this.summands = builder;
  }

  @Override
  public Node derive(String to, Set<String> explanation) {
    QuantifiedSet.Mutable derived = new QuantifiedSet.Mutable(false);
    if (summands.size() > 1) {
      explanation.add("sum rule");
    }
    for (Map.Entry<Node,Double> summand: summands.entries()) {
      if (summand.getValue() != 1) {
        explanation.add("constant factor rule");
      }
      derived.add(summand.getValue(), NodeFactory.derive(summand.getKey(), to));
    }
    return new Sum(0, derived);
  }

  @Override
  public Node simplify(Set<String> explanation) {
    QuantifiedSet.Mutable<Node> simplified = new QuantifiedSet.Mutable<>(false);
    boolean changed = false;
    double cc = c;
    // Simplify children
    for (Map.Entry<Node, Double> entry: summands.entries()) {
      Node node = entry.getKey().simplify(explanation);
      changed = changed || !node.equals(entry.getKey());
      simplified.add(entry.getValue(), node);
    }

    if (!changed) {
      // Aggregate constants and flatten
      simplified = new QuantifiedSet.Mutable<>(true);
      for (Map.Entry<Node, Double> entry : summands.entries()) {
        double count = entry.getValue();
        Node node = entry.getKey();
        if (node instanceof Product && ((Product) node).c != 1) {
          // Pull up constant factors.
          Product product = ((Product) node);
          simplified.add(count * product.c, new Product(1, product.factors));
        } else if (node instanceof Constant) {
          cc += count * ((Constant) node).value;
        } else {
          simplified.add(count, node);
        }
      }
      if (simplified.size() == 1 && cc == 0) {
        Map.Entry<Node, Double> entry = simplified.entries().iterator().next();
        if (entry.getValue() == 1.0) {
          return entry.getKey();
        }
      }
    }

    if (simplified.size() == 0) {
      return new Constant(cc);
    }

    return new Sum(cc, simplified);
  }

  public String2d toString2d(Stringify type) {
    String2d.Builder sb = new String2d.Builder();
    if (c != 0 || summands.size() == 0) {
      sb.append(Constant.toString(c));
    }
    for (Map.Entry<Node,Double> entry: summands.entries()) {
      double count = entry.getValue();
      Node node = entry.getKey();
      sb.append(count >= 0
          ? (sb.isEmpty() ? "" : " + ")
          : (sb.isEmpty() ? "-" : " − "));
      sb.append(Product.toString2d(type, Math.abs(count), node));
    }
    return sb.build();
  }

  @Override
  public int getPrecedence() {
    return 0;
  }
}
