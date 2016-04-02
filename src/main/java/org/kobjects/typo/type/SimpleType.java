package org.kobjects.typo.type;

import org.kobjects.typo.parser.ParsingContext;

class SimpleType implements Type {

  final String name;

  SimpleType(String name) {
    this.name = name;
  }

  @Override
  public boolean assignableFrom(Type other) {
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
  public Type resolve(ParsingContext context) {
    return this;
  }
  @Override
  public String toString() {
    return name;
  }
}
