package org.kobjects.typo.expression;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.parser.Position;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;

public class Ternary extends ExpressionN {
  Type type;
  public Ternary(Position pos, Expression condition, Expression ifBranch, Expression elseBranch) {
    super(pos, condition, ifBranch, elseBranch);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    super.resolve(context);
    this.type = Types.commonType(children[1].type(), children[2].type());
    if (children[0].type() != Types.BOOLEAN) {
      throw new RuntimeException("Ternary condition must be boolean. " + CodePrinter.toString(this));
    }
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return (Boolean) children[0].eval(context) ? children[1].eval(context) : children[2].eval(context);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("(");
    children[0].print(cp);
    cp.append(" ? ");
    children[1].print(cp);
    cp.append(" : ");
    children[2].print(cp);
    cp.append(")");
  }

  @Override
  public Type type() {
    return type;
  }
}
