package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Types;
import org.kobjects.expressionparser.demo.thinscript.wasm.Operation;

class Operator extends Node {
  Operation op;

  public Operator(Operation op, Expression... children) {
    super(Types.NUMBER, children);
    this.op = op;
  }

  public Object eval(EvaluationContext ctx) {
    switch(op) {
      case F64Abs: return Math.abs(evalF64(ctx, 0));
      case F64Add: return evalF64(ctx, 0) + evalF64(ctx, 1);
      case F64Div: return evalF64(ctx, 0) / evalF64(ctx, 1);
      case F64Max: return Math.max(evalF64(ctx, 0), evalF64(ctx, 1));
      case F64Min: return Math.min(evalF64(ctx, 0), evalF64(ctx, 1));
      case F64Mul: return evalF64(ctx, 0) * evalF64(ctx, 1);
      case F64Sub: return evalF64(ctx, 0) - evalF64(ctx, 1);
      default: throw new UnsupportedOperationException(op.name());
    }
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append(op);
    cp.append('(');
    if (children.length > 0) {
      children[0].print(cp);
      for (int i = 1; i < children.length; i++) {
        cp.append(", ");
        children[i].print(cp);
      }
    }
    cp.append(')');
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved.");
  }
}
