package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.type.Type;

public interface Expression {

  Expression resolve(org.kobjects.expressionparser.demo.thin.ParsingContext context);

  Object eval(org.kobjects.expressionparser.demo.thin.EvaluationContext context);

  Type type();

  void resolveSignatures(org.kobjects.expressionparser.demo.thin.ParsingContext context);
}
