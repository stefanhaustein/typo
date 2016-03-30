package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.Applicable;
import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.FunctionType;
import org.kobjects.expressionparser.demo.thin.type.Type;

public class Function implements Expression, Applicable {
  String name;
  public Parameter[] parameters;
  org.kobjects.expressionparser.demo.thin.type.Type returnType;
  org.kobjects.expressionparser.demo.thin.type.FunctionType type;
  public Statement body;

  Function(String name, Parameter[] parameters, Type returnType, Statement body) {
    this.name = name;
    this.parameters = parameters;
    this.returnType = returnType;
    this.body = body;
  }

  @Override
  public Object apply(EvaluationContext context) {
    return body.eval(context);
  }

  @Override
  public Object eval(EvaluationContext context) {
    return this;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    ParsingContext bodyContext = new ParsingContext(context, context.self);
    for (Parameter param : parameters) {
      bodyContext.declareLocal(param.name, param.type);
    }
    body.resolve(bodyContext);
    return this;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    returnType = returnType.resolveType(context);
    org.kobjects.expressionparser.demo.thin.type.Type[] parameterTypes = new Type[parameters.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      parameterTypes[i] = parameters[i].type.resolveType(context);
      parameters[i].type = parameterTypes[i];
    }
    type = new FunctionType(returnType, parameterTypes);
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
    if (body.kind == Statement.Kind.BLOCK){
      sb.append(body);
    } else {
      sb.append("{");
      sb.append(body);
      sb.append("}");
    }
    return sb.toString();
  }

  @Override
  public FunctionType type() {
    return type;
  }

  public static class Parameter {
    String name;
    Type type;

    public Parameter(String name, Type type) {
      this.name = name;
      this.type = type;
    }

    public String toString() {
      return name + ": " + type;
    }
  }
}
