package org.kobjects.typo.expression;

import org.kobjects.typo.Applicable;
import org.kobjects.typo.statement.TsClass;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.Instance;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.FunctionType;

class ApplyMember extends Node {
  final Expression base;
  final TsClass.Member member;

  ApplyMember(Expression base, TsClass.Member member, Expression[] children) {
    super(((FunctionType) member.type()).returnType, children);
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
    EvaluationContext newContext = new EvaluationContext(instance, (Applicable) member.staticValue);
    for (int i = 0; i < children.length; i++) {
      newContext.setLocal(i, children[i].eval(context));
    }
    return ((Applicable) member.staticValue).apply(newContext);
  }

  @Override
  public void print(CodePrinter cp) {
    base.print(cp);
    cp.append('.');
    cp.append(member.name());
    cp.append('(');
    if (children.length > 0) {
      children[0].print(cp);
      for (int i = 1; i < children.length; i++) {
        cp.append(", ");
        children[i].print(cp);
      }
    }
    cp.append(')');
  }
}
