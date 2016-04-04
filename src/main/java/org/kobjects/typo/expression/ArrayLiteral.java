package org.kobjects.typo.expression;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.ArrayType;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;

import java.util.ArrayList;

public class ArrayLiteral extends ExpressionN {
  ArrayType type;
  public ArrayLiteral(Expression[] elements) {
    super(elements);
  }

  @Override
  public Object eval(EvaluationContext context) {
    ArrayList<Object> list = new ArrayList<>(children.length);
    for (Expression child: children) {
      list.add(child.eval(context));
    }
    return list;
  }

  @Override
  public Type type() {
    return type;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append('[');
    if (children.length > 0) {
      children[0].print(cp);
      for (int i = 1; i < children.length; i++) {
        cp.append(", ");
        children[i].print(cp);
      }
    }
    cp.append(']');
  }

  @Override
  public ArrayLiteral resolve(ParsingContext context) {
    super.resolve(context);
    Type elementType;
    if (children.length == 0) {
      elementType = Types.ANY;
    } else {
      elementType = children[0].type();
      for (int i = 1; i < children.length; i++) {
        elementType = Types.commonType(elementType, children[i].type());
      }
    }
    type = new ArrayType(elementType);
    return this;
  }
}
