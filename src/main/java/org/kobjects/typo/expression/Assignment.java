package org.kobjects.typo.expression;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.Type;

public class Assignment extends ExpressionN {

  Assignment(Expression target, Expression source) {
    super(target, source);
    if (!target.type().assignableFrom(source.type())) {
      throw new RuntimeException("Can't assign " + source.type() + " to " + target.type()
          + " in " + CodePrinter.toString(this));
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

  @Override
  public Type type() {
    return children[0].type();
  }
}
