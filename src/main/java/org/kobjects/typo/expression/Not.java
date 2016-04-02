package org.kobjects.typo.expression;

import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.type.Types;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;

public class Not extends Node {
  Not(Expression expr) {
    super(Types.BOOLEAN, expr);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    super.resolveChildren(context);
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return !(Boolean) children[0].eval(context);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("(!");
    children[0].print(cp);
    cp.append(")");
  }
}
