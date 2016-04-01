package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.Applicable;
import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.statement.Block;
import org.kobjects.expressionparser.demo.thinscript.statement.TsClass;
import org.kobjects.expressionparser.demo.thinscript.statement.Statement;
import org.kobjects.expressionparser.demo.thinscript.type.FunctionType;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

public class Function implements Expression, Applicable {
  String name;
  TsClass owner;
  public FunctionType.Parameter[] parameters;
  Type returnType;
  FunctionType type;
  public Statement body;
  public int localCount;

  public Function(TsClass owner, String name, FunctionType.Parameter[] parameters, Type returnType, Statement body) {
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
        cp.append(", ");
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
    for (FunctionType.Parameter param : parameters) {
      bodyContext.declareLocal(param.name, param.type);
    }
    body.resolve(bodyContext);
    this.localCount = bodyContext.locals.size();
    return this;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    returnType = returnType.resolveType(context);
    for (int i = 0; i < parameters.length; i++) {
      parameters[i].type = parameters[i].type.resolveType(context);
    }
    type = new FunctionType(returnType, parameters);
    body.resolveSignatures(context);
  }

  @Override
  public FunctionType type() {
    return type;
  }
}
