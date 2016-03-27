package org.kobjects.expressionparser.demo.typo;

class Apply extends Node {
  Node target;

  Apply(Node target, Node... params) {
    super(((FunctionType) (target.type)).type, params);
    this.target = target;
  }

  @Override
  public Object eval(EvaluationContext context) {
    EvaluationContext newContext = (EvaluationContext) target.eval(context);
    for (int i = 0; i < children.length; i++) {
      newContext.setLocal(i, children[i].eval(context));
    }
    return newContext.applicable.apply(newContext);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(target.toString());
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
