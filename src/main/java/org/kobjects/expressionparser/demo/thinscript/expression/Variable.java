package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.Field;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

class Variable implements Expression {
  final Field field;

  public Variable(Field field) {
    this.field = field;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    field.set(context, value);
  }

  @Override
  public Object eval(EvaluationContext context) {
    return field.get(context);
  }

  @Override
  public boolean isAssignable() {
    return true;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append(field.name());
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved");
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
  }

  @Override
  public Type type() {
    return field.type();
  }
}
