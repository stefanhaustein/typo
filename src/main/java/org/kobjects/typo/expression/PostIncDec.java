package org.kobjects.typo.expression;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;

public class PostIncDec extends Expression1 {
  int delta;
  public PostIncDec(Expression child, int delta) {
    super(child);
    this.delta = delta;
  }

  @Override
  public Object eval(EvaluationContext context) {
    Number result = (Number) child.eval(context);
    child.assign(context, result.doubleValue() + delta);
    return result;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    super.resolve(context);
    if (!child.isAssignable() || child.type() != Types.NUMBER) {
      throw new RuntimeException("Assignable number required for ++/--");
    }
    return this;
  }

  @Override
  public Type type() {
    return Types.NUMBER;
  }

  @Override
  public void print(CodePrinter cp) {
    child.print(cp);
    cp.append(delta == 1 ? "++" : "--");
  }
}
