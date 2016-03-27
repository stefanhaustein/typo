package org.kobjects.expressionparser.demo.typo;


class New extends Node {
  New(Classifier classifier, Node... child) {
    super(classifier, child);
  }

  @Override
  Object eval(EvaluationContext context) {
    Object[] args = new Object[children.length];
    for (int i = 0; i < args.length; i++) {
      args[i] = children[i].eval(context);
    }
    return (((Classifier) type).newInstance(context, args));
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
}
