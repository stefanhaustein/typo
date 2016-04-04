package org.kobjects.typo.statement;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.expression.Expression;

public class WhileStatement extends SimpleStatement {
  public WhileStatement(Expression condition, Statement body) {
    super(condition, body);
  }

  @Override
  public Object eval(EvaluationContext context) {
    while ((Boolean) expression.eval(context)) {
      Object result = children[0].eval(context);
      if (result != Statement.NO_RESULT) {
        return result;
      }
    }
    return Statement.NO_RESULT;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("while (");
    expression.print(cp);
    cp.append(") ");
    children[0].print(cp);
  }
}
