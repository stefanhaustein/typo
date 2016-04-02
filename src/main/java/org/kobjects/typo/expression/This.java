package org.kobjects.typo.expression;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.statement.TsClass;

public class This extends Node {
  public This(TsClass type) {
    super(type);
  }

  @Override
  public Object eval(EvaluationContext context) {
    return context.self;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("this");
  }

  @Override
  public Node resolve(ParsingContext context) {
    throw new UnsupportedOperationException();
  }
}
