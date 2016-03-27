package org.kobjects.expressionparser.demo.typo;

class Type {
  static Type VOID = new Type("void", null);
  static Type NONE = new Type("N/A", null);
  static Type NUMBER = new Type("number", Wasm.Type.F64);
  static Type STRING = new Type("string", null);

  String name;
  Wasm.Type wasmType;

  Type(String name, Wasm.Type wasmType) {
    this.name = name;
    this.wasmType = wasmType;
  }

  static class Meta extends Type {
    Type type;
    Meta(Type type) {
      super("Meta<" + type.name + ">", null);
      this.type = type;
    }
  }

  @Override
  public String toString() {
    return name;
  }

  public static Type fromWasm(Wasm.Type type) {
    if (type == Wasm.Type.F64) {
      return NUMBER;
    }
    throw new IllegalArgumentException("Unmapped WASM type: " + type);
  }

  public static Type of(Object value) {
    if (value instanceof Typed) {
      return ((Typed) value).type();
    }
    if (value instanceof Type) {
      return new Type("MetaType<" + value + ">", null);
    }
    if (value instanceof Double) {
      return NUMBER;
    }
    if (value instanceof String) {
      return STRING;
    }
    throw new IllegalArgumentException("Unrecognized type: "
        + (value == null ? null : value.getClass()) + " of " + value);
  }
}
