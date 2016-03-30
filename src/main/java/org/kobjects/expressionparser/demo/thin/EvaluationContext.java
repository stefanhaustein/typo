package org.kobjects.expressionparser.demo.thin;

import org.kobjects.expressionparser.demo.thin.ast.Function;

public class EvaluationContext {
  public Instance self;
  public Object[] locals;
  public Applicable applicable;

  public EvaluationContext(Instance self, Applicable applicable) {
    this.self = self;
    this.applicable = applicable;
    if (applicable instanceof Function) {
      Function function = (Function) applicable;
      Function.Parameter[] params = function.parameters;
      this.locals = new Object[params.length];
    } else if (applicable instanceof Applicable){
      this.locals = new Object[applicable.type().parameterTypes.length];
    } else {
      this.locals = new Object[0];
    }
  }

  public void setLocal(int index, Object value) {
    locals[index] = value;
  }

  public Object getLocal(int index) {
    return locals[index];
  }
}
