package org.kobjects.typo.runtime;

import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.runtime.Instance;
import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.type.Typed;

public interface Applicable extends Typed {

  @Override
  FunctionType type();

  Object apply(EvaluationContext context);

  EvaluationContext createContext(Instance self);
}
