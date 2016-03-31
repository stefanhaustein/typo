package org.kobjects.expressionparser.demo.thinscript;

import org.kobjects.expressionparser.demo.thinscript.type.FunctionType;
import org.kobjects.expressionparser.demo.thinscript.type.Typed;

public interface Applicable extends Typed {

  @Override
  FunctionType type();

  Object apply(EvaluationContext context);
}
