package org.kobjects.expressionparser.demo.thinscript.expression;


import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.statement.Classifier;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

public class New extends Node {
  public New(Type type, Expression... child) {
    super(type, child);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("new ");
    cp.append(type);
    cp.append('(');
    if (children.length > 0) {
      cp.append(children[0]);
      for (int i = 1; i < children.length; i++) {
        cp.append(", ");
        cp.append(children[i]);
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
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    Object[] args = new Object[children.length];
    for (int i = 0; i < args.length; i++) {
      args[i] = children[i].eval(context);
    }
    return (((Classifier) type).newInstance(context, args));
  }
}
