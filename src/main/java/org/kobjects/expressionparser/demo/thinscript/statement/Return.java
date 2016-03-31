package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.expression.Expression;

public class Return extends Statement {

  Expression expression;

  public Return(Expression expression) {
    this.expression = expression;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return expression.eval(context);
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
    cp.append("return ");
    expression.print(cp);
    cp.append("; ");
  }
}
