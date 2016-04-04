package org.kobjects.typo.expression;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.ArrayType;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;

import java.util.List;

public class ArrayAccess extends ExpressionN {
  Type type;

  public ArrayAccess(Expression base, Expression index) {
    super(base, index);
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    List list = (List) children[0].eval(context);
    Number index = (Number) children[1].eval(context);
    list.set(index.intValue(), value);
  }

  @Override
  public Object eval(EvaluationContext context) {
    List<?> list = (List<?>) children[0].eval(context);
    Number index = (Number) children[1].eval(context);
    return (list.get(index.intValue()));
  }

  @Override
  public boolean isAssignable() {
    return true;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    super.resolve(context);
    if (!(children[0].type() instanceof ArrayType)) {
      throw new RuntimeException("Base must be an array.");
    }
    if (children[1].type() != Types.NUMBER) {
      throw new RuntimeException("Index must be numeric");
    }
    type = ((ArrayType) children[0].type()).elementType;
    return this;
  }

  @Override
  public Type type() {
    return type;
  }

  @Override
  public void print(CodePrinter cp) {
    children[0].print(cp);
    cp.append('[');
    children[1].print(cp);
    cp.append(']');
  }
}
