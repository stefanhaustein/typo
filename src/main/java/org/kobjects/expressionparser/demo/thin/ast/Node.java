package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

abstract class Node implements Expression {

  Type type;
  final Expression[] children;

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

  Node(Type type, Expression... children) {
    this.type = type;
    this.children = children;
  }

  double evalF64(EvaluationContext context, int index) {
    return (Double) children[index].eval(context);
  }

  public Type type() {
    return type;
  }

}
