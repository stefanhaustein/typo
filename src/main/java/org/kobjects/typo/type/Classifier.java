package org.kobjects.typo.type;


import org.kobjects.typo.parser.NamedEntity;
import org.kobjects.typo.parser.ParsingContext;

public abstract class Classifier implements Type, NamedEntity {
  public abstract void resolveMembers(ParsingContext context);

  abstract Type propertyType(String key);

  @Override
  public void declareStatics(ParsingContext context) {
    context.declareStatic(name(), this);
  }
}
