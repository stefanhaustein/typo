package org.kobjects.expressionparser.demo.thinscript.type;

import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;

public interface Type {


  String name();

  Type resolveType(ParsingContext context);

}
