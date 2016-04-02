package org.kobjects.typo.statement;

import org.kobjects.typo.Printable;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;

public abstract class Statement implements Printable {
  final static Object NO_RESULT = new String("NO_RESULT");

  public abstract Object eval(EvaluationContext context);

  public abstract void resolve(ParsingContext context);
}