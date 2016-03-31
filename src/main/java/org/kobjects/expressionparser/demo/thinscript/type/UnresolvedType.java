package org.kobjects.expressionparser.demo.thinscript.type;

import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

public class UnresolvedType implements Type {
  private final String name;

  public UnresolvedType(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return "unresolved<" + name + ">";
  }

  @Override
  public Type resolveType(ParsingContext context) {
    Object resolved = context.resolveStatic(name);
    if (!(resolved instanceof Type)) {
      throw new RuntimeException("Not a type: " + name);
    }
    return (Type) resolved;
  }

  @Override
  public String toString() {
    return name();
  }
}
