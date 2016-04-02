package org.kobjects.typo.expression;


import org.kobjects.typo.type.Interface;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.MetaType;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;

public class UnresolvedProperty extends Expression {
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
    if (baseType instanceof TsClass) {
      TsClass classifier = (TsClass) resolvedBase.type();
      TsClass.Member member = classifier.members.get(name);
      if (member == null) {
        throw new RuntimeException("Member '" + name + "' not found in " + classifier);
      }
      return new Member(resolvedBase, member);
    }
    if (baseType instanceof Interface) {
      Interface itf = (Interface) resolvedBase.type();
      Type propertyType = itf.propertyType(name);
      if (propertyType == null) {
        throw new RuntimeException("Property '" + name + "' not found in " + itf);
      }
      return new Property(resolvedBase, name);
    }
    if (baseType instanceof MetaType && ((MetaType) baseType).of instanceof TsClass) {
      TsClass classifier = (TsClass) ((MetaType) baseType).of;
      TsClass.Member member = classifier.members.get(name);
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
  public Type type() {
    return null;
  }
}
