package org.kobjects.expressionparser.demo.thin;

import org.kobjects.expressionparser.demo.thin.type.FunctionType;
import org.kobjects.expressionparser.demo.thin.type.Typed;

public interface Applicable extends Typed {

  @Override
  FunctionType type();

  Object apply(EvaluationContext context);
}
