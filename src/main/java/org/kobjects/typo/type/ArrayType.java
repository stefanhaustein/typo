package org.kobjects.typo.type;

import org.kobjects.typo.parser.ParsingContext;

public class ArrayType implements Type {

  public Type elementType;
  public ArrayType(Type elementType) {
    this.elementType = elementType;
  }

  @Override
  public String name() {
    return elementType.name() + "[]";
  }

  @Override
  public Type resolve(ParsingContext context) {
    elementType = elementType.resolve(context);
    return this;
  }

  @Override
  public boolean assignableFrom(Type type) {
    return false;
  }
}
