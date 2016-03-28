package org.kobjects.expressionparser.demo.typo;


class EvaluationContext {
  Instance self;
  Object[] locals;
  Applicable applicable;

  EvaluationContext(Instance self, Applicable applicable) {
    this.self = self;
    this.applicable = applicable;
    if (applicable instanceof Function) {
      Function function = (Function) applicable;
      this.locals = new Object[function.parameters.length];
    } else if (applicable instanceof Applicable){
      this.locals = new Object[applicable.type().parameterTypes.length];
    } else {
      this.locals = new Object[0];
    }
  }

  void setLocal(int index, Object value) {
    locals[index] = value;
  }

  Object getLocal(int index) {
    return locals[index];
  }
}
