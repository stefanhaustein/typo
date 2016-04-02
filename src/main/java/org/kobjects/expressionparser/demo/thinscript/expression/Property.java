package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.runtime.StaticMap;
import org.kobjects.expressionparser.demo.thinscript.statement.Interface;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

public class Property extends Node {

  String name;

  Property(Expression base, String name) {
    super(((Interface) base.type()).getType(name), base);
    this.name = name;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    throw new RuntimeException("Already resolved");
  }

  @Override
  public Object eval(EvaluationContext context) {
    return ((StaticMap) children[0].eval(context)).get(name);
  }

  @Override
  public void print(CodePrinter cp) {
    children[0].print(cp);
    cp.append(".").append(name);
  }
}
