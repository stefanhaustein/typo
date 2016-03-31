package org.kobjects.expressionparser.demo.thin.ast;


import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

class UnresolvedProperty implements Expression {
  final Expression base;
  final String name;

  UnresolvedProperty(Expression base, String name) {
    this.base = base;
    this.name = name;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Expression eval(EvaluationContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAssignable() {
    return false;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    Expression resolvedBase = base.resolve(context);
    if (!(resolvedBase.type() instanceof org.kobjects.expressionparser.demo.thin.statement.Classifier)) {
      throw new RuntimeException("Classifier expected; got: " + resolvedBase.type());
    }
    org.kobjects.expressionparser.demo.thin.statement.Classifier classifier = (org.kobjects.expressionparser.demo.thin.statement.Classifier) resolvedBase.type();

    org.kobjects.expressionparser.demo.thin.statement.Classifier.Member member = classifier.members.get(name);
    if (member == null) {
      throw new RuntimeException("Member '" + name + "' not found in " + classifier);
    }
    return new Property(resolvedBase, member);
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    base.resolveSignatures(context);
  }

  @Override
  public String toString() {
    return base + "." + name;
  }

  @Override
  public Type type() {
    return null;
  }
}
