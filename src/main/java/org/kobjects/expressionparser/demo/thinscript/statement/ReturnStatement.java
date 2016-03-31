package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.expression.Expression;

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
