package org.kobjects.typo.expression;

import org.kobjects.typo.Applicable;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.type.Type;

public class Apply extends ExpressionN {
  Expression target;

  public Apply(Expression target, Expression... params) {
    super(params);
    this.target = target;
  }

  @Override
  public Object eval(EvaluationContext context) {
    Applicable applicable = (Applicable) target.eval(context);
    EvaluationContext newContext = new EvaluationContext(context.self, applicable);
    for (int i = 0; i < children.length; i++) {
      newContext.setLocal(i, children[i].eval(context));
    }
    return applicable.apply(newContext);
  }

  @Override
  public void print(CodePrinter cp) {
    target.print(cp);
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
  public Expression resolve(ParsingContext context) {
    super.resolve(context);
    target = target.resolve(context);

    if (!(target.type() instanceof FunctionType)) {
      throw new RuntimeException("Target must be function for apply() instead of " + target.type());
    }

    FunctionType functionType = (FunctionType) target.type();
    try {
      functionType.assertSignature(childTypes(), CodePrinter.toString(this));
    } catch (Exception e) {
      throw new RuntimeException("In " + CodePrinter.toString(this), e);
    }

    if (target instanceof Member) {
      Member property = (Member) target;
      return new ApplyMember(property.base, property.member, children);
    }
    return this;
  }

  public Type type() {
    return ((FunctionType) target.type()).returnType;
  }
}
