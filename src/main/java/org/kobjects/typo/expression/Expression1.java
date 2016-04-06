package org.kobjects.typo.expression;

import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.parser.Position;

public abstract class Expression1 extends Expression {
  public Expression child;

  Expression1(Position pos, Expression child) {
    super(pos);
    this.child = child;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    child = child.resolve(context);
    return this;
  }

}
