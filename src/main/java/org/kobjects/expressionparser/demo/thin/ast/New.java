package org.kobjects.expressionparser.demo.thin.ast;


import org.kobjects.expressionparser.demo.thin.type.Type;

class New extends Node {
  New(Type type, Expression... child) {
    super(type, child);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("new ");
    sb.append(type);
    sb.append('(');
    if (children.length > 0) {
      sb.append(children[0]);
      for (int i = 1; i < children.length; i++) {
        sb.append(", ");
        sb.append(children[i]);
      }
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public Expression resolve(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    resolveChildren(context);
    type = type.resolveType(context);
    if (!(type instanceof org.kobjects.expressionparser.demo.thin.ast.Classifier)) {
      throw new RuntimeException("'" + type + "' must be a class for new.");
    }
    return this;
  }

  @Override
  public Object eval(org.kobjects.expressionparser.demo.thin.EvaluationContext context) {
    Object[] args = new Object[children.length];
    for (int i = 0; i < args.length; i++) {
      args[i] = children[i].eval(context);
    }
    return (((org.kobjects.expressionparser.demo.thin.ast.Classifier) type).newInstance(context, args));
  }
}
