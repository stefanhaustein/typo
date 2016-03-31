package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.Applicable;
import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

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
  public Expression resolve(ParsingContext context) {
    resolveChildren(context);
    target = target.resolve(context);

    if (target instanceof Property) {
      Property property = (Property) target;
      return new ApplyProperty(property.base, property.member, children);
    }

    return this;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    target.resolveSignatures(context);
    super.resolveSignatures(context);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("(");
    if (children.length > 0) {
      cp.append(children[0]);
      for (int i = 1; i < children.length; i++) {
        cp.append(", ");
        cp.append(children[i]);
      }
    }
    cp.append(')');
  }
}
