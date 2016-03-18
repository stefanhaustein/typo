package net.tidej.expressionparser.demo.derive.tree;

import java.util.Iterator;
import java.util.Map;

public class Sum extends Node {

  final double c;
  final QuantifiedNodeSet summands;

  Sum(double c, QuantifiedNodeSet summands) {
    this.c = c;
    this.summands = summands;
  }

  @Override
  public Node derive(String to) {
    QuantifiedNodeSet derived = new QuantifiedNodeSet(false);
    for (Node summand: summands.nodeSet()) {
      derived.add(1, summand.derive(to));
    }
    return new Sum(0, derived);
  }

  @Override
  public Node simplify() {
    QuantifiedNodeSet simplifiedSummands = new QuantifiedNodeSet(true);

    // Aggregate constants and flatten
    double cc = c;
    for (Map.Entry<Node, Double> entry: summands.entrySet()) {
      double count = entry.getValue();
      Node simplified = entry.getKey().simplify();
      if (simplified instanceof Sum) {
        Sum subSum = (Sum) simplified;
        cc += subSum.c;
        for (Map.Entry<Node, Double> subEntry: subSum.summands.entrySet()) {
          simplifiedSummands.add(subEntry.getValue(), subEntry.getKey());
        }
      } else if (simplified instanceof Constant) {
        cc += count * ((Constant) simplified).value;
      } else {
        simplifiedSummands.add(entry.getValue(), entry.getKey());
      }
    }

    if (simplifiedSummands.size() == 0) {
      return new Constant(cc);
    }
    if (simplifiedSummands.size() == 1 && cc == 0) {
      Map.Entry<Node, Double> entry = simplifiedSummands.entrySet().iterator().next();
      if (entry.getValue() == 1.0) {
        return entry.getKey();
      }
    }
    return new Sum(cc, simplifiedSummands);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (c != 0 || summands.size() == 0) {
      sb.append(c);
    }
    for (Map.Entry<Node,Double> entry: summands.entrySet()) {
      double count = entry.getValue();
      Node node = entry.getKey();

      if (count >= 0) {
        if (sb.length() > 0) {
          sb.append(" + ");
        }
        if (count == 1) {
          sb.append(node.toString(getPrecedence()));
        } else {
          sb.append(count);
          sb.append("*");
          sb.append(node.toString(1));
        }
      } else {
        sb.append(sb.length() > 0 ? " - " : "-");
        if (count == -1) {
          sb.append(node.toString(getPrecedence()));
        } else {
          sb.append(-count);
          sb.append("*");
          sb.append(node.toString(1));
        }
      }
    }
    return sb.toString();
  }

  @Override
  public int getChildCount() {
    return summands.size();
  }

  @Override
  public Node getChild(int index) {
    Iterator<Node> iterator = summands.nodeSet().iterator();
    for (int i = 0; i < index; i++) {
      iterator.next();
    }
    return iterator.next();
  }

  @Override
  public int getPrecedence() {
    return 0;
  }
}
