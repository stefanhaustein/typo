package org.kobjects.expressionparser.demo.typo;

class Function implements Applicable {
  FunctionType type;
  Parameter[] parameters;
  Statement[] body;

  Function(Type returnType, Parameter[] parameters, Statement... body) {
    this.parameters = parameters;
    this.body = body;

    Type[] parameterTypes = new Type[parameters.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      parameterTypes[i] = parameters[i].type;
    }
    this.type = new FunctionType(returnType, parameterTypes);
  }

  public Object apply(EvaluationContext context) {
    for (int i = 0; i < body.length; i++) {
      Object result = body[i].eval(context);
      if (result != Statement.NO_RETURN) {
        return result;
      }
    }
    return null;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("function (");
    if (parameters.length > 0) {
      sb.append(parameters[0]);
      for (int i = 1; i < parameters.length; i++) {
        sb.append(parameters[i]);
      }
    }
    sb.append(") {");
    for (Statement statement: body) {
      sb.append(statement.toString());
      sb.append(' ');
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public FunctionType type() {
    return type;
  }

  static class Parameter {
    String name;
    Type type;
    Parameter(String name, Type type) {
      this.name = name;
      this.type = type;
    }
    @Override
    public String toString() {
      return name + ": " + type;
    }
  }
}
