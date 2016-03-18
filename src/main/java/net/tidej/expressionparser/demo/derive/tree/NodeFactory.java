package net.tidej.expressionparser.demo.derive.tree;

public class NodeFactory {

  public static Node add(Node... nodes) {
    QuantifiedNodeSet set = new QuantifiedNodeSet(false);
    for (Node node: nodes) {
      set.add(1, node);
    }
    return new Sum(0, set);
  }

  public static Node c(double c) {
    return new Constant(c);
  }

  public static Node sub(Node left, Node right) {
    QuantifiedNodeSet set = new QuantifiedNodeSet(false);
    set.add(1, left);
    set.add(-1, right);
    return new Sum(0, set);
  }

  public static Node neg(Node node) {
    QuantifiedNodeSet set = new QuantifiedNodeSet(false);
    set.add(-1, node);
    return new Sum(0, set);
  }

  public static Node mul(Node... factors) {
    return new Product(factors);
  }

  public static Node div(Node left, Node right) {
    return mul(left, rez(right));
  }

  public static Node rez(Node node) {
    return new Reciprocal(node);
  }

  public static Node pow(Node left, Node right) {
    return new Power(left, right);
  }
}
