package org.kobjects.expressionparser.demo.thin.ast;


import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;

class UnresolvedProperty extends Node {

  private final String name;

  UnresolvedProperty(Expression base, String name) {
    super(null, base);
    this.name = name;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new RuntimeException("NYI");
  }

  @Override
  public Expression eval(EvaluationContext context) {
    throw new RuntimeException("NYI");
  }

  public String toString() {
    return children[0] + "." + name;
  }
}
