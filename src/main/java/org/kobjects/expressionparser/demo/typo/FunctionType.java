package org.kobjects.expressionparser.demo.typo;


import java.util.Arrays;

class FunctionType extends Type {
  Type type;
  Type[] parameterTypes;

  FunctionType(Type type, Type... parameterTypes) {
    super(Arrays.toString(parameterTypes) + " => " + type, null);
    this.type = type;
    this.parameterTypes = parameterTypes;
  }
}
