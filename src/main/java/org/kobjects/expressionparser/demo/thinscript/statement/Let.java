package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.Field;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.expression.Expression;

public class Let extends Statement {
  Field target;
  String variableName;
  Expression expression;

  public Let(String variableName, Expression expression) {
    this.variableName = variableName;
    this.expression = expression;
  }

  @Override
  public Object eval(EvaluationContext context) {
    target.set(context, expression.eval(context));
    return NO_RESULT;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    expression.resolveSignatures(context);
  }

  @Override
  public void resolve(ParsingContext context) {
    expression = expression.resolve(context);
    target = context.declareLocal(variableName, expression.type());
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("let ").append(variableName).append(" = ");
    expression.print(cp);
    cp.append("; ");
  }
}
