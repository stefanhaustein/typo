package net.tidej.expressionparser;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A simple configurable expression parser.
 */
public class ExpressionParser<T> {

  /**
   * Called by the expression parser, needs to be implemented by the user. May process
   * the expressions directly or build a tree.
   */
  public interface Processor<T> {
    /** Called when an infix operator with the given name is parsed. */
    T infix(String name, T left, T right);

    /** Called when a prefix operator with the given name is parsed. */
    T prefix(String name, T argument);

    /** Called when a suffix operator with the given name is parsed. */
    T suffix(String name, T argument);

    /** Called when the given number literal is parsed. */
    T number(String value);

    /** Called when the given (quoted) string literal is parsed. */
    T string(char quote, String value);

    /** Called when the given identifier is parsed. */
    T identifier(String name);

    /** Called when a group with the given opening bracket and elements is parsed. */
    T group(String paren, List<T> elements);

    /**
     * Called when a bracked registered for calls following an identifier is parsed.
     * Useful to avoid apply(indentifier(identifier), bracket, arguments) in simple cases,
     * see calculator example.
     */
    T call(String identifier, String bracket, List<T> arguments);

    /** Called when an argument list with the given base, opening bracket and elements is parsed. */
    T apply(T base, String bracket, List<T> arguments);
  }

  /**
   * Implement this interface where a StreamTokenizer-based parser is insufficient.
   */
  public interface Tokenizer {
    enum TokenType {
      BOF, IDENTIFIER, SYMBOL, NUMBER, STRING, EOF
    }

    /** The type of the current token. BOF before the first call to nextToken(). */
    TokenType currentType();

    /** The unescaped / unquoted string value of the current token. */
    String currentValue();

    /** The quote char if the current toke is a string; 0 otherwise */
    char currentQuote();

    /** Advance to the next token. */
    void nextToken() throws IOException;
  }

  static class Operators {
    HashSet<String> prefix = new HashSet<>();
    HashMap<String, String[]> group = new HashMap<>();
    HashSet<String> infix = new HashSet<>();
    HashSet<String> infixRtl = new HashSet<>();
    HashSet<String> suffix = new HashSet<>();
    HashMap<String, String[]> apply = new HashMap<>();
  }
  private final HashMap<String, String[]> calls = new HashMap<>();

  private final ArrayList<Operators> precedenceList = new ArrayList<>();
  private final Processor<T> processor;

  private static String currentOperator(Tokenizer tokenizer) {
    return tokenizer.currentType() == Tokenizer.TokenType.IDENTIFIER
        || tokenizer.currentType() == Tokenizer.TokenType.SYMBOL ? tokenizer.currentValue() : "";
  }

  public ExpressionParser(Processor<T> processor) {
    this.processor = processor;
  }

  /**
   * Adds "call" brackets, parsed eagerly after identifiers.
   */
  public void addCallBrackets(String open, String separator, String close) {
    calls.put(open, new String[]{separator, close});
  }

  /**
   * Adds "apply" brackets with the given precedence. Used for function calls or array element access.
   */
  public void addApplyBrackets(int precedence, String open, String separator, String close) {
    operators(precedence).apply.put(open, new String[]{separator, close});
  }

  /**
   * Adds grouping with the given precedence. If the separator is null, only a single element
   * will be permitted. If the separator is empty, whitespace will be sufficient for element
   * separation. Used for parsing lists or overriding the operator precedence (typically with
   * parens and a null separator).
   */
  public ExpressionParser<T> addGroupBrackets(int precedence, String open, String separator, String close) {
    operators(precedence).group.put(open, new String[]{separator, close});
    return this;
  }

  /**
   * Add regular (left-to-right) infix operators with the given precedence.
   */
  public void addInfixOperators(int precedence, String... names) {
    for (String name : names) {
      operators(precedence).infix.add(name);
    }
  }

  /**
   * Add right-binding infix operators with the given precedence.
   */
  public void addInfixRtlOperators(int precedence, String... names) {
    for (String name : names) {
      operators(precedence).infixRtl.add(name);
    }
  }

  /**
   * Add prefix operators with the given precedence.
   */
  public void addPrefixOperators(int precedence, String... names) {
    for (String name: names) {
      operators(precedence).prefix.add(name);
    }
  }

