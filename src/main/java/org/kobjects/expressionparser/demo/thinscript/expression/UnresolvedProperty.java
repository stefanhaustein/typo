package org.kobjects.expressionparser.demo.thinscript.expression;


import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.statement.Classifier;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.MetaType;
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
    base.print(cp);
    cp.append('.').append(name);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    Expression resolvedBase = base.resolve(context);
    Type baseType = resolvedBase.type();
    if (baseType instanceof Classifier) {
      Classifier classifier = (Classifier) resolvedBase.type();
      Classifier.Member member = classifier.members.get(name);
      if (member == null) {
        throw new RuntimeException("Member '" + name + "' not found in " + classifier);
      }
      return new Property(resolvedBase, member);
    }
    if (baseType instanceof MetaType && ((MetaType) baseType).of instanceof Classifier) {
      Classifier classifier = (Classifier) ((MetaType) baseType).of;
      Classifier.Member member = classifier.members.get(name);
      if (member == null) {
        throw new RuntimeException("Member '" + name + "' not found in " + classifier);
      }
      if (member.fieldIndex != -1) {
        throw new RuntimeException("Member '" + name + "' must be static for static access.");
      }
      return new Literal(member.staticValue, classifier.name() + "." + name);
    }
    throw new RuntimeException("Classifier expected; got: " + resolvedBase.type());
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
