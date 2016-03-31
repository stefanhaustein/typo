package org.kobjects.expressionparser.demo.thinscript.type;

public class Types {
  public static final Type BOOLEAN = new SimpleType("boolean");
  public static final Type INT = new SimpleType("int");
  public static final Type NUMBER = new SimpleType("number");
  public static final Type STRING = new SimpleType("string");
  public static final Type VOID = new SimpleType("void");
  public static final Type NULL = new SimpleType("(null)");

  public static Type typeOf(Object value) {
    if (value == null) {
      return NULL;
    }
    if (value instanceof Typed) {
      return ((Typed) value).type();
    }
    if (value instanceof Type) {
      return new MetaType((Type) value);
    }
    if (value instanceof Boolean) {
      return BOOLEAN;
    }
    if (value instanceof Integer) {
      return INT;
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
}
