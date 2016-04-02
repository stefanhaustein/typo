package org.kobjects.typo;


import org.kobjects.typo.type.Type;

public interface Field {
  String name();
  void set(org.kobjects.typo.runtime.EvaluationContext context, Object value);
  Type type();
  Object get(org.kobjects.typo.runtime.EvaluationContext context);
}
