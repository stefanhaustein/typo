package org.kobjects.expressionparser.demo.thin.statement;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.ast.Expression;

public class ExpressionStatement extends Statement {
  public Expression expression;

  public ExpressionStatement(Expression expression) {
    this.expression = expression;
  }

  @Override
  public Object eval(EvaluationContext context) {
    expression.eval(context);
    return NO_RESULT;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    expression.resolveSignatures(context);
  }

  @Override
  public void resolve(ParsingContext context) {
    expression = expression.resolve(context);
  }

  @Override
  public String toString() {
    return expression + ";";
  }
}
