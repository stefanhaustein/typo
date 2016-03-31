package org.kobjects.expressionparser.demo.thin.statement;


import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;

public class Block extends Statement {
  Statement[] children;

  public Block(Statement[] children) {
    this.children = children;
  }

  @Override
  public Object eval(EvaluationContext context) {
    for (Statement s: children) {
      Object result = s.eval(context);
      if (result != NO_RESULT) {
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

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (Statement s: children) {
      sb.append(s);
    }
    sb.append("} ");
    return sb.toString();
  }

}
