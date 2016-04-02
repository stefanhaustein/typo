package org.kobjects.typo.statement;

import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.expression.Expression;
import org.kobjects.typo.CodePrinter;

public class ExpressionStatement extends SimpleStatement {

  public ExpressionStatement(Expression expression) {
    super(expression);
  }

  @Override
  public Object eval(EvaluationContext context) {
    expression.eval(context);
    return NO_RESULT;
  }

  @Override
  public void print(CodePrinter cp) {
    expression.print(cp);
    cp.append("; ");
  }

}
