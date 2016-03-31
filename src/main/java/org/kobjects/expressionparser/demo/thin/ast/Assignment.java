package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;

public class Assignment extends Node {

  Assignment(Expression target, Expression source) {
    super(source.type(), target, source);
    if (source.type() != target.type()) {
      throw new RuntimeException("Can't assign " + source.type() + " to " + target.type());
    }
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Resolved already.");
  }

  @Override
  public Object eval(EvaluationContext context) {
    Object value = children[1].eval(context);
    children[0].assign(context, value);
    return value;
  }

  @Override
  public String toString() {
    return children[0] + " = " + children[1];
  }
}
