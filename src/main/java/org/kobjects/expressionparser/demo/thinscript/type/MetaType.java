package org.kobjects.expressionparser.demo.thinscript.type;

import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

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
  public Type resolveType(ParsingContext context) {
    return this;
  }
}
