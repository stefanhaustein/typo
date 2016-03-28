package org.kobjects.expressionparser.demo.typo;

class GetLocal extends Node {
  String name;
  int index;
  GetLocal(String name, Type type, int index) {
    super(type);
    this.name = name;
    this.index = index;
  }

  @Override
  Object eval(EvaluationContext context) {
    return context.getLocal(index);
  }

  @Override
  public String toString() {
    return name;
  }
}
