package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Types;

class Concat extends Node {

  public Concat(Expression left, Expression right) {
    super(Types.STRING, left, right);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("concat(").append(children[0]).append(children[1]).append(')');
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
