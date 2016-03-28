package org.kobjects.expressionparser.demo.typo;


class GetProperty extends Node {

  Classifier.Member member;

  GetProperty(Node base, Classifier.Member member) {
    super(member.type, base);
    this.member = member;
  }

  @Override
  Object eval(EvaluationContext context) {
    Instance base = (Instance) (children[0].eval(context));
    if (member.fieldIndex == -1) {
      return new EvaluationContext(base, member.implementation);
    }
    return base.fields[member.fieldIndex];
  }

  @Override
  public String toString() {
    return children[0] + "." + member.name;
  }
}
