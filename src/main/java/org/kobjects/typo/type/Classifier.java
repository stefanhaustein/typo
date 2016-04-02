package org.kobjects.typo.type;


import org.kobjects.typo.parser.NamedEntity;
import org.kobjects.typo.parser.ParsingContext;

public interface Classifier extends Type, NamedEntity {
  void resolveMembers(ParsingContext context);
  abstract Type propertyType(String key);
}
