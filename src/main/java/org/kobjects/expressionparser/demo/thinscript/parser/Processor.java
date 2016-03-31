package org.kobjects.expressionparser.demo.thinscript.parser;


import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.demo.thinscript.statement.Statement;

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

    System.out.println("Raw Parsed:  " + body);
    System.out.println("New statics: " + newStatics);

    body.resolveSignatures(context);

    System.out.println("S. resolved: " + body);

    body.resolve(context);
    return body;
  }
}
