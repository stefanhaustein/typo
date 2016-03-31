package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.expression.Expression;

public class WhileStatement extends SimpleStatement {
  public WhileStatement(Expression condition, Statement body) {
    super(condition, body);
  }

  @Override
  public Object eval(EvaluationContext context) {
    while ((Boolean) expression.eval(context)) {
      Object result = children[0].eval(context);
      if (result != NO_RESULT) {
        return result;
      }
    }
    return NO_RESULT;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("while (");
    expression.print(cp);
    cp.append(") ");
    children[0].print(cp);
  }
}
