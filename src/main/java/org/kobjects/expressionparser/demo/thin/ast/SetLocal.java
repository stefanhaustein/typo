package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;

public class SetLocal extends Node {
  int index;
  SetLocal(int index, Expression node) {
    super(null, node);
    this.index = index;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    resolveChildren(context);
    this.type = children[0].type();
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    Object result = children[0].eval(context);
    context.setLocal(index, result);
    return result;
  }

}
