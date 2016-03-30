package org.kobjects.expressionparser.demo.thin;

import org.kobjects.expressionparser.demo.thin.type.Typed;

public interface Applicable extends Typed {

  @Override
  org.kobjects.expressionparser.demo.thin.type.FunctionType type();

  Object apply(EvaluationContext context);
}
