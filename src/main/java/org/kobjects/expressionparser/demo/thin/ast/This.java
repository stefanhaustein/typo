package org.kobjects.expressionparser.demo.thin.ast;

;import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;

public class This extends Node {
  public This() {
    super(null);
  }

  @Override
  public Object eval(EvaluationContext context) {
    return context.self;
  }

  @Override
  public Node resolve(ParsingContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "this";
  }
}
