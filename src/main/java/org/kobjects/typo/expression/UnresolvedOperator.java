package org.kobjects.typo.expression;

import org.kobjects.typo.parser.Position;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;
import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.wasm.Operation;

public class UnresolvedOperator extends ExpressionN {
  String name;

  public UnresolvedOperator(Position pos, String name, Expression... children) {
    super(pos, children);
    this.name = name;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("(");
    if (children.length == 1) {
      cp.append(name);
      children[0].print(cp);
    } else {
      children[0].print(cp);
      cp.append(' ').append(name).append(' ');
      children[1].print(cp);
    }
    cp.append(')');
  }

  @Override
  public Expression resolve(ParsingContext context) {
    super.resolve(context);
    boolean allNumber = true;
    // TODO: compute common type instead.
    for (int i = 0; i < children.length; i++) {
      if (!Types.NUMBER.assignableFrom(children[i].type())) {
        allNumber = false;
      }
    }

    if (name.equals("&&") || name.equals("||")) {
      if (children[0].type() != Types.BOOLEAN || children[1].type() != Types.BOOLEAN) {
        throw new RuntimeException("Boolean arguments expected for logical operation.");
      }
      return name.equals("&&") ? new LogicalAnd(pos, children[0], children[1]) : new LogicalOr(pos, children[0], children[1]);
    }

    if (name.equals("+") && children.length == 1) {
      if (!allNumber) {
        throw new RuntimeException("Number expected for unary +");
      }
      return children[0];
    }
    if (name.equals("!")) {
      return new Not(pos, children[0]);
    }
    if (name.startsWith("==")) {
      return new Equals(pos, children[0], children[1]);
    }
    if (name.startsWith("!=")) {
      return new Not(pos, new Equals(pos, children[0], children[1]));
    }
    if (name.startsWith("<") || name.startsWith(">")) {
      return new Compare(pos, name.startsWith("<")
          ? (name.endsWith("=") ? Compare.Op.LE : Compare.Op.LT)
          : (name.endsWith("=") ? Compare.Op.GE : Compare.Op.GT), children[0], children[1]);
    }

    if (name.equals("=")) {
      if (!children[0].isAssignable()) {
        throw new RuntimeException("Cannot assign to " + children[0]);
      }
      return new Assignment(pos, children[0], children[1]);
    }

    if (!allNumber) {
      if (!name.equals("+")) {
        throw new IllegalArgumentException("number arguments expected for " + name);
      }
      return new Concat(pos, children[0], children[1]);
    }

    Operation op;
    switch(name.charAt(0)) {
      case '+': op = Operation.F64Add; break;
      case '-':
        op = children.length == 1 ? Operation.F64Neg : Operation.F64Sub;
        break;
      case '*': op = Operation.F64Mul; break;
      case '/': op = Operation.F64Div; break;
      case '%': op = Operation.F64Mod; break;
      default:
        throw new RuntimeException("Unrecognized operator: " + name);
    }
    return new Operator(pos, op, children);
  }

  @Override
  public Object eval(EvaluationContext context) {
    throw new UnsupportedOperationException("Unresolved");
  }

  @Override
  public Type type() {
    throw new UnsupportedOperationException();
  }
}
