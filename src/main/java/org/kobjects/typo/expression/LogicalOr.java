package org.kobjects.typo.expression;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.parser.Position;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;

public class LogicalOr extends ExpressionN {

  LogicalOr(Position pos, Expression left, Expression right) {
    super(pos, left, right);
  }

  @Override
  public Object eval(EvaluationContext context) {
    boolean left = (boolean) children[0].eval(context);
    if (!left) {
      return false;
    }
    return children[1].eval(context);
  }

  @Override
  public Type type() {
    return Types.BOOLEAN;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("(");
    children[0].print(cp);
    cp.append(" && ");
    children[1].print(cp);
    cp.append(")");
  }
}
