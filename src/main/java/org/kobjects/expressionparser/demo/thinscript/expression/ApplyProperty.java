package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.Applicable;
import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.Instance;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.statement.TsClass;
import org.kobjects.expressionparser.demo.thinscript.type.FunctionType;

class ApplyProperty extends Node {
  final Expression base;
  final TsClass.Member member;

  ApplyProperty(Expression base, TsClass.Member member, Expression[] children) {
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
