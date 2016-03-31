package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.Printable;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

public abstract class Statement implements Printable {
  String NO_RESULT = new String("NO_RESULT");

  public abstract Object eval(EvaluationContext context);

  public abstract void resolveSignatures(ParsingContext context);

  public abstract void resolve(ParsingContext context);

  public String toString() {
    CodePrinter cp = new CodePrinter();
    print(cp);
    return cp.toString();
  }
}