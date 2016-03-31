package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.expression.Expression;

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
  public void print(CodePrinter cp) {
    expression.print(cp);
    cp.append("; ");
  }

}
