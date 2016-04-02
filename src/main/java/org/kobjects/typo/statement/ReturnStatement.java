package org.kobjects.typo.statement;

import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.expression.Expression;

public class ReturnStatement extends SimpleStatement {

  public ReturnStatement(Expression expression) {
    super(expression);
  }

  @Override
  public Object eval(EvaluationContext context) {
    return expression.eval(context);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("return ");
    expression.print(cp);
    cp.append("; ");
  }
}
