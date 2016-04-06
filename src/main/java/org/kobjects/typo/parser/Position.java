package org.kobjects.typo.parser;

import org.kobjects.expressionparser.ExpressionParser;


public class Position {
  public static final Position UNDEFINED = new Position(-1, -1);
  public int line;
  int column;

  Position(int line, int column) {
    this.line = line;
    this.column = column;
  }

  Position(ExpressionParser.Tokenizer tokenizer) {
    this.line = tokenizer.currentLine;
    this.column = tokenizer.currentColumn();
  }
}
