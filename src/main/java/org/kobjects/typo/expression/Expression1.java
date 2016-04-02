package org.kobjects.typo.expression;

import org.kobjects.typo.parser.ParsingContext;

public abstract class Expression1 extends Expression {
  public Expression child;

  Expression1(Expression child) {
    this.child = child;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    child = child.resolve(context);
    return this;
  }

}
