package org.kobjects.expressionparser.demo.typo;

interface Applicable extends Typed {

  @Override
  FunctionType type();

  Object apply(EvaluationContext context);
}
