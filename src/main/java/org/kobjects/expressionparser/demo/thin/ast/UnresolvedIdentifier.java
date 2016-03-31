package org.kobjects.expressionparser.demo.thin.ast;


import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.Field;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

class UnresolvedIdentifier implements Expression {
  String name;

  UnresolvedIdentifier(String name) {
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
  public Expression resolve(ParsingContext context) {
    Field field = context.resolveField(name);
    if (field != null) {
      return new Variable(field);
    }
    Object o = context.resolveStatic(name);
    if (o instanceof Expression) {
      return (Expression) o;
    }
    if (o != null) {
      return new Literal(o);
    }
    throw new RuntimeException("Cannot resolve identifier '" + name + "'.");
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Type type() {
    return null;
  }

}
