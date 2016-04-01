package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Types;

public class Compare extends Node {
  enum Op {LT, LE, GT, GE};
  Op op;

  Compare(Op op, Expression left, Expression right) {
    super(Types.BOOLEAN, left, right);
    this.op = op;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new RuntimeException("Resolved already.");
  }

  @Override
  public Object eval(EvaluationContext context) {
    Object left = children[0].eval(context);
    Object right = children[1].eval(context);
    int result = (left instanceof Number)
        ? Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue())
        : ((Comparable) left).compareTo(right);
    switch (op) {
      case LT: return result < 0;
      case LE: return result <= 0;
      case GT: return result > 0;
      case GE: return result >= 0;
      default:
        throw new RuntimeException("Unknown: " + op);
    }
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("(");
    children[0].print(cp);
    switch(op) {
      case GE: cp.append(" >= "); break;
      case GT: cp.append(" > "); break;
      case LE: cp.append(" <= "); break;
      case LT: cp.append(" < "); break;
    }
    children[1].print(cp);
    cp.append(")");
  }
}
