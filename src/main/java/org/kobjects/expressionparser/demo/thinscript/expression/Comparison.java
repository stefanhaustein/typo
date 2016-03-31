package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Types;

public class Comparison extends Node {
  boolean eq;
  Comparison(boolean eq, Expression left, Expression right) {
    super(Types.BOOLEAN, left, right);
    this.eq = eq;
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
      return eq ? left == right : left != right;
    }
    if (left instanceof Double || right instanceof Double) {
      if (!(left instanceof Number) || !(right instanceof Number)) {
        return false;
      }
      return ((Number) left).doubleValue() == ((Number) right).doubleValue() ? eq : !eq;
    }
    return left.equals(right) ? eq : !eq;
  }

  @Override
  public void print(CodePrinter cp) {

  }
}
