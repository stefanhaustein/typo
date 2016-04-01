package org.kobjects.expressionparser.demo.thinscript.type;

import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

public class ArrayType implements Type {

  Type elementType;
  public ArrayType(Type elementType) {
    this.elementType = elementType;
  }

  @Override
  public String name() {
    return elementType.name() + "[]";
  }

  @Override
  public Type resolveType(ParsingContext context) {
    elementType = elementType.resolveType(context);
    return this;
  }

  @Override
  public boolean assignableFrom(Type type) {
    return false;
  }
}
