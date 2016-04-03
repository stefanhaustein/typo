package org.kobjects.typo.wasm;

public enum Operation {
  F64Add(0x89, Type.F64, Type.F64, Type.F64),
  F64Sub(0x8a, Type.F64, Type.F64, Type.F64),
  F64Mul(0x8b, Type.F64, Type.F64, Type.F64),
  F64Div(0x8c, Type.F64, Type.F64, Type.F64),
  F64Min(0x8d, Type.F64, Type.F64, Type.F64),
  F64Max(0x8e, Type.F64, Type.F64, Type.F64),
  F64Abs(0x8f, Type.F64, Type.F64),
  F64Neg(0x90, Type.F64, Type.F64),
  F64Mod(265, Type.F64, Type.F64, Type.F64);


  int code;
  Type type;
  public Type[] paramTypes;

  Operation(int code, Type type, Type... paramTypes) {
    this.code = code;
    this.type = type;
    this.paramTypes = paramTypes;
  }
}
