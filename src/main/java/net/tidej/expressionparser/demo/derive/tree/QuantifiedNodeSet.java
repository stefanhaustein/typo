package net.tidej.expressionparser.demo.derive.tree;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class QuantifiedNodeSet {
  Map<Node,Double> map;

  public QuantifiedNodeSet(boolean aggregate) {
    map = aggregate ? new TreeMap<Node,Double>() : new LinkedHashMap<Node,Double>();
  }

  public void add(double count, Node node) {
    Double old = map.get(node);
    map.put(node, old == null ? count : (old + count));
  }

  double get(Node node) {
    Double count = map.get(node);
    return count == null ? 0 : count;
  }


  public int size() {
    return map.size();
  }

  public Iterable<Node> nodeSet() {
    return map.keySet();
  }

  public Iterable<Map.Entry<Node, Double>> entrySet() {
    return map.entrySet();
  }
}
