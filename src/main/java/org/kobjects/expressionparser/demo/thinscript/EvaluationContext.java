package org.kobjects.expressionparser.demo.thinscript;

import org.kobjects.expressionparser.demo.thinscript.expression.Function;

public class EvaluationContext {
  static final Object[] NO_LOCALS = new Object[0];

  public Instance self;
  public Object[] locals;

  public EvaluationContext(Instance self, Applicable applicable) {
    this.self = self;
    if (applicable instanceof Function) {
      Function function = (Function) applicable;
      Function.Parameter[] params = function.parameters;
      this.locals = new Object[params.length];
    } else if (applicable instanceof Applicable){
      this.locals = new Object[applicable.type().parameterTypes.length];
    } else {
      this.locals = NO_LOCALS;
    }
  }

  void adjustLocals(org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext parsingContext) {
    if (parsingContext.locals.size() > locals.length) {
      Object[] newLocals = new Object[parsingContext.locals.size()];
      System.arraycopy(locals, 0, newLocals, 0, locals.length);
      locals = newLocals;
    }
  }

  public void setLocal(int index, Object value) {
    locals[index] = value;
  }

  public Object getLocal(int index) {
    return locals[index];
  }
}
