package org.kobjects.typo.expression;

import org.kobjects.typo.parser.Position;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.runtime.Instance;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.type.Type;

class ApplyMember extends ExpressionN {
  final Expression base;
  final TsClass.Member member;

  ApplyMember(Position pos, Expression base, TsClass.Member member, Expression[] children) {
    super(pos, children);
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
    Function method = (Function) member.staticValue;
    EvaluationContext newContext = method.createContext(instance);
    for (int i = 0; i < children.length; i++) {
      newContext.setLocal(i, children[i].eval(context));
    }
    return ((Function) member.staticValue).apply(newContext);
  }

  @Override
  public void print(CodePrinter cp) {
    base.print(cp);
    cp.append('.');
    cp.append(member.name);
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

  @Override
  public Type type() {
    return ((FunctionType) member.type).returnType;
  }
}
