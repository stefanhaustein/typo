package org.kobjects.typo.expression;

import org.kobjects.typo.type.Types;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;

class Concat extends Node {

  public Concat(Expression left, Expression right) {
    super(Types.STRING, left, right);
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
  public Expression resolve(ParsingContext context) {
    resolveChildren(context);
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return "" + children[0].eval(context) + children[1].eval(context);
  }

}
