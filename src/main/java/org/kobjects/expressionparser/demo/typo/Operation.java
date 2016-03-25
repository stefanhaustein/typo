package org.kobjects.expressionparser.demo.typo;

public class Operation extends Node {
  Wasm.Op op;

  Operation(Wasm.Op op, Node... children) {
    super(op.type, children);
    this.op = op;
    if (children.length != op.paramTypes.length) {
      throw new IllegalArgumentException("Expected " + op.paramTypes.length + " parameters; got "
          + children.length);
    }
    for (int i = 0; i < children.length; i++) {
      if (children[i].type != op.paramTypes[i]) {
        throw new IllegalArgumentException("Type mismatch for parameter " + i + ": expected");
      }
    }
  }

  public Object eval() {
    switch(op) {
      case F64ABS: return Math.abs(evalF64(0));
      case F64ADD: return evalF64(0) + evalF64(1);
      case F64DIV: return evalF64(0) / evalF64(1);
      case F64MAX: return Math.max(evalF64(0), evalF64(1));
      case F64MIN: return Math.min(evalF64(0), evalF64(1));
      case F64MUL: return evalF64(0) * evalF64(1);
      case F64SUB: return evalF64(0) - evalF64(1);
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
