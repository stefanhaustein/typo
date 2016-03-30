package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.ParsingContext;

public class Statement {
  private String NO_RESULT = new String("NO_RESULT");

  public enum Kind {BLOCK, CLASSIFIER, EXPRESSION, LET, RETURN};

  public Kind kind;
  Expression expression;
  Statement[] children;

  public Statement(Kind kind, Expression expression) {
    this.kind = kind;
    this.expression = expression;
  }

  public Statement(Statement[] children) {
    this.kind = Kind.BLOCK;
    this.children = children;
  }

  public Object eval(EvaluationContext context) {
    switch (kind) {
      case BLOCK:
        for (Statement s: children) {
          Object result = s.eval(context);
          if (result != NO_RESULT) {
            return result;
          }
        }
        break;

      case RETURN:
        return expression.eval(context);

      default:
        expression.eval(context);
    }
    return NO_RESULT;
  }


  public String toString() {
    switch (kind) {
      case LET: return "let " + expression + "; ";
      case RETURN: return "return " + expression + "; ";
      case BLOCK: {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Statement s: children) {
          sb.append(s);
        }
        sb.append("} ");
      }
      default:
        return expression + "; ";
    }
  }

  public void resolveSignatures(ParsingContext context) {
    if (expression != null) {
      expression.resolveSignatures(context);
    }
    if (children != null) {
      for (Statement child: children) {
        child.resolveSignatures(context);
      }
    }
  }

  public void resolve(ParsingContext context) {
    if (expression != null) {
      expression = expression.resolve(context);
    }
    if (children != null) {
      for (Statement child: children) {
        child.resolve(context);
      }
    }
  }
}