package org.kobjects.typo.statement;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.expression.Expression;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.ArrayType;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;

import java.util.LinkedHashMap;
import java.util.List;

public class ForInStatement extends SimpleStatement {
  boolean declare;
  String variableName;
  ParsingContext.LocalDeclaration target;

  public ForInStatement(boolean declare, String variableName, Expression expression, Statement body) {
    super(expression, body);
    this.declare = declare;
    this.variableName = variableName;
  }

  @Override
  public Object eval(EvaluationContext context) {
    List<?> array = (List<?>) expression.eval(context);
    for (double i = 0; i < array.size(); i++) {
      context.setLocal(target.localIndex, i);
      Object result = children[0].eval(context);
      if (result != NO_RESULT) {
        return result;
      }
    }
    return NO_RESULT;
  }

  @Override
  public void resolve(ParsingContext context) {
    expression = expression.resolve(context);
    if (!(expression.type() instanceof ArrayType)) {
      throw new RuntimeException("for .. in: Arrays only currently");
    }
    if (declare) {
      target = context.declareLocal(variableName, Types.NUMBER);
    } else {
      target = context.resolveField(variableName);
      if (target == null) {
        throw new RuntimeException("Not found: " + variableName);
      }
      if (!target.type().assignableFrom(Types.NUMBER)) {
        throw new RuntimeException("Incompatible types");
      }
    }
  }


  @Override
  public void print(CodePrinter cp) {
    cp.append("for (");
    if (declare) {
      cp.append("let ");
    }
    cp.append(variableName);
    cp.append(" in ");
    expression.print(cp);
    cp.append(") ");
    children[0].print(cp);
  }
}
