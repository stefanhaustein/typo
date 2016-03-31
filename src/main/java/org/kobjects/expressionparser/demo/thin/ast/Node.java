package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

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

  double evalF64(EvaluationContext context, int index) {
    return (Double) children[index].eval(context);
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


  public Type type() {
    return type;
  }

}
