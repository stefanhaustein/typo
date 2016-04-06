package org.kobjects.typo.type;


import org.kobjects.typo.parser.NamedEntity;
import org.kobjects.typo.parser.ParsingContext;

public abstract class Classifier implements Type, NamedEntity {
  public abstract void resolveMembers(ParsingContext context);

  public abstract Member member(String name);

  @Override
  public void declareStatics(ParsingContext context) {
    context.declareStatic(name(), this);
  }

  public interface Member {
    Type type();
    String name();
    void set(Object instance, Object value);
    Object get(Object instance);
  }
}
