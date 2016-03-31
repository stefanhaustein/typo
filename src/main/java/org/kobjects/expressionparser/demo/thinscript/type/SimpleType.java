package org.kobjects.expressionparser.demo.thinscript.type;

import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

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
  public Type resolveType(ParsingContext context) {
    return this;
  }

  @Override
  public String toString() {
    return name;
  }
}
