package org.kobjects.typo.expression;

import org.kobjects.typo.parser.Position;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;
import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;

class Concat extends ExpressionN {

  public Concat(Position pos, Expression left, Expression right) {
    super(pos, left, right);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append('(');
    children[0].print(cp);
    cp.append(" + ");
    children[1].print(cp);
    cp.append(')');
  }

  @Override
  public Object eval(EvaluationContext context) {
    return "" + children[0].eval(context) + children[1].eval(context);
  }

  @Override
  public Type type() {
    return Types.STRING;
  }
}
