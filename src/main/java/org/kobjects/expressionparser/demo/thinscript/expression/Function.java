package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.Applicable;
import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.statement.Block;
import org.kobjects.expressionparser.demo.thinscript.statement.Classifier;
import org.kobjects.expressionparser.demo.thinscript.statement.Statement;
import org.kobjects.expressionparser.demo.thinscript.type.FunctionType;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

public class Function implements Expression, Applicable {
  String name;
  Classifier owner;
  public Parameter[] parameters;
  Type returnType;
  FunctionType type;
  public Statement body;

  public Function(Classifier owner, String name, Parameter[] parameters, Type returnType, Statement body) {
    this.owner = owner;
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
  public void assign(EvaluationContext context, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object eval(EvaluationContext context) {
    return this;
  }

  @Override
  public boolean isAssignable() {
    return false;
  }

  public String name() {
    return name;
  }

  @Override
  public void print(CodePrinter cp) {
    if (owner == null) {
      cp.append("function ");
    }
    if (name != null) {
      cp.append(name);
    }
    cp.append('(');
    if (parameters.length > 0) {
      cp.append(parameters[0]);
      for (int i = 1; i < parameters.length; i++) {
        cp.append(parameters[i]);
      }
    }
    cp.append(")");
    if (owner == null || this != owner.constructor) {
      cp.append(": ");
      cp.append(returnType.name());
    }
    cp.append(' ');
    if (body instanceof Block){
      body.print(cp);
    } else {
      cp.append("{");
      cp.indent();
      cp.newLine();
      body.print(cp);
      cp.outdent();
      cp.newLine();
      cp.append("}");
    }
  }

  @Override
  public Function resolve(ParsingContext context) {
    ParsingContext bodyContext = new ParsingContext(context, owner);
    for (Parameter param : parameters) {
      bodyContext.declareLocal(param.name, param.type);
    }
    body.resolve(bodyContext);
    return this;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    returnType = returnType.resolveType(context);
    Type[] parameterTypes = new Type[parameters.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      parameterTypes[i] = parameters[i].type.resolveType(context);
      parameters[i].type = parameterTypes[i];
    }
    type = new FunctionType(returnType, parameterTypes);
    body.resolveSignatures(context);
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
