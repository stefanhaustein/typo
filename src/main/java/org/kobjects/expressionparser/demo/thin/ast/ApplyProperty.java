package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.Instance;
import org.kobjects.expressionparser.demo.thin.ParsingContext;

class ApplyProperty extends Node {
  final Expression base;
  final Classifier.Member member;

  ApplyProperty(Expression base, Classifier.Member member, Expression[] children) {
    super(member.type, children);
    this.base = base;
    this.member = member;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new RuntimeException("Already resolved");
  }

  @Override
  public Object eval(EvaluationContext context) {
    Instance instance = (Instance) base.eval(context);
    EvaluationContext newContext = new EvaluationContext(instance, member.implementation);
    for (int i = 0; i < children.length; i++) {
      newContext.setLocal(i, children[i].eval(context));
    }
    return member.implementation.apply(newContext);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(base.toString());
    sb.append('.');
    sb.append(member.name());
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
