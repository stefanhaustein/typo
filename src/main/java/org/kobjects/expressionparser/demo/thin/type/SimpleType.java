package org.kobjects.expressionparser.demo.thin.type;

public class SimpleType implements Type {

  final String name;

  SimpleType(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Type resolveType(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    return this;
  }

  @Override
  public String toString() {
    return name;
  }
}
