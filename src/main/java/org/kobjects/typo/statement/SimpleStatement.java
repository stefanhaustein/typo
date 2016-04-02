package org.kobjects.typo.statement;

import org.kobjects.typo.expression.Expression;
import org.kobjects.typo.parser.ParsingContext;

public abstract class SimpleStatement extends Statement {
  public Expression expression;
  public Statement[] children;

  SimpleStatement(Expression expression, Statement... children) {
    this.expression = expression;
    this.children = children;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    if (expression != null) {
      expression.resolveSignatures(context);
    }
    for (Statement child: children) {
      child.resolveSignatures(context);
    }
  }

  @Override
  public void resolve(ParsingContext context) {
    if (expression != null) {
      expression = expression.resolve(context);
    }
    for (Statement child: children) {
      child.resolve(context);
    }
  }

}
