package org.kobjects.expressionparser.demo.thin.statement;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.Field;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.ast.Expression;

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
  public String toString() {
    return "let " + expression + ";";
  }
}