  /**
   * Add suffix operators with the given precedence.
   */
  public void addSuffixOperators(int precedence, String... names) {
    for (String name: names) {
      operators(precedence).suffix.add(name);
    }
  }

  private Operators operators(int precedence) {
    while (precedenceList.size() <= precedence) {
      precedenceList.add(new Operators());
    }
    return precedenceList.get(precedence);
  }

  /**
   * Parse the given expression using a simple StreamTokenizer-based parser.
   * Leftover tokens will cause an exception.
   */
  public T parse(String expr) throws IOException {
    Tokenizer tokenizer = new SimpleTokenizer(new StringReader(expr));
    tokenizer.nextToken();
    T result = parse(tokenizer);
    if (tokenizer.currentType() != Tokenizer.TokenType.EOF) {
      throw new RuntimeException("Leftover token: " + tokenizer);
    }
    return result;
  }

  /**
   * Parse an expression from the given tokenizer. Leftover tokens will be ignored and
   * may be handled by the caller.
   */
  public T parse(Tokenizer tokenizer) throws IOException {
    return parseOperator(tokenizer, 0);
  }

  private T parseOperator(Tokenizer tokenizer, int precedence) throws IOException {
    if (precedence >= precedenceList.size()) {
      return parsePrimary(tokenizer);
    }

    // Prefix

    Operators operators = operators(precedence);
    String operator = currentOperator(tokenizer);
    if (operators.prefix.contains(operator)) {
      tokenizer.nextToken();
      return processor.prefix(operator, parseOperator(tokenizer, precedence));
    }
    if (operators.group.containsKey(operator)) {
      tokenizer.nextToken();
      String[] grouping = operators.group.get(operator);
      return processor.group(operator, parseList(tokenizer, grouping[0], grouping[1]));
    }

    // Infix

    T result = parseOperator(tokenizer, precedence + 1);
    operator = currentOperator(tokenizer);

    if (operators.infixRtl.contains(operator)) {
      tokenizer.nextToken();
      return processor.infix(operator, result, parseOperator(tokenizer, precedence));
    }

    while (operators.infix.contains(operator)) {
      tokenizer.nextToken();
      T right = parseOperator(tokenizer, precedence + 1);
      result = processor.infix(operator, result, right);
      operator = currentOperator(tokenizer);
    }

    // Suffix

    while (operators.suffix.contains(operator) || operators.apply.containsKey(operator)) {
      tokenizer.nextToken();
      if (operators.suffix.contains(operator)) {
        result = processor.suffix(operator, result);
      } else {
        String[] apply = operators.apply.get(operator);
        result = processor.apply(result, operator, parseList(tokenizer, apply[0], apply[1]));
      }
      operator = currentOperator(tokenizer);
    }
    return result;
  }

  // Precondition: Opening paren consumed
  // Postcondition: Closing paren consumed
  List<T> parseList(Tokenizer tokenizer, String separator, String close) throws IOException {
    ArrayList<T> elements = new ArrayList<>();
    if (!currentOperator(tokenizer).equals(close)) {
      while (true) {
        elements.add(parse(tokenizer));
        String op = currentOperator(tokenizer);
        if (op.equals(close)) {
          break;
        }
        if (separator == null) {
          throw new RuntimeException("Closing bracket " + close + " expected at " + tokenizer);
        }
        if (!separator.isEmpty()) {
          if (!op.equals(separator)) {
            throw new RuntimeException("List separator " + separator + " or closing paren " + close
                + " expected at " + tokenizer);
          }
          tokenizer.nextToken();  // separator
        }
      }
    }
    tokenizer.nextToken();  // closing paren
    return elements;
  }

  T parsePrimary(Tokenizer tokenizer) throws IOException {
    T result;
    switch (tokenizer.currentType()) {
      case NUMBER:
        result = processor.number(tokenizer.currentValue());
        tokenizer.nextToken();
        break;
      case IDENTIFIER:
        String identifier = tokenizer.currentValue();
        tokenizer.nextToken();
        if (calls.containsKey(currentOperator(tokenizer))) {
          String openingBracket = currentOperator(tokenizer);
          String[] call = calls.get(openingBracket);
          tokenizer.nextToken();
          result = processor.call(identifier, openingBracket, parseList(tokenizer, call[0], call[1]));
        } else {
          result = processor.identifier(identifier);
        }
        break;
      case STRING:
        result = processor.string(tokenizer.currentQuote(), tokenizer.currentValue());
        tokenizer.nextToken();
        break;
      default:
        throw new RuntimeException("Unexpected token: " + tokenizer);
    }
    return result;
  }

