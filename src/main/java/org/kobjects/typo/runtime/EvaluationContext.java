package org.kobjects.typo.runtime;

import org.kobjects.typo.Applicable;
import org.kobjects.typo.expression.Function;
import org.kobjects.typo.parser.ParsingContext;

public class EvaluationContext {
  static final Object[] NO_LOCALS = new Object[0];

  public org.kobjects.typo.runtime.Instance self;
  public Object[] locals;

  public EvaluationContext(org.kobjects.typo.runtime.Instance self, Applicable applicable) {
    this.self = self;
    if (applicable instanceof Function) {
      Function function = (Function) applicable;
      this.locals = new Object[function.localCount];
    } else if (applicable instanceof Applicable){
      this.locals = new Object[applicable.type().parameters.length];
    } else {
      this.locals = NO_LOCALS;
    }
  }

  public void adjustLocals(ParsingContext parsingContext) {
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
