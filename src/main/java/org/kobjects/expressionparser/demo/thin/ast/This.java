package org.kobjects.expressionparser.demo.thin.ast;

;

public class This extends Node {
  public This() {
    super(null);
  }

  @Override
  public Object eval(org.kobjects.expressionparser.demo.thin.EvaluationContext context) {
    return context.self;
  }

  @Override
  public Node resolve(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "this";
  }
}
