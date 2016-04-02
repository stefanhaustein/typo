package org.kobjects.typo.expression;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;

public class Assignment extends Node {

  Assignment(Expression target, Expression source) {
    super(source.type(), target, source);
    if (source.type() != target.type()) {
      throw new RuntimeException("Can't assign " + source.type() + " to " + target.type());
    }
  }

  @Override
  public Object eval(EvaluationContext context) {
    Object value = children[1].eval(context);
    children[0].assign(context, value);
    return value;
  }

  @Override
  public void print(CodePrinter cp) {
    children[0].print(cp);
    cp.append(" = ");
    children[1].print(cp);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Resolved already.");
  }
}
