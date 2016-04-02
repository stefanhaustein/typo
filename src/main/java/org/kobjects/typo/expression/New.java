package org.kobjects.typo.expression;


import org.kobjects.typo.Instance;
import org.kobjects.typo.statement.TsClass;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;

public class New extends Node {
  TsClass classifier;  // Filled on resolve only
  public New(Type type, Expression... child) {
    super(type, child);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("new ");
    cp.append(type.name());
    cp.append('(');
    if (children.length > 0) {
      children[0].print(cp);
      for (int i = 1; i < children.length; i++) {
        cp.append(", ");
        children[i].print(cp);
      }
    }
    cp.append(')');
  }

  @Override
  public Expression resolve(ParsingContext context) {
    resolveChildren(context);
    type = type.resolveType(context);
    if (!(type instanceof TsClass)) {
      throw new RuntimeException("'" + type + "' must be a class for new.");
    }
    classifier = (TsClass) type;

    if (classifier.constructor == null) {
      if (children.length != 0) {
        throw new RuntimeException("No constructor arguments expected.");
      }
    } else {
      Function constructor = classifier.constructor;
      constructor.type().assertSignature(childTypes(), CodePrinter.toString(this));
    }

    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    Instance instance = new Instance(classifier);
    EvaluationContext newContext = new EvaluationContext(instance, classifier.constructor);
    for (TsClass.Member member: classifier.members.values()) {
      if (member.fieldIndex != -1) {
        if (member.initializer != null) {
          newContext.setLocal(member.fieldIndex, member.initializer.eval(newContext));
        } else {
          // Set neutral value.
        }
      }
    }
    for (int i = 0; i < children.length; i++) {
      newContext.setLocal(i, children[i].eval(context));
    }
    if (classifier.constructor != null) {
      classifier.constructor.apply(newContext);
    }
    return newContext.self;
  }
}
