package net.tidej.expressionparser.demo.derive.tree;

import java.util.ArrayList;
import java.util.Arrays;

public class Product extends Node {
  final Node[] factors;

  public Product(Node... factors) {
    this.factors = factors;
  }

  @Override
  public Node derive(String to) {
    Node left = factors[0];
    Node right = factors.length == 2 ? factors[1] : new Product(Arrays.copyOfRange(factors, 1, factors.length));
    return new Sum(
         new Product(left, right.derive(to)),
         new Product(left.derive(to), right));
  }

  @Override
  public Node simplify() {
    ArrayList<Node> simplifiedFactors = new ArrayList<Node>();
    double c = 1;
    for (Node factor: factors) {
      Node simplified = factor.simplify();
      if (simplified instanceof Product) {
        for (Node grandChild : ((Product) simplified).factors) {
          if (grandChild instanceof Constant) {
            c *= ((Constant) grandChild).value;
          } else if (grandChild instanceof Negation) {
            c = -c;
            simplifiedFactors.add(((Negation) grandChild).param);
          } else {
            simplifiedFactors.add(grandChild);
          }
        }
      } else if (simplified instanceof Constant) {
        c *= ((Constant) simplified).value;
      } else if (simplified instanceof Negation) {
        c = -c;
        simplifiedFactors.add(((Negation) simplified).param);
      } else {
        simplifiedFactors.add(simplified);
      }
    }

    if (c == 0) {
      return new Constant(0);
    }
    if (c != 1 || simplifiedFactors.size() == 0) {
      simplifiedFactors.add(0, new Constant(c));
    }
    if (simplifiedFactors.size() == 1) {
      return simplifiedFactors.get(0);
    }
    Node[] array = new Node[simplifiedFactors.size()];
    simplifiedFactors.toArray(array);
    return new Product(array);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("(");
    for (Node factor: factors) {
      if (factor instanceof Reciprocal) {
        sb.append(sb.length() == 1 ? "1/" : " / ");
        sb.append(((Reciprocal) factor).param);
      } else {
        if (sb.length() > 1) {
          sb.append(" * ");
        }
        sb.append(factor);
      }
    }
    sb.append(")");
    return sb.toString();
  }
}
