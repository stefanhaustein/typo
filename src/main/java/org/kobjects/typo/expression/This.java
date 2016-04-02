package org.kobjects.typo.expression;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.type.Type;

public class This extends Expression {
  TsClass type;
  public This(TsClass type) {
    this.type = type;
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
  public Expression resolve(ParsingContext context) {
    return this;
  }

  @Override
  public Type type() {
    return type;
  }
}
