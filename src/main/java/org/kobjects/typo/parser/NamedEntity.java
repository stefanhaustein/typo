package org.kobjects.typo.parser;

import org.kobjects.typo.io.Printable;

public interface NamedEntity extends Printable {
  String name();
  void resolveSignatures(ParsingContext context);

  void declareStatics(ParsingContext context);
}
