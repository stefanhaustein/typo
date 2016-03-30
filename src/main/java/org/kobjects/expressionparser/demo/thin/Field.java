package org.kobjects.expressionparser.demo.thin;


import org.kobjects.expressionparser.demo.thin.type.Type;

public interface Field {
  String name();
  void set(EvaluationContext context, Object value);
  Type type();
  Object get(EvaluationContext context);
}
