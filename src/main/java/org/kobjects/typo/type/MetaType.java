package org.kobjects.typo.type;

import org.kobjects.typo.parser.ParsingContext;

public class MetaType implements Type {
  public Type of;

  public MetaType(Type type) {
    this.of = type;
  }

  @Override
  public boolean assignableFrom(Type other) {
    return equals(other);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof MetaType)) {
      return false;
    }
    return of.equals(((MetaType) other).of);
  }

  @Override
  public String name() {
    return "meta<" + of + ">";
  }

  @Override
  public Type resolve(ParsingContext context) {
    return this;
  }
}
