package org.kobjects.expressionparser.demo.thin.ast;


public class Function implements Expression, org.kobjects.expressionparser.demo.thin.Applicable {
  String name;
  public Parameter[] parameters;
  org.kobjects.expressionparser.demo.thin.type.Type returnType;
  org.kobjects.expressionparser.demo.thin.type.FunctionType type;
  public Statement body;

  Function(String name, Parameter[] parameters, org.kobjects.expressionparser.demo.thin.type.Type returnType, Statement body) {
    this.name = name;
    this.parameters = parameters;
    this.returnType = returnType;
    this.body = body;
  }

  @Override
  public Object apply(org.kobjects.expressionparser.demo.thin.EvaluationContext context) {
    return body.eval(context);
  }

  @Override
  public Object eval(org.kobjects.expressionparser.demo.thin.EvaluationContext context) {
    return new org.kobjects.expressionparser.demo.thin.EvaluationContext(context.self, this);
  }

  @Override
  public Expression resolve(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    org.kobjects.expressionparser.demo.thin.ParsingContext bodyContext = new org.kobjects.expressionparser.demo.thin.ParsingContext(context.self);
    for (Parameter param : parameters) {
      bodyContext.addLocal(param.name, param.type);
    }
    body.resolve(bodyContext);
    return this;
  }

  @Override
  public void resolveSignatures(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    returnType = returnType.resolveType(context);
    org.kobjects.expressionparser.demo.thin.type.Type[] parameterTypes = new org.kobjects.expressionparser.demo.thin.type.Type[parameters.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      parameterTypes[i] = parameters[i].type.resolveType(context);
      parameters[i].type = parameterTypes[i];
    }
    type = new org.kobjects.expressionparser.demo.thin.type.FunctionType(returnType, parameterTypes);
    body.resolveSignatures(context);
  }


  public String toString() {
    StringBuilder sb = new StringBuilder("function ");
    if (name != null) {
      sb.append(name);
    }
    sb.append('(');
    if (parameters.length > 0) {
      sb.append(parameters[0]);
      for (int i = 1; i < parameters.length; i++) {
        sb.append(parameters[i]);
      }
    }
    sb.append("):");
    sb.append(returnType);
    sb.append(" ");
    sb.append(body.toString());
    return sb.toString();
  }

  @Override
  public org.kobjects.expressionparser.demo.thin.type.FunctionType type() {
    return type;
  }

  public static class Parameter {
    String name;
    org.kobjects.expressionparser.demo.thin.type.Type type;

    public Parameter(String name, org.kobjects.expressionparser.demo.thin.type.Type type) {
      this.name = name;
      this.type = type;
    }

    public String toString() {
      return name + ": " + type;
    }
  }
}
