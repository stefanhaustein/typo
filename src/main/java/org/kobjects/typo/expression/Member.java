package org.kobjects.typo.expression;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.runtime.Instance;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.type.Type;

class Member extends Expression {
  TsClass.Member member;
  Expression base;

  Member(Expression base, TsClass.Member member) {
    this.base = base;
    this.member = member;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    if (member.fieldIndex == -1) {
      member.set(context, value);
    } else {
      Instance instance = (Instance) base.eval(context);
      instance.setField(member.fieldIndex, value);
    }
  }

  @Override
  public Object eval(EvaluationContext context) {
    Instance instance = (Instance) base.eval(context);
    return member.fieldIndex == -1 ? member.staticValue : instance.fields[member.fieldIndex];
  }

  @Override
  public boolean isAssignable() {
    return true; // member.fieldIndex != -1;
  }

  @Override
  public void print(CodePrinter cp) {
    base.print(cp);
    cp.append('.').append(member.name());
  }

  @Override
  public Type type() {
    return member.type();
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved.");
  }

}
