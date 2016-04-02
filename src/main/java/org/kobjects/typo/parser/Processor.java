package org.kobjects.typo.parser;


import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.statement.Statement;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processor {
  Parser parser = new Parser();

  public Statement process(ParsingContext context, Reader reader) {
    List<NamedEntity> newStatics = new ArrayList<>();
    ExpressionParser.Tokenizer tokenizer = parser.createTokenizer(reader);
    tokenizer.nextToken();
    Statement body = parser.parseBlock(tokenizer, newStatics);

    System.out.println("Raw Parsed:  " + CodePrinter.toString(body));
    System.out.println("New statics: " + newStatics);

    for (NamedEntity entity : newStatics) {
      context.declareStatic(entity.name(), entity);
    }
    for (NamedEntity entity : newStatics) {
      entity.resolveSignatures(context);
    }

    System.out.println("S. resolved: " + CodePrinter.toString(body));

    body.resolve(context);
    return body;
  }
}