  /** 
   * A simple tokenizer utilizing java.io.StreamTokenizer.
   */
  public static class SimpleTokenizer implements Tokenizer {
    protected StreamTokenizer tokenizer;
    protected String currentValue = "";
    protected TokenType currentType = TokenType.BOF;
    protected char currentQuote;

    public SimpleTokenizer(Reader reader) {
      tokenizer = new StreamTokenizer(reader);
      tokenizer.resetSyntax();
      tokenizer.whitespaceChars(0, ' ');

      tokenizer.wordChars('0', '9');
      tokenizer.wordChars('a', 'z');
      tokenizer.wordChars('A', 'Z');
      tokenizer.wordChars(0xc0, 0x1fff);
      // All kinds of (math) symbols
      tokenizer.wordChars(0x2c00, 0xffff);

      tokenizer.quoteChar('"');
      tokenizer.quoteChar('\'');

      tokenizer.eolIsSignificant(false);
      tokenizer.slashStarComments(true);
    }

    public SimpleTokenizer(StreamTokenizer tokenizer) {
      this.tokenizer = tokenizer;
    }

    @Override
    public TokenType currentType() {
      return currentType;
    }

    @Override
    public String currentValue() {
      return currentValue;
    }

    @Override
    public char currentQuote() {
      return currentQuote;
    }

    private int peek() throws IOException {
      int result = tokenizer.nextToken();
      tokenizer.pushBack();
      return result;
    }

    private boolean readNumber() throws IOException {
      char c0 = tokenizer.sval.charAt(0);
      if (c0 < '0' || c0 > '9') {
        return false;
      }
      currentValue = tokenizer.sval;
      currentType = TokenType.NUMBER;
      if (peek() == '.') {
        tokenizer.nextToken();
        currentValue += '.';
        if (peek() == StreamTokenizer.TT_WORD) {
          tokenizer.nextToken();
          currentValue += tokenizer.sval;
        }
      }
      if ((currentValue.endsWith("e") || currentValue.endsWith("E")) && peek() == '-') {
        tokenizer.nextToken();
        currentValue += '-';
        tokenizer.nextToken();
        currentValue += tokenizer.sval;
      }
      return true;
    }

    @Override
    public void nextToken() throws IOException {
      tokenizer.nextToken();
      currentQuote = 0;
      switch (tokenizer.ttype) {
        case StreamTokenizer.TT_EOF:
          currentType = TokenType.EOF;
          currentValue = "";
          break;
        case StreamTokenizer.TT_NUMBER:
          currentType = TokenType.NUMBER;
          currentValue = String.valueOf(tokenizer.nval);
          break;
        case StreamTokenizer.TT_WORD:
          if (!readNumber()) {
            currentValue = tokenizer.sval;
            currentType = TokenType.IDENTIFIER;
          }
          break;
        case '\'':
        case '"':
          currentType = TokenType.STRING;
          currentValue = tokenizer.sval;
          currentQuote = (char) tokenizer.ttype;
          break;
        default: {
          char c = (char) tokenizer.ttype;
          if (c == '.' && peek() == StreamTokenizer.TT_WORD) {
            tokenizer.nextToken();
            if (readNumber()) {
              currentValue = '.' + currentValue;
              break;
            }
            tokenizer.pushBack();
            currentValue = ".";
          } else if ((c == '<' || c == '>' || c == '!' || c == '=') && peek() == '=') {
            currentValue = c + "=";
            tokenizer.nextToken();
          } else if ((c == '&' || c == '|') && peek() == c) {
            currentValue = String.valueOf(c) + c;
            tokenizer.nextToken();
          } else {
            currentValue = String.valueOf(c);
          }
          currentType = TokenType.SYMBOL;
        }
      }
    }

    @Override
    public String toString() {
      return tokenizer.toString();
    }
  }
}
