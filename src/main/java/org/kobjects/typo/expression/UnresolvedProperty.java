package org.kobjects.typo.expression;

import org.kobjects.typo.runtime.StaticMap;
import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.statement.Module;
import org.kobjects.typo.type.Classifier;
import org.kobjects.typo.type.Interface;
import org.kobjects.typo.type.MetaType;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.type.Type;

public class UnresolvedProperty extends Expression1 {

  final String name;

  public UnresolvedProperty(Expression base, String name) {
    super(base);
    this.name = name;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    super.resolve(context);
    if (child instanceof Literal && ((Literal) child).value instanceof Module) {
      Module module = (Module) ((Literal) child).value;
      ParsingContext.LocalDeclaration local = module.parsingContext.resolveField(name);
      return new ModuleProperty(module, local);
    }
    Type baseType = child.type();
    if (baseType instanceof Classifier) {
      Classifier classifier = (Classifier) child.type();
      Classifier.Member member = classifier.member(name);
      if (member == null) {
        throw new RuntimeException("Member '" + name + "' not found in " + classifier);
      }
      return new Member(child, member);
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
      return new StaticMember(member);
    }
    throw new RuntimeException("Interface or class expected; got: " + child.type() + " for " + CodePrinter.toString(this));
  }

  @Override
  public Object eval(EvaluationContext context) {
    return ((StaticMap) child.eval(context)).get(name);
  }

  @Override
  public void print(CodePrinter cp) {
    child.print(cp);
    cp.append(".").append(name);
  }

  @Override
  public Type type() {
    return null;
  }
}
