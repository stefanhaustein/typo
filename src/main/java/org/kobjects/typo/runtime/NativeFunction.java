package org.kobjects.typo.runtime;

import org.kobjects.typo.expression.Function;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.type.Type;

public abstract class NativeFunction extends Function {

  public NativeFunction(TsClass owner, Type returnType, FunctionType.Parameter... params) {
    super(owner, null, params, returnType, null);
    this.type = new FunctionType(returnType, params);
    if (owner == null) {
      localCount = params.length;
    } else {
      localCount = params.length + 1;
      thisIndex = params.length;
    }
  }

  @Override
  public Function resolve(ParsingContext context) {
    return this;
  }

  @Override
  public abstract Object apply(EvaluationContext context);
}
