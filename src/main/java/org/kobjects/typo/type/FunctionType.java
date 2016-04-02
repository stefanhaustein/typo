package org.kobjects.typo.type;


import org.kobjects.typo.parser.ParsingContext;

public class FunctionType implements Type {
  public Type returnType;
  public Parameter[] parameters;

  public FunctionType(Type type, Parameter... parameters) {
    this.returnType = type;
    this.parameters = parameters;
  }

  public void assertSignature(Type[] types, String message) {
    if (types.length != parameters.length) {
      throw new RuntimeException(message + "  " + parameters.length + " parameters expected, but got "
          + types.length);
    }
    for (int i = 0; i < types.length; i++) {
      if (!parameters[i].type.assignableFrom(types[i])) {
        throw new RuntimeException(message + " '" + parameters[i].type.name() + "' expected for parameter "
            + i + " but got " + types[i].name());
      }
    }
  }

  @Override
  public boolean assignableFrom(Type other) {
    return equals(other);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof FunctionType)) {
      return false;
    }
    FunctionType otherF = (FunctionType) other;
    if (!returnType.equals(otherF.returnType)
        || parameters.length != otherF.parameters.length) {
      return false;
    }
    for (int i = 0; i < parameters.length; i++) {
      if (!parameters[i].type.equals(otherF.parameters[i].type)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String name() {
    StringBuilder sb = new StringBuilder("(");
    if (parameters.length > 0) {
      sb.append(parameters[0]);
      for (int i = 1; i < parameters.length; i++) {
        sb.append(", ");
        sb.append(parameters[i]);
      }
    }
    sb.append(" => ").append(returnType.name());
    return sb.toString();
  }

  @Override
  public Type resolve(ParsingContext context) {
    returnType = returnType.resolve(context);
    for (int i = 0; i < parameters.length; i++) {
      parameters[i].type = parameters[i].type.resolve(context);
    }
    return this;
  }

  @Override
  public String toString() {
    return name();
  }

  public static class Parameter {
    public String name;
    public Type type;
    public Parameter(String name, Type type) {
      this.name = name;
      this.type = type;
    }

    public String toString() {
      return name + ": " + type.name();
    }
  }
}
