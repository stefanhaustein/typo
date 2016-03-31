package org.kobjects.expressionparser.demo.thinscript;


import org.kobjects.expressionparser.demo.thinscript.type.Type;

public interface Field {
  String name();
  void set(EvaluationContext context, Object value);
  Type type();
  Object get(EvaluationContext context);
}
