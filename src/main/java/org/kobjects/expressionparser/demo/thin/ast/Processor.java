package org.kobjects.expressionparser.demo.thin.ast;


import org.kobjects.expressionparser.ExpressionParser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Processor {
  Parser parser = new Parser();

  public Statement process(org.kobjects.expressionparser.demo.thin.ParsingContext context, Reader reader) {
    List<Classifier> newClassifiers = new ArrayList<>();
    ExpressionParser.Tokenizer tokenizer = parser.createTokenizer(reader);
    tokenizer.nextToken();
    Statement body = parser.parseBlock(tokenizer, newClassifiers);

    for (Classifier classifier : newClassifiers) {
      context.declare(classifier);
    }

    System.out.println("Raw Parsed: " + body);

    body.resolveSignatures(context);

    System.out.println("Signatures resolved: " + body);

    body.resolve(context);
    return body;
  }

  private void resolveSignatures(org.kobjects.expressionparser.demo.thin.ParsingContext context, Statement[] body) {
    for (Statement statement : body) {
      statement.resolveSignatures(context);
    }
  }
}
