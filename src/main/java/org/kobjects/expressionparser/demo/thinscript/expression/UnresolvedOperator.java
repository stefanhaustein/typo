package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Types;
import org.kobjects.expressionparser.demo.thinscript.wasm.Operation;

public class UnresolvedOperator extends Node {
  String name;

  public UnresolvedOperator(String name, Expression... children) {
    super(null, children);
    this.name = name;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("(");
    children[0].print(cp);
    cp.append(' ').append(name).append(' ');
    children[1].print(cp);
    cp.append(')');
  }

  @Override
  public Expression resolve(ParsingContext context) {
    boolean allNumber = true;
    Expression[] resolved = new Expression[children.length];
    for (int i = 0; i < children.length; i++) {
      resolved[i] = children[i].resolve(context);
      if (resolved[i].type() != Types.NUMBER) {
        allNumber = false;
      }
    }

    if (name.equals("=")) {
      if (!resolved[0].isAssignable()) {
        throw new RuntimeException("Cannot assign to " + resolved[0]);
      }
      return new Assignment(resolved[0], resolved[1]);
    }

    if (!allNumber) {
      if (!name.equals("+")) {
        throw new IllegalArgumentException("number arguments expected for " + name);
      }
      return new Concat(resolved[0], resolved[1]);
    }

    Operation op;
    switch(name.charAt(0)) {
      case '+': op = Operation.F64Add; break;
      case '-': op = Operation.F64Sub; break;
      case '*': op = Operation.F64Mul; break;
      case '/': op = Operation.F64Div; break;
      default:
        throw new RuntimeException("Unrecognized operator: " + name);
    }
    return new Operator(op, resolved[0], resolved[1]);
  }

  @Override
  public Object eval(EvaluationContext context) {
    throw new UnsupportedOperationException("Unresolved");
  }

}
