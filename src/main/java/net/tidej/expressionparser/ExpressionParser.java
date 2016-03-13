package net.tidej.expressionparser;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ExpressionParser<T> {

  public interface Processor<T> {
    T infix(String name, T left, T right);
    T prefix(String name, T argument);
    T number(String value);
    T string(String value);
    T identifier(String name);
    T list(String paren, List<T> elements);
    T call(String identifier, List<T> arguments);
    T apply(T base, String bracket, List<T> arguments);
  }

  public interface Tokenizer {
    enum TokenType {
      BOF, IDENTIFIER, SYMBOL, NUMBER, STRING, EOF
    }
    TokenType currentType();
    String currentValue();
    void nextToken() throws IOException;
  }

  private final ArrayList<HashSet<String>> infixOperators = new ArrayList<>();
  private final HashSet<String> prefixOperators = new HashSet<>();
  private final Processor<T> processor;
  private final HashMap<String,String[]> applyBrackets = new HashMap<>();
  private final HashMap<String,String[]> listBrackets = new HashMap<>();
  private final HashMap<String,String> expressionBrackets = new HashMap<>();

  private static String currentOperator(Tokenizer tokenizer) {
    return tokenizer.currentType() == Tokenizer.TokenType.IDENTIFIER
        || tokenizer.currentType() == Tokenizer.TokenType.SYMBOL ? tokenizer.currentValue() : "";
  }

  public ExpressionParser(Processor<T> processor) {
    this.processor = processor;
  }

  public ExpressionParser<T> addPrefixOperators(String... names) {
    for (String name: names) {
      prefixOperators.add(name);
    }
    return this;
  }

  public ExpressionParser<T> addApplyBrackets(String open, String separator, String close) {
    applyBrackets.put(open, new String[]{separator, close});
    return this;
  }

  public ExpressionParser<T> addExpressionBrackets(String open, String close) {
    expressionBrackets.put(open, close);
    return this;
  }

  public ExpressionParser<T> addListBrackets(String open, String separator, String close) {
    listBrackets.put(open, new String[]{separator, close});
    return this;
  }

  public ExpressionParser<T> addInfixOperators(int precedence, String... names) {
    while (infixOperators.size() <= precedence) {
      infixOperators.add(new HashSet<String>());
    }
    for (String name : names) {
      infixOperators.get(precedence).add(name);
    }
    return this;
  }

  public T parse(String s) throws IOException {
    Tokenizer tokenizer = new SimpleTokenizer(new StringReader(s));
    tokenizer.nextToken();
    T result = parse(tokenizer);
    if (tokenizer.currentType() != Tokenizer.TokenType.EOF) {
      throw new RuntimeException("Leftover token: " + tokenizer);
    }
    return result;
  }

  public T parse(Tokenizer tokenizer) throws IOException {
    return parseInfix(tokenizer, infixOperators.size() - 1);
  }

  T parseInfix(Tokenizer tokenizer, int precedence) throws IOException {
    HashSet<String> operators;
    while(true) {
      if (precedence < 0) {
        return parsePrimary(tokenizer);
      }
      operators = infixOperators.get(precedence);
      if (operators.size() > 0) {
        break;
      }
      precedence--;
    }
    T result = parseInfix(tokenizer, precedence - 1);
    String operator = currentOperator(tokenizer);
    while (operators.contains(operator)) {
      tokenizer.nextToken();
      T right = parseInfix(tokenizer, precedence - 1);
      result = processor.infix(operator, result, right);
      operator = currentOperator(tokenizer);
    }
    return result;
  }

  List<T> parseList(Tokenizer tokenizer, String separator, String close) throws IOException {
    ArrayList<T> elements = new ArrayList<>();
    tokenizer.nextToken();  // opening paren
    if (!currentOperator(tokenizer).equals(close)) {
      while (true) {
        elements.add(parse(tokenizer));
        String op = currentOperator(tokenizer);
        if (op.equals(close)) {
          break;
        }
        if (!op.equals(separator)) {
          throw new RuntimeException("List separator " + separator + " or closing paren " + close
              + " expected at " + tokenizer);
        }
        tokenizer.nextToken();  // separator
      }
    }
    tokenizer.nextToken();  // closing paren
    return elements;
  }

  T parsePrimary(Tokenizer tokenizer) throws IOException {
    String op = currentOperator(tokenizer);
    if (prefixOperators.contains(op)) {
      tokenizer.nextToken();
      return processor.prefix(op, parse(tokenizer));
    }
    String closing = expressionBrackets.get(op);
    if (closing != null) {
      tokenizer.nextToken();
      T result = parse(tokenizer);
      if (!currentOperator(tokenizer).equals(closing)) {
        throw new RuntimeException("Expected closing paren " + closing + " at " + tokenizer);
      }
      tokenizer.nextToken();
      return result;
    }
    T result;
    String[] lp = listBrackets.get(op);
    if (lp != null) {
      result = processor.list(op, parseList(tokenizer, lp[0], lp[1]));
    } else {
      switch (tokenizer.currentType()) {
        case NUMBER:
          result = processor.number(tokenizer.currentValue());
          tokenizer.nextToken();
          break;
        case IDENTIFIER:
          String identifier = tokenizer.currentValue();
          tokenizer.nextToken();
          if (currentOperator(tokenizer).equals("(") && !applyBrackets.containsKey("(")) {
            result = processor.call(identifier, parseList(tokenizer, ",", ")"));
          } else {
            result = processor.identifier(identifier);
          }
          break;
        case STRING:
          result = processor.string(tokenizer.currentValue());
          tokenizer.nextToken();
          break;
        default:
          throw new RuntimeException("Unexpected token: " + tokenizer);
      }
    }
    op = currentOperator(tokenizer);
    while (true) {
      String[] ap = applyBrackets.get(op);
      if (ap == null) {
        return result;
      }
      result = processor.apply(result, op, parseList(tokenizer, ap[0], ap[1]));
    }
  }

  private class SimpleTokenizer implements Tokenizer {
    private StreamTokenizer tokenizer;

    SimpleTokenizer(Reader reader) {
      tokenizer = new StreamTokenizer(reader);
      tokenizer.ordinaryChar('-');
      tokenizer.ordinaryChar('/');
    }

    @Override
    public TokenType currentType() {
      switch (tokenizer.ttype) {
        case StreamTokenizer.TT_WORD:
          return TokenType.IDENTIFIER;
        case '"':
          return TokenType.STRING;
        case StreamTokenizer.TT_EOF:
          return TokenType.EOF;
        case StreamTokenizer.TT_NUMBER:
          return TokenType.NUMBER;
        case -4:
          return TokenType.BOF;
        default:
          return TokenType.SYMBOL;
      }
    }

    @Override
    public String currentValue() {
      switch (tokenizer.ttype) {
        case StreamTokenizer.TT_NUMBER:
          return String.valueOf(tokenizer.nval);
        case -4:
        case StreamTokenizer.TT_EOF:
          return "";
        case StreamTokenizer.TT_WORD:
        case '"':
          return tokenizer.sval;
        default:
          return String.valueOf((char) tokenizer.ttype);
      }
    }

    @Override
    public void nextToken() throws IOException {
      tokenizer.nextToken();
    }

    @Override
    public String toString() {
      return tokenizer.toString();
    }
  }
}
