package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.expression.Expression;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

public class IfStatement extends SimpleStatement {

  public IfStatement(Expression condition, Statement thenBranch) {
    super(condition, thenBranch);
  }

  public IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) {
    super(condition, thenBranch, elseBranch);
  }

  public Object eval(EvaluationContext context) {
    boolean b = (Boolean) expression.eval(context);
    if (b) {
      return children[0].eval(context);
    } else if (children.length > 1) {
      return children[1].eval(context);
    }
    return NO_RESULT;
  }


  @Override
  public void print(CodePrinter cp) {
    cp.append("if (");
    expression.print(cp);
    cp.append(") ");
    children[0].print(cp);
    if (children.length > 1) {
      cp.append("else ");
      children[1].print(cp);
    }
  }
}
