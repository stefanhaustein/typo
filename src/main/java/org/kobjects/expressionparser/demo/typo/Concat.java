package org.kobjects.expressionparser.demo.typo;

public class Concat extends Node {


  Concat(Node left, Node right) {
    super(Type.STRING, left, right);
  }

  @Override
  Object eval(EvaluationContext context) {
    return "" + children[0].eval(context) + children[1].eval(context);
  }
}
