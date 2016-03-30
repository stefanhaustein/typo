package org.kobjects.expressionparser.demo.thin.type;

public class Types {
  public static Type typeOf(Object value) {
    if (value instanceof Typed) {
      return ((Typed) value).type();
    }
    if (value instanceof Type) {
      return new MetaType((Type) value);
    }
    if (value instanceof Double) {
      return Type.NUMBER;
    }
    if (value instanceof String) {
      return Type.STRING;
    }
    throw new IllegalArgumentException("Unrecognized of: "
        + (value == null ? null : value.getClass()) + " of " + value);
  }
}
