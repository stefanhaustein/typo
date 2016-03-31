package org.kobjects.expressionparser.demo.thinscript.statement;


import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

public class Block extends Statement {
  public Statement[] children;

  public Block(Statement[] children) {
    this.children = children;
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
  public void resolveSignatures(ParsingContext context) {
    for (Statement child: children) {
      child.resolveSignatures(context);
    }
  }

  @Override
  public void resolve(ParsingContext context) {
    for (Statement child: children) {
      child.resolve(context);
    }
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
