package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

public abstract class Node implements Expression {

  Type type;
  public final Expression[] children;

  Node(Type type, Expression... children) {
    this.type = type;
    this.children = children;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    throw new UnsupportedOperationException();
  }

  int evalInt(EvaluationContext context, int index) {
    return (Integer) children[index].eval(context);
  }

  double evalNumber(EvaluationContext context, int index) {
    return ((Number) children[index].eval(context)).doubleValue();
  }

  @Override
  public boolean isAssignable() {
    return false;
  }

  void resolveChildren(ParsingContext context) {
    for (int i = 0; i < children.length; i++) {
      children[i] = children[i].resolve(context);
    }
  }

  public void resolveSignatures(ParsingContext context) {
    for (Expression child: children) {
      child.resolveSignatures(context);
    }
  }

  public Type[] childTypes() {
    Type[] result = new Type[children.length];
    for (int i = 0; i < children.length; i++) {
      result[i] = children[i].type();
    }
    return result;
  }

  @Override
  public String toString() {
    CodePrinter cp = new CodePrinter();
    print(cp);
    return cp.toString();
  }

  public Type type() {
    return type;
  }

}
