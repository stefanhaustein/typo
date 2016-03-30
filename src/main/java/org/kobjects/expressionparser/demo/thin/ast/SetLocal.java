package org.kobjects.expressionparser.demo.thin.ast;

public class SetLocal extends Node {
  int index;
  SetLocal(int index, Expression node) {
    super(null, node);
    this.index = index;
  }

  @Override
  public Expression resolve(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    resolveChildren(context);
    this.type = children[0].type();
    return this;
  }

  @Override
  public Object eval(org.kobjects.expressionparser.demo.thin.EvaluationContext context) {
    Object result = children[0].eval(context);
    context.setLocal(index, result);
    return result;
  }

}
