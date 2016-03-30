package org.kobjects.expressionparser.demo.thin.type;


import org.kobjects.expressionparser.demo.thin.ParsingContext;

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
}
