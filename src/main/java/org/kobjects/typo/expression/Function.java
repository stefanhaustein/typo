package org.kobjects.typo.expression;

import org.kobjects.typo.Applicable;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.NamedEntity;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.statement.Block;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.statement.Statement;
import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.type.Type;

public class Function extends Expression implements Applicable, NamedEntity {
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
  public Object eval(EvaluationContext context) {
    return this;
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
    resolveSignatures(context);
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
    if (type == null) {
      returnType = returnType.resolve(context);
      for (int i = 0; i < parameters.length; i++) {
        parameters[i].type = parameters[i].type.resolve(context);
      }
      type = new FunctionType(returnType, parameters);
    }
  }

  @Override
  public FunctionType type() {
    return type;
  }
}
