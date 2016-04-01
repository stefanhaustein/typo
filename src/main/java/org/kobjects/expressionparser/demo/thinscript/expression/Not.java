package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Types;

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
