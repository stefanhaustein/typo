package org.kobjects.expressionparser.demo.thin.ast;


import org.kobjects.expressionparser.demo.thin.EvaluationContext;
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
    if (o instanceof ParsingContext.LocalDeclaration) {
      ParsingContext.LocalDeclaration var = (ParsingContext.LocalDeclaration) o;
      return new org.kobjects.expressionparser.demo.thin.ast.GetLocal(
          var.name, var.type, var.localIndex);
    }
    if (o instanceof org.kobjects.expressionparser.demo.thin.ast.Classifier.Member) {
      org.kobjects.expressionparser.demo.thin.ast.Classifier.Member member = (org.kobjects.expressionparser.demo.thin.ast.Classifier.Member) o;
      return new org.kobjects.expressionparser.demo.thin.ast.GetProperty(new org.kobjects.expressionparser.demo.thin.ast.This(), member);
    }
    if (o instanceof org.kobjects.expressionparser.demo.thin.ast.Classifier) {
      return new org.kobjects.expressionparser.demo.thin.ast.Literal(o);
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
