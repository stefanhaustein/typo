package org.kobjects.expressionparser.demo.thinscript.expression;


import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.Instance;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.statement.Classifier;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

public class New extends Node {
  Classifier classifier;  // Filled on resolve only
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
    if (!(type instanceof Classifier)) {
      throw new RuntimeException("'" + type + "' must be a class for new.");
    }
    classifier = (Classifier) type;

    if (classifier.constructor == null) {
      if (children.length != 0) {
        throw new RuntimeException("No constructor arguments expected.");
      }
    } else {
      Function constructor = classifier.constructor;
      if (constructor.parameters.length != children.length) {
        throw new RuntimeException("Expected " + constructor.parameters.length
            + " constructor parameters; got " + children.length);
      }
      for (int i = 0; i < children.length; i++) {
        if (!constructor.parameters[i].type.assignableFrom(children[i].type())) {
          throw new RuntimeException("Expected type '" + constructor.parameters[i].type.name()
              + "' for parameter " + constructor.parameters[i].name
              + ", but got '" + children[i].type());
        }
      }
    }

    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    EvaluationContext newContext = new EvaluationContext(new Instance(classifier), classifier.constructor);
    for (int i = 0; i < children.length; i++) {
      newContext.setLocal(i, children[i].eval(context));
    }
    if (classifier.constructor != null) {
      classifier.constructor.apply(newContext);
    }
    return newContext.self;
  }
}
