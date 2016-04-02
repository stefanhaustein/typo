package org.kobjects.typo.parser;


import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.statement.Statement;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class Processor {
  Parser parser = new Parser();

  public Statement process(ParsingContext context, Reader reader) {
    Map<String, Object> newStatics = new HashMap<>();
    ExpressionParser.Tokenizer tokenizer = parser.createTokenizer(reader);
    tokenizer.nextToken();
    Statement body = parser.parseBlock(tokenizer, newStatics);

    for (String name : newStatics.keySet()) {
      context.declareStatic(name, newStatics.get(name));
    }

    System.out.println("Raw Parsed:  " + CodePrinter.toString(body));
    System.out.println("New statics: " + newStatics);

    body.resolveSignatures(context);

    System.out.println("S. resolved: " + CodePrinter.toString(body));

    body.resolve(context);
    return body;
  }
}
