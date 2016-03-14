package net.tidej.expressionparser.demo.derive.tree;

import java.util.ArrayList;

public class Sum extends Node {
  final Node[] summands;

  public Sum(Node... summands) {
    this.summands = summands;
  }

  @Override
  public Node derive(String to) {
    Node[] derived = new Node[summands.length];
    for (int i = 0; i < summands.length; i++) {
      derived[i] = summands[i].derive(to);
    }
    return new Sum(derived);
  }

  @Override
  public Node simplify() {
    ArrayList<Node> simplifiedSummands = new ArrayList<Node>();

    // Aggregate constants and flatten
    double c = 0;
    for (Node summand: summands) {
      Node simplified = summand.simplify();
      if (simplified instanceof Sum) {
        for (Node grandChild: ((Sum) simplified).summands) {
          if (grandChild instanceof Constant) {
            c += ((Constant) grandChild).value;
          } else {
            simplifiedSummands.add(grandChild);
          }
        }
      } else if (simplified instanceof Constant) {
        c += ((Constant) simplified).value;
      } else {
        simplifiedSummands.add(simplified);
      }
    }

    if (c != 0 || simplifiedSummands.size() == 0) {
      simplifiedSummands.add(0, new Constant(c));
    }
    if (simplifiedSummands.size() == 1) {
      return simplifiedSummands.get(0);
    }
    Node[] array = new Node[simplifiedSummands.size()];
    simplifiedSummands.toArray(array);
    return new Sum(array);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("(");
    for (Node summand: summands) {
      if (summand instanceof Negation) {
        sb.append(sb.length() == 1 ? "-" : " - ");
        sb.append(((Negation) summand).param);
      } else {
        if (sb.length() > 1) {
          sb.append(" + ");
        }
        sb.append(summand);
      }
    }
    sb.append(")");
    return sb.toString();
  }
}
