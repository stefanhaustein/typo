package org.kobjects.expressionparser.demo.typo;

class LocalVariable extends Node {
  String name;
  int index;
  LocalVariable(String name, Type type, int index) {
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
