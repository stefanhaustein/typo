package org.kobjects.typo.expression;


import org.kobjects.typo.type.Type;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;

public class UnresolvedIdentifier extends Expression {
  String name;

  public UnresolvedIdentifier(String name) {
    this.name = name;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object eval(EvaluationContext context) {
    throw new UnsupportedOperationException("Can't eval unresolved identifier '" + name + "'.");
  }

  @Override
  public boolean isAssignable() {
    return false;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append(name);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    if (name.equals("this") && context.self != null) {
      return new This(context.self);
    }
    ParsingContext.LocalDeclaration field = context.resolveField(name);
    if (field != null) {
      return new Variable(field);
    }
    Object o = context.resolveStatic(name);
    if (o != null) {
      return new Literal(o, name);
    }
    throw new RuntimeException("Cannot resolve identifier '" + name + "'.");
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Type type() {
    throw new RuntimeException("Unresolved");
  }

}
