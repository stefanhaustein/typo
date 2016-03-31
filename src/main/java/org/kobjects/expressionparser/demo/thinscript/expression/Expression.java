package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.Printable;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

public interface Expression extends Printable {
  boolean isAssignable();

  void assign(EvaluationContext context, Object value);

  Expression resolve(ParsingContext context);

  Object eval(EvaluationContext context);

  Type type();

  void resolveSignatures(ParsingContext context);
}
