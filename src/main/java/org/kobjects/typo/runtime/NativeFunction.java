package org.kobjects.typo.runtime;

import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.type.Type;

public abstract class NativeFunction implements Applicable {
  FunctionType type;

  public NativeFunction(Type returnType, FunctionType.Parameter... params) {
    this.type = new FunctionType(returnType, params);
  }

  @Override
  public EvaluationContext createContext(Instance self) {
    return new EvaluationContext(self, type.parameters.length);
  }

  @Override
  public FunctionType type() {
    return type;
  }
}
