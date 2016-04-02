package org.kobjects.typo.expression;

import org.kobjects.typo.type.Type;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;

public abstract class ExpressionN extends Expression {
  public final Expression[] children;

  ExpressionN(Expression... children) {
    this.children = children;
  }

  double evalNumber(EvaluationContext context, int index) {
    return ((Number) children[index].eval(context)).doubleValue();
  }

  @Override
  public Expression resolve(ParsingContext context) {
    for (int i = 0; i < children.length; i++) {
      children[i] = children[i].resolve(context);
    }
    return this;
  }

  public Type[] childTypes() {
    Type[] result = new Type[children.length];
    for (int i = 0; i < children.length; i++) {
      result[i] = children[i].type();
    }
    return result;
  }
}
