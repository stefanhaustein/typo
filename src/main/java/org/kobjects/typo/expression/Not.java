package org.kobjects.typo.expression;

import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;

public class Not extends Expression1 {
  Not(Expression expr) {
    super(expr);
  }


  @Override
  public Object eval(EvaluationContext context) {
    return !(Boolean) child.eval(context);
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("(!");
    child.print(cp);
    cp.append(")");
  }

  @Override
  public Type type() {
    return Types.BOOLEAN;
  }
}
