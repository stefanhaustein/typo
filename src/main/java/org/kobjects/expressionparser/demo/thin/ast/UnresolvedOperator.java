package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

class UnresolvedOperator extends Node {
  String name;

  UnresolvedOperator(String name, Expression... children) {
    super(null, children);
    this.name = name;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    boolean allNumber = true;
    Expression[] resolved = new Expression[children.length];
    for (int i = 0; i < children.length; i++) {
      resolved[i] = children[i].resolve(context);
      if (resolved[i].type() != Type.NUMBER) {
        allNumber = false;
      }
    }

    if (!allNumber) {
      if (!name.equals("+")) {
        throw new IllegalArgumentException("number arguments expected for " + name);
      }
      return new Concat(resolved[0], resolved[1]);
    }

    org.kobjects.expressionparser.demo.thin.Wasm.Op op;
    switch(name.charAt(0)) {
      case '+': op = org.kobjects.expressionparser.demo.thin.Wasm.Op.F64Add; break;
      case '-': op = org.kobjects.expressionparser.demo.thin.Wasm.Op.F64Sub; break;
      case '*': op = org.kobjects.expressionparser.demo.thin.Wasm.Op.F64Mul; break;
      case '/': op = org.kobjects.expressionparser.demo.thin.Wasm.Op.F64Div; break;
      default:
        throw new RuntimeException("Unrecognized operator: " + name);
    }
    return new Operator(op, resolved[0], resolved[1]);
  }

  @Override
  public Object eval(EvaluationContext context) {
    throw new UnsupportedOperationException("Unresolved");
  }

  public String toString() {
    return "(" + children[0] + ' ' + name + ' ' + children[1] + ')';
  }
}
