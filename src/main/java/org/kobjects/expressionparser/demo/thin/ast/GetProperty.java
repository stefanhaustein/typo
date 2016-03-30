package org.kobjects.expressionparser.demo.thin.ast;

public class GetProperty extends Node {

  Classifier.Member member;

  public GetProperty(Node base, Classifier.Member member) {
    super(member.type, base);
    this.member = member;
  }

  @Override
  public Expression resolve(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved");
  }

  @Override
  public Object eval(org.kobjects.expressionparser.demo.thin.EvaluationContext context) {
    org.kobjects.expressionparser.demo.thin.Instance base = (org.kobjects.expressionparser.demo.thin.Instance) (children[0].eval(context));
    if (member.fieldIndex == -1) {
      return new org.kobjects.expressionparser.demo.thin.EvaluationContext(base, (org.kobjects.expressionparser.demo.thin.Applicable) member.implementation);
    }
    return base.fields[member.fieldIndex];
  }

  @Override
  public String toString() {
    return children[0] + "." + member.name;
  }
}
