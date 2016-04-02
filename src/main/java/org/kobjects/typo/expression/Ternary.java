package org.kobjects.typo.expression;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.Types;

public class Ternary extends Node {
  public Ternary(Expression condition, Expression ifBranch, Expression elseBranch) {
    super(null, condition, ifBranch, elseBranch);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    resolveChildren(context);
    this.type = Types.commonType(children[1].type(), children[2].type());
    if (this.type == null) {
      throw new RuntimeException("Can't find a common type for "
          + children[1].type().name() + " and " + children[2].type().name());
    }
    if (children[0].type() != Types.BOOLEAN) {
      throw new RuntimeException("Ternary condition must be boolean.");
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
}
