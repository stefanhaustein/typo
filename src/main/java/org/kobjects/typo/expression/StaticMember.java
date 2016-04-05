package org.kobjects.typo.expression;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.runtime.Instance;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.type.Type;

class StaticMember extends Expression {
  TsClass.Member member;

  StaticMember(TsClass.Member member) {
    this.member = member;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    member.staticValue = value;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return member.staticValue;
  }

  @Override
  public boolean isAssignable() {
    return true; // member.fieldIndex != -1;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append(member.owner.name()).append('.').append(member.name);
  }

  @Override
  public Type type() {
    return member.type;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved.");
  }

}
