package org.kobjects.typo.statement;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.expression.Expression;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.Types;


public class ForStatement extends Statement{
  boolean declare;
  String variableName;
  ParsingContext.LocalDeclaration target;
  Statement body;
  Expression initialValue;
  Expression condition;
  Expression increment;

  public ForStatement(boolean declare, String varName, Expression initialValue, Expression condition, Expression increment, Statement body) {
    this.declare = declare;
    this.variableName = varName;
    this.initialValue = initialValue;
    this.condition = condition;
    this.increment = increment;
    this.body = body;
  }

  @Override
  public Object eval(EvaluationContext context) {
    context.setLocal(target.localIndex, initialValue.eval(context));
    while ((boolean) condition.eval(context)) {
      Object result = body.eval(context);
      if (result != NO_RESULT) {
        return result;
      }
      increment.eval(context);
    }
    return NO_RESULT;
  }

  @Override
  public void resolve(ParsingContext context) {
    initialValue = initialValue.resolve(context);
    if (declare) {
      target = context.declareLocal(variableName, Types.NUMBER);
    } else {
      target = context.resolveField(variableName);
    }
    condition = condition.resolve(context);
    if (condition.type() != Types.BOOLEAN) {
      throw new RuntimeException("for condition must be boolean.");
    }
    increment = increment.resolve(context);
    body.resolve(context);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("for (");
    if (declare) {
      cp.append("let ");
    }
    cp.append(variableName);
    cp.append(" = ");
    initialValue.print(cp);
    cp.append("; ");
    condition.print(cp);
    cp.append("; ");
    increment.print(cp);
    cp.append(") ");
    body.print(cp);
  }
}
