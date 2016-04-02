package org.kobjects.typo.type;

public class Types {
  public static final Type BOOLEAN = new SimpleType("boolean");
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
    if (value instanceof Double) {
      return NUMBER;
    }
    if (value instanceof String) {
      return STRING;
    }
    throw new IllegalArgumentException("Unrecognized of: "
        + (value == null ? null : value.getClass()) + " of " + value);
  }

  public static Type commonType(Type t1, Type t2) {
    if (t1.assignableFrom(t2)) {
      return t1;
    }
    if (t2.assignableFrom(t1)) {
      return t2;
    }
    return null;
  }
}
