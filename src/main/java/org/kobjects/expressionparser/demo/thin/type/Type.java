package org.kobjects.expressionparser.demo.thin.type;

public interface Type {
  static Type of(Object value) {
    if (value instanceof Typed) {
      return ((Typed) value).type();
    }
    if (value instanceof Type) {
      return new MetaType((Type) value);
    }
    if (value instanceof Double) {
      return NUMBER;
    }
    if (value instanceof String) {
      return STRING;
    }
    throw new IllegalArgumentException("Unrecognized of: "
        + (value == null ? null : value.getClass()) + " of " + value);
  }

  Type VOID = new SimpleType("void");
  Type NONE = new SimpleType("N/A");
  Type NUMBER = new SimpleType("number");
  Type STRING = new SimpleType("string");

  String name();

  Type resolveType(org.kobjects.expressionparser.demo.thin.ParsingContext context);

}
