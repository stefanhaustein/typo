package org.kobjects.expressionparser.demo.thin.statement;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.ast.Expression;
import org.kobjects.expressionparser.demo.thin.ast.UnresolvedOperator;

public abstract class Statement {
  String NO_RESULT = new String("NO_RESULT");

  public abstract Object eval(EvaluationContext context);

  public abstract void resolveSignatures(ParsingContext context);

  public abstract void resolve(ParsingContext context);
}