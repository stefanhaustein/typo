package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

public interface Expression {
  boolean isAssignable();

  void assign(EvaluationContext context, Object value);

  Expression resolve(ParsingContext context);

  Object eval(EvaluationContext context);

  Type type();

  void resolveSignatures(ParsingContext context);
}
