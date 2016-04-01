package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.Applicable;
import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.FunctionType;

public class Apply extends Node {
  Expression target;

  public Apply(Expression target, Expression... params) {
    super(null, params);
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
    resolveChildren(context);
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

    if (target instanceof Property) {
      Property property = (Property) target;
      return new ApplyProperty(property.base, property.member, children);
    }

    this.type = functionType.returnType;
    return this;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    target.resolveSignatures(context);
    super.resolveSignatures(context);
  }

}
