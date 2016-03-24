package org.kobjects.expressionparser.demo.basic;


class Literal extends Node {
  Object value;

  Literal(Object value) {
    super((Node[]) null);
    this.value = value;
  }

  @Override
  public Object eval() {
    return value;
  }

  @Override
  Class<?> returnType() {
    return value.getClass();
  }

  @Override
  public String toString() {
    if (value != Basic.INVISIBLE_STRING && value instanceof String) {
      return Basic.quote((String) value);
    }
    return Basic.toString(value);
  }
}
