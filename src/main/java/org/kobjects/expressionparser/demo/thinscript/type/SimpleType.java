package org.kobjects.expressionparser.demo.thinscript.type;

import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

class SimpleType implements Type {

  final String name;

  SimpleType(String name) {
    this.name = name;
  }

  @Override
  public boolean assignableFrom(Type other) {
    if (this == Types.NUMBER && other == Types.INT) {
      return true;
    }
    return equals(other);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof SimpleType)) {
      return false;
    }
    return name.equals(((SimpleType) other).name);
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
