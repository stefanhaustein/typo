package org.kobjects.typo.expression;

import org.kobjects.typo.parser.Position;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.io.Printable;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.parser.ParsingContext;

public abstract class Expression implements Printable {
  public Position pos;

  Expression(Position pos) {
    this.pos = pos;
  }

  boolean isAssignable() {
    return false;
  }

  void assign(EvaluationContext context, Object value) {
    throw new UnsupportedOperationException();
  }

  public abstract Object eval(EvaluationContext context);

  public abstract Expression resolve(ParsingContext context);


  public abstract Type type();
}
