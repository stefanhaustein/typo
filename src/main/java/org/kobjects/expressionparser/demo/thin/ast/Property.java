package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.Instance;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

class Property implements Expression {
  Classifier.Member member;
  Expression base;

  Property(Expression base, Classifier.Member member) {
    this.base = base;
    this.member = member;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    Instance instance = (Instance) base.eval(context);
    instance.setField(member.fieldIndex, value);
  }

  @Override
  public Object eval(EvaluationContext context) {
    Instance instance = (Instance) base.eval(context);
    return member.fieldIndex == -1 ? member.implementation : instance.fields[member.fieldIndex];
  }

  @Override
  public boolean isAssignable() {
    return member.fieldIndex != -1;
  }

  @Override
  public Type type() {
    return member.type();
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved.");
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    throw new RuntimeException("Already resolved");
  }
}
