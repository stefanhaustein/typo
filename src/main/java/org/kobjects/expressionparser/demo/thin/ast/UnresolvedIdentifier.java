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

  public String toString() {
    return name;
  }

  @Override
  public Object eval(EvaluationContext context) {
    throw new UnsupportedOperationException("Can't eval unresolved identifier '" + name + "'.");
  }

  @Override
  public Expression resolve(ParsingContext context) {
    Object o = context.resolve(name);
    if (o == null) {
      throw new RuntimeException("Undeclared variable: " + name);
    }
    if (o instanceof Field) {
      return new GetField((Field) o);
    }
    if (o instanceof Classifier) {
      return new Literal(o);
    }
    throw new RuntimeException("Don't know how to handle " + o + " class " + o.getClass()
        + " for identifier " + name);
  }

  @Override
  public Type type() {
    return null;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
  }
}
