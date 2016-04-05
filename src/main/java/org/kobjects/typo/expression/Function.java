package org.kobjects.typo.expression;

import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.NamedEntity;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.Instance;
import org.kobjects.typo.statement.Block;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.statement.Statement;
import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Typed;
import org.kobjects.typo.type.UnresolvedType;

import java.util.ArrayList;
import java.util.List;

public class Function extends Expression implements Typed, NamedEntity {
  String name;
  TsClass owner;
  public FunctionType.Parameter[] parameters;
  Type returnType;
  public FunctionType type;
  public Statement body;
  public int localCount;
  public int thisIndex = -1;
  public List<Closure> closures = new ArrayList<>();
  Object[] capture;

  public Function(TsClass owner, String name, FunctionType.Parameter[] parameters, Type returnType, Statement body) {
    this.owner = owner;
    this.name = name;
    this.parameters = parameters;
    this.returnType = returnType;
    this.body = body;
  }

  public Object apply(EvaluationContext context) {
    return body.eval(context);
  }

  public EvaluationContext createContext(Object self) {
    EvaluationContext context = new EvaluationContext(localCount);
    if (thisIndex != -1) {
      if (self == null) {
        throw new RuntimeException("this must not be null for " + toString());
      }
      context.setLocal(thisIndex, self);
    }
    // TODO: Copying shouldn't be necessary -- add some form of special access instead?
    for (int i = 0; i < closures.size(); i++) {
      context.setLocal(closures.get(i).target.localIndex, capture[i]);
    }
    return context;
  }

  @Override
  public void declareStatics(ParsingContext context) {
    context.declareStatic(name(), this);
  }


  @Override
  public Object eval(EvaluationContext context) {
    if (closures.size() > 0) {
      capture = new Object[closures.size()];
      for (int i = 0; i < capture.length; i++) {
        capture[i] = context.getLocal(closures.get(i).source.localIndex);
      }
    }
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
    ParsingContext bodyContext = new ParsingContext(context, this);
    for (FunctionType.Parameter param : parameters) {
      bodyContext.declareLocal(param.name, param.type);
    }
    if (owner != null) {
      thisIndex = bodyContext.declareLocal("this", owner).localIndex;
    }
    body.resolve(bodyContext);
    localCount = bodyContext.locals.size();
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

  public static class Closure {
    ParsingContext.LocalDeclaration source;
    ParsingContext.LocalDeclaration target;
    public Closure(ParsingContext.LocalDeclaration source, ParsingContext.LocalDeclaration target) {
      this.source = source;
      this.target = target;
    }
  }
}
