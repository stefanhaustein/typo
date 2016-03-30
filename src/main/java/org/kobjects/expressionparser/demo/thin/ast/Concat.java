package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

class Concat extends Node {

  public Concat(Expression left, Expression right) {
    super(Type.STRING, left, right);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    resolveChildren(context);
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return "" + children[0].eval(context) + children[1].eval(context);
  }
}
