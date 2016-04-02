package org.kobjects.typo.runtime;

import org.kobjects.typo.parser.ParsingContext;

public class EvaluationContext {
  static final Object[] NO_LOCALS = new Object[0];

  public Instance self;
  public Object[] locals;

  public EvaluationContext(Instance self, int size) {
    this.self = self;
    this.locals = size == 0 ? NO_LOCALS : new Object[size];
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
