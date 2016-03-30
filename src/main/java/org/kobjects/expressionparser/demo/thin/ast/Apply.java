package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;

class Apply extends Node {
  Expression target;

  public Apply(Expression target, Expression... params) {
    super(null, params);
    this.target = target;
  }

  @Override
  public Object eval(EvaluationContext context) {
    EvaluationContext newContext = (EvaluationContext) target.eval(context);
    for (int i = 0; i < children.length; i++) {
      newContext.setLocal(i, children[i].eval(context));
    }
    return newContext.applicable.apply(newContext);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    resolveChildren(context);
    target = target.resolve(context);
    return this;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    target.resolveSignatures(context);
    super.resolveSignatures(context);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(target.toString());
    sb.append('(');
    if (children.length > 0) {
      sb.append(children[0]);
      for (int i = 1; i < children.length; i++) {
        sb.append(", ");
        sb.append(children[i]);
      }
    }
    sb.append(')');
    return sb.toString();
  }
}
