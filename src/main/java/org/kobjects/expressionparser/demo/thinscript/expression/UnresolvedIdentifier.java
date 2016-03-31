package org.kobjects.expressionparser.demo.thinscript.expression;


import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.Field;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

public class UnresolvedIdentifier implements Expression {
  String name;

  public UnresolvedIdentifier(String name) {
    this.name = name;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object eval(EvaluationContext context) {
    throw new UnsupportedOperationException("Can't eval unresolved identifier '" + name + "'.");
  }

  @Override
  public boolean isAssignable() {
    return false;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append(name);
  }

  @Override
  public Expression resolve(ParsingContext context) {
    Field field = context.resolveField(name);
    if (field != null) {
      return new Variable(field);
    }
    Object o = context.resolveStatic(name);
    if (o != null) {
      return new Literal(o, name);
    }
    throw new RuntimeException("Cannot resolve identifier '" + name + "'.");
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Type type() {
    return null;
  }

}
