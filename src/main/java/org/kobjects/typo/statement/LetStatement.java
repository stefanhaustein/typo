package org.kobjects.typo.statement;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.expression.Expression;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.Type;

public class LetStatement extends Statement {
  ParsingContext.LocalDeclaration target;
  String variableName;
  Expression expression;
  Type type;

  public LetStatement(String variableName, Type type, Expression expression) {
    this.variableName = variableName;
    this.type = type;
    this.expression = expression;
    if (type == null && expression == null) {
      throw new RuntimeException("Expression or type required.");
    }
  }

  @Override
  public Object eval(EvaluationContext context) {
    if (expression != null) {
      target.set(context, expression.eval(context));
    }
    return NO_RESULT;
  }

  @Override
  public void resolve(ParsingContext context) {
    if (type != null) {
      type = type.resolve(context);
    }
    if (expression != null) {
      expression = expression.resolve(context);
      if (type == null) {
        type = expression.type();
      } else if (!type.assignableFrom(expression.type())) {
        throw new RuntimeException("Incompatible types for " + CodePrinter.toString(this));
      }
    }
    target = context.declareLocal(variableName, expression.type());
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("let ").append(variableName);
    if (type != null) {
      cp.append(": ");
      cp.append(type.name());
    }
    if (expression != null) {
      cp.append(" = ");
      expression.print(cp);
    }
    cp.append("; ");
  }
}
