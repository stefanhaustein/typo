package org.kobjects.typo.type;

import org.kobjects.typo.parser.ParsingContext;

public interface Type {

  String name();

  Type resolveType(ParsingContext context);

  boolean assignableFrom(Type type);
}
