package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.Wasm;
import org.kobjects.expressionparser.demo.thin.type.Type;

public class Operator extends Node {
  Wasm.Op op;

  public Operator(Wasm.Op op, org.kobjects.expressionparser.demo.thin.ast.Expression... children) {
    super(Type.NUMBER, children);
    this.op = op;
  }

  @Override
  public org.kobjects.expressionparser.demo.thin.ast.Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved.");
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

  public String toString() {
    StringBuilder sb = new StringBuilder(op.name());
    sb.append('(');
    if (children.length > 0) {
      sb.append(children[0]);
      for (int i = 1; i < children.length; i++) {
        sb.append(", ");
        sb.append(children[i]);
      }
    }
    sb.append(')');
    return sb.toString();
  }
}
