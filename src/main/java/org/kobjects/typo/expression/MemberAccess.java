package org.kobjects.typo.expression;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.parser.Position;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.Classifier;
import org.kobjects.typo.type.Type;

class MemberAccess extends Expression1 {
  Classifier.Member member;

  MemberAccess(Position pos, Expression base, Classifier.Member member) {
    super(pos, base);
    this.member = member;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    Object instance = child.eval(context);
    member.set(instance, value);
  }

  @Override
  public Object eval(EvaluationContext context) {
    Object instance = child.eval(context);
    // Needs to go this path for native member support.
    return member.get(instance);
  }

  @Override
  public boolean isAssignable() {
    return true; // member.fieldIndex != -1;
  }

  @Override
  public void print(CodePrinter cp) {
    child.print(cp);
    cp.append('.').append(member.name());
  }

  @Override
  public Type type() {
    return member.type();
  }
}
