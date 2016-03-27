package org.kobjects.expressionparser.demo.typo;

class This extends Node {
  This(Type type) {
    super(type);
  }

  @Override
  Object eval(EvaluationContext context) {
    return context.self;
  }

  @Override
  public String toString() {
    return "this";
  }
}
