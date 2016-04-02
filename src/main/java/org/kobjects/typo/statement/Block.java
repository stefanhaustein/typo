package org.kobjects.typo.statement;


import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;

public class Block extends SimpleStatement {

  public Block(Statement[] children) {
    super(null, children);
  }

  @Override
  public Object eval(EvaluationContext context) {
    for (Statement s: children) {
      System.out.println("Eval block statement: " + s);
      Object result = s.eval(context);
      if (result != NO_RESULT) {
        System.out.println("Aborting; Result observed: " + result);
        return result;
      }
    }
    return NO_RESULT;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("{");
    if (children.length > 0) {
      cp.indent();
      for (Statement s : children) {
        cp.newLine();
        s.print(cp);
      }
      cp.outdent();
      cp.newLine();
    }
    cp.append("} ");
  }

}
