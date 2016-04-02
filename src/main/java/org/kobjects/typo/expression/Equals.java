package org.kobjects.typo.expression;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;

public class Equals extends ExpressionN {
  Equals(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved");
  }

  @Override
  public Object eval(EvaluationContext context) {
    Object left = children[0].eval(context);
    Object right = children[1].eval(context);
    if (left == null || right == null) {
      return left == right;
    }
    if (left instanceof Double || right instanceof Double) {
      if (!(left instanceof Number) || !(right instanceof Number)) {
        return false;
      }
      return ((Number) left).doubleValue() == ((Number) right).doubleValue();
    }
    return left.equals(right);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append('(');
    children[0].print(cp);
    cp.append(" == ");
    children[1].print(cp);
    cp.append(')');
  }

  @Override
  public Type type() {
    return Types.BOOLEAN;
  }

}
