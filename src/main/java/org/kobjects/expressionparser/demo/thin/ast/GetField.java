package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.Field;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

class GetField implements Expression {
  final Field field;

  public GetField(Field field) {
    this.field = field;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved");
  }

  @Override
  public Object eval(EvaluationContext context) {
    return field.get(context);
  }

  @Override
  public Type type() {
    return field.type();
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
  }

  @Override
  public String toString() {
    return field.name();
  }
}
