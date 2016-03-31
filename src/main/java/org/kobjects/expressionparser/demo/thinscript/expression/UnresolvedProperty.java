package org.kobjects.expressionparser.demo.thinscript.expression;


import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.statement.Classifier;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

public class UnresolvedProperty implements Expression {
  final Expression base;
  final String name;

  public UnresolvedProperty(Expression base, String name) {
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
  public void print(CodePrinter cp) {
    cp.append(base).append('.').append(name);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    Expression resolvedBase = base.resolve(context);
    if (!(resolvedBase.type() instanceof Classifier)) {
      throw new RuntimeException("Classifier expected; got: " + resolvedBase.type());
    }
    Classifier classifier = (Classifier) resolvedBase.type();

    Classifier.Member member = classifier.members.get(name);
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
  public Type type() {
    return null;
  }
}
