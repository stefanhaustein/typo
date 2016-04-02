package org.kobjects.typo.expression;

import org.kobjects.typo.Field;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;

class Variable extends Expression {
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
  public Type type() {
    return field.type();
  }
}
