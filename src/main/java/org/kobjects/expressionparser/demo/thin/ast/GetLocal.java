package org.kobjects.expressionparser.demo.thin.ast;


import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

class GetLocal extends Node {
  String name;
  int index;

  public GetLocal(String name, Type type, int index) {
    super(type);
    this.name = name;
    this.index = index;
  }

  @Override
  public org.kobjects.expressionparser.demo.thin.ast.Expression resolve(ParsingContext context) {
    throw new UnsupportedOperationException("Already resolved");
  }

  @Override
  public Object eval(EvaluationContext context) {
    return context.getLocal(index);
  }

  @Override
  public String toString() {
    return name;
  }
}
