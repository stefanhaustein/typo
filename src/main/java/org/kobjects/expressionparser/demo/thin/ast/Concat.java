package org.kobjects.expressionparser.demo.thin.ast;


import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.Type;

public class Concat extends Node {

  public Concat(org.kobjects.expressionparser.demo.thin.ast.Expression left, org.kobjects.expressionparser.demo.thin.ast.Expression right) {
    super(Type.STRING, left, right);
  }

  @Override
  public org.kobjects.expressionparser.demo.thin.ast.Expression resolve(ParsingContext context) {
    resolveChildren(context);
    return this;
  }

  @Override
  public Object eval(org.kobjects.expressionparser.demo.thin.EvaluationContext context) {
    return "" + children[0].eval(context) + children[1].eval(context);
  }
}
