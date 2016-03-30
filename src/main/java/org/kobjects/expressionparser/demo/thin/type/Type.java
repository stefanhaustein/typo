package org.kobjects.expressionparser.demo.thin.type;

public interface Type {


  Type VOID = new SimpleType("void");
  Type NONE = new SimpleType("N/A");
  Type NUMBER = new SimpleType("number");
  Type STRING = new SimpleType("string");

  String name();

  Type resolveType(org.kobjects.expressionparser.demo.thin.ParsingContext context);

}
