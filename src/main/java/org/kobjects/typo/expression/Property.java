package org.kobjects.typo.expression;

import org.kobjects.typo.runtime.StaticMap;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.Interface;
import org.kobjects.typo.type.Type;

public class Property extends Expression1 {

  String name;

  Property(Expression base, String name) {
    super(base);
    this.name = name;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new RuntimeException("Already resolved");
  }

  @Override
  public Object eval(EvaluationContext context) {
    return ((StaticMap) child.eval(context)).get(name);
  }

  @Override
  public void print(CodePrinter cp) {
    child.print(cp);
    cp.append(".").append(name);
  }

  @Override
  public Type type() {
    return ((Interface) child.type()).propertyType(name);
  }
}
