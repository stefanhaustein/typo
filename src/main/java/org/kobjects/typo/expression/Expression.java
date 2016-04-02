package org.kobjects.typo.expression;

import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.Printable;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.parser.ParsingContext;

public interface Expression extends Printable {
  boolean isAssignable();

  void assign(EvaluationContext context, Object value);

  Expression resolve(ParsingContext context);

  Object eval(EvaluationContext context);

  Type type();

  void resolveSignatures(ParsingContext context);
}
