package org.kobjects.expressionparser.demo.typo;

public class SetLocal extends Node {
  int index;
  SetLocal(int index, Node node) {
    super(node.type, node);
    this.index = index;
  }

  @Override
  Object eval(EvaluationContext context) {
    Object result = children[0].eval(context);
    context.setLocal(index, result);
    return result;
  }
}
