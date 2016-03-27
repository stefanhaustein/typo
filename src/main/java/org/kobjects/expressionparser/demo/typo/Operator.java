package org.kobjects.expressionparser.demo.typo;

class Operator extends Node {
  Wasm.Op op;

  Operator(Wasm.Op op, Node... children) {
    super(Type.fromWasm(op.type), children);
    this.op = op;
    if (children.length != op.paramTypes.length) {
      throw new IllegalArgumentException("Expected " + op.paramTypes.length + " parameters; got "
          + children.length);
    }
    for (int i = 0; i < children.length; i++) {
      if (children[i].type.wasmType != op.paramTypes[i]) {
        throw new IllegalArgumentException("Type mismatch for parameter " + i + ": expected");
      }
    }
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
