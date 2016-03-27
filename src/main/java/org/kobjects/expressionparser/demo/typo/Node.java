package org.kobjects.expressionparser.demo.typo;

abstract class Node {
  final Type type;
  final Node[] children;

  Node(Type type, Node... children) {
    this.type = type;
    this.children = children;
  }

  abstract Object eval(EvaluationContext context);

  double evalF64(EvaluationContext context, int index) {
    return (Double) children[index].eval(context);
  }
}
