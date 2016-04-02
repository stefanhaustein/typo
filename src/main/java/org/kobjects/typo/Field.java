package org.kobjects.typo;


import org.kobjects.typo.type.Type;

public interface Field {
  String name();
  void set(EvaluationContext context, Object value);
  Type type();
  Object get(EvaluationContext context);
}
