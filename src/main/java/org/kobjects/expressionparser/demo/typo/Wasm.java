package org.kobjects.expressionparser.demo.typo;


public class Wasm {
  enum Type {
    F64
  }
  enum Op {
    F64Add(0x89, Type.F64, Type.F64, Type.F64),
    F64Sub(0x8a, Type.F64, Type.F64, Type.F64),
    F64Mul(0x8b, Type.F64, Type.F64, Type.F64),
    F64Div(0x8c, Type.F64, Type.F64, Type.F64),
    F64Min(0x8d, Type.F64, Type.F64, Type.F64),
    F64Max(0x8e, Type.F64, Type.F64, Type.F64),
    F64Abs(0x8f, Type.F64, Type.F64);

    int opCode;
    Type type;
    Type[] paramTypes;

    Op(int opcode, Type type, Type... paramTypes) {
      this.opCode = opcode;
      this.type = type;
      this.paramTypes = paramTypes;
    }
  }
}
