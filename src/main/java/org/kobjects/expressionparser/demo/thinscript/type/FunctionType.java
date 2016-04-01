package org.kobjects.expressionparser.demo.thinscript.type;


import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

import java.util.Arrays;

public class FunctionType implements Type {
  public Type returnType;
  public Type[] parameterTypes;

  public FunctionType(Type type, Type... parameterTypes) {
    this.returnType = type;
    this.parameterTypes = parameterTypes;
  }

  @Override
  public String name() {
    return Arrays.toString(parameterTypes) + " => " + returnType;
  }

  @Override
  public Type resolveType(ParsingContext context) {
    return this;
  }

  @Override
  public String toString() {
    return name();
  }

  @Override
  public boolean assignableFrom(Type other) {
    return equals(other);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof FunctionType)) {
      return false;
    }
    FunctionType otherF = (FunctionType) other;
    if (!returnType.equals(otherF.returnType)
        || parameterTypes.length != otherF.parameterTypes.length) {
      return false;
    }
    for (int i = 0; i < parameterTypes.length; i++) {
      if (!parameterTypes[i].equals(otherF.parameterTypes[i])) {
        return false;
      }
    }
    return true;
  }

  public void assertSignature(Type[] types, String message) {
    if (types.length != parameterTypes.length) {
      throw new RuntimeException(message + "  " + parameterTypes.length + " parameters expected, but got "
          + types.length);
    }
    for (int i = 0; i < types.length; i++) {
      if (!parameterTypes[i].assignableFrom(types[i])) {
        throw new RuntimeException(message + " '" + parameterTypes[i].name() + "' expected for parameter "
            + i + " but got " + types[i].name());
      }
    }
  }
}
