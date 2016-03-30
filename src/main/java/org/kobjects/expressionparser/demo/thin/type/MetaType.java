package org.kobjects.expressionparser.demo.thin.type;

public class MetaType implements Type {
  Type of;

  public MetaType(Type type) {
    this.of = type;
  }

  @Override
  public String name() {
    return "meta<" + of + ">";
  }

  @Override
  public Type resolveType(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    return this;
  }
}
