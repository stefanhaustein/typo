package org.kobjects.typo.statement;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.Field;
import org.kobjects.typo.expression.Expression;
import org.kobjects.typo.parser.ParsingContext;

public class LetStatement extends Statement {
  Field target;
  String variableName;
  Expression expression;

  public LetStatement(String variableName, Expression expression) {
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
