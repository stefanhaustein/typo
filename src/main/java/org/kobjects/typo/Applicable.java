package org.kobjects.typo;

import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.type.Typed;

public interface Applicable extends Typed {

  @Override
  FunctionType type();

  Object apply(org.kobjects.typo.runtime.EvaluationContext context);
}
