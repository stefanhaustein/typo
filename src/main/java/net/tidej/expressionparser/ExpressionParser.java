package net.tidej.expressionparser;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

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

  static class Operators {
    HashSet<String> prefix = new HashSet<>();
    HashMap<String, String[]> group = new HashMap<>();
    HashSet<String> infix = new HashSet<>();
    HashSet<String> infixRtl = new HashSet<>();
    HashSet<String> suffix = new HashSet<>();
    HashMap<String, String[]> apply = new HashMap<>();
  }
  private final HashMap<String, String[]> calls = new HashMap<>();
  private final HashMap<String, Boolean> allSymbols = new HashMap<>();

  private final ArrayList<Operators> precedenceList = new ArrayList<>();
  private final Processor<T> processor;

  private static String currentOperator(Tokenizer tokenizer) {
    return tokenizer.currentType == Tokenizer.TokenType.IDENTIFIER
        || tokenizer.currentType == Tokenizer.TokenType.SYMBOL ? tokenizer.currentValue : "";
  }

  public ExpressionParser(Processor<T> processor) {
    this.processor = processor;
  }

  /**
   * Adds "apply" brackets with the given precedence. Used for function calls or array element access.
   */
  public void addApplyBrackets(int precedence, String open, String separator, String close) {
    operators(precedence).apply.put(addSymbol(open, false),
        new String[] {addSymbol(separator, false), addSymbol(close, false)});
  }

  /**
   * Adds "call" brackets, parsed eagerly after identifiers.
   */
  public void addCallBrackets(String open, String separator, String close) {
    calls.put(addSymbol(open, false),
        new String[]{addSymbol(separator, false), addSymbol(close, false)});
  }

  /**
   * Adds grouping with the given precedence. If the separator is null, only a single element
   * will be permitted. If the separator is empty, whitespace will be sufficient for element
   * separation. Used for parsing lists or overriding the operator precedence (typically with
   * parens and a null separator).
   */
  public ExpressionParser<T> addGroupBrackets(int precedence, String open, String separator, String close) {
    operators(precedence).group.put(addSymbol(open, true),
        new String[] {addSymbol(separator, false), addSymbol(close, false)});
    return this;
  }

  /**
   * Used to keep track of all registered operators / symbols and whether they may occur in
   * a non-prefix position.
   */
  private String addSymbol(String symbol, boolean prefix) {
    if (symbol != null && (!allSymbols.containsKey(symbol) || !prefix)) {
      allSymbols.put(symbol, prefix);
    }
    return symbol;
  }

  /**
   * Add regular (left-to-right) infix operators with the given precedence.
   */
  public void addInfixOperators(int precedence, String... names) {
    for (String name : names) {
      operators(precedence).infix.add(addSymbol(name, false));
    }
  }

  /**
   * Add right-binding infix operators with the given precedence.
   */
  public void addInfixRtlOperators(int precedence, String... names) {
    for (String name : names) {
      operators(precedence).infixRtl.add(addSymbol(name, false));
    }
  }

  /**
   * Add prefix operators with the given precedence.
   */
  public void addPrefixOperators(int precedence, String... names) {
    for (String name: names) {
      operators(precedence).prefix.add(addSymbol(name, true));
    }
  }

  /**
   * Add suffix operators with the given precedence.
   */
  public void addSuffixOperators(int precedence, String... names) {
    for (String name: names) {
      operators(precedence).suffix.add(addSymbol(name, false));
    }
  }

  private Operators operators(int precedence) {
    while (precedenceList.size() <= precedence) {
      precedenceList.add(new Operators());
    }
    return precedenceList.get(precedence);
  }

  /**
   * Returns all symbols registered via add...Operator and add...Bracket calls.
   * Useful for tokenizer construction.
   */
  public Iterable<String> getSymbols() {
    return allSymbols.keySet();
  }

  /**
   * Parse the given expression using a simple StreamTokenizer-based parser.
   * Leftover tokens will cause an exception.
   */
  public T parse(String expr) {
    Tokenizer tokenizer = new Tokenizer(new Scanner(expr), getSymbols());
    tokenizer.nextToken();
    T result = parse(tokenizer);
    if (tokenizer.currentType != Tokenizer.TokenType.EOF) {
      throw new RuntimeException("Leftover token: " + tokenizer);
    }
    return result;
  }

  /**
   * Parse an expression from the given tokenizer. Leftover tokens will be ignored and
   * may be handled by the caller.
   */
  public T parse(Tokenizer tokenizer) {
    return parseOperator(tokenizer, 0);
  }

  private T parseOperator(Tokenizer tokenizer, int precedence) {
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
  List<T> parseList(Tokenizer tokenizer, String separator, String close) {
    ArrayList<T> elements = new ArrayList<>();
    if (!currentOperator(tokenizer).equals(close)) {
      while (true) {
        elements.add(parse(tokenizer));
        String op = currentOperator(tokenizer);
        if (op.equals(close)) {
          break;
        }
        if (separator == null) {
          throw new ParsingException("Closing bracket " + close + " expected at " + tokenizer,
              tokenizer.currentPosition, null);
        }
        if (!separator.isEmpty()) {
          if (!op.equals(separator)) {
            throw new ParsingException("List separator " + separator + " or closing paren " + close
                + " expected at " + tokenizer, tokenizer.currentPosition, null);
          }
          tokenizer.nextToken();  // separator
        }
      }
    }
    tokenizer.nextToken();  // closing paren
    return elements;
  }

  T parsePrimary(Tokenizer tokenizer) {
    T result;
    switch (tokenizer.currentType) {
      case NUMBER:
        result = processor.number(tokenizer.currentValue);
        tokenizer.nextToken();
        break;
      case IDENTIFIER:
        String identifier = tokenizer.currentValue;
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
        result = processor.string(tokenizer.currentQuote, tokenizer.currentValue);
        tokenizer.nextToken();
        break;
      default:
        throw new ParsingException("Unexpected token type " + tokenizer, tokenizer.currentPosition, null);
    }
    return result;
  }

  public static class ParsingException extends RuntimeException {
    final public int position;
    public ParsingException(String text, int position, Exception base) {
      super(text, base);
      this.position = position;
    }
  }

  /** 
   * A simple tokenizer utilizing java.util.Scanner.
   */
  public static class Tokenizer {
    public static final Pattern DEFAULT_NUMBER_PATTERN = Pattern.compile(
        "\\G\\s*\\d+(\\.\\d*)?([eE][+-]?\\d+)?");

    public static final Pattern DEFAULT_IDENTIFIER_PATTERN = Pattern.compile(
        "\\G\\s*\\p{IsAlphabetic}[\\p{IsAlphabetic}\\d]*");

    public static final Pattern EOF_PATTERN = Pattern.compile("\\G\\s*\\Z");

    public enum TokenType {
      BOF, IDENTIFIER, SYMBOL, NUMBER, STRING, EOF
    }

    public Pattern numberPattern = DEFAULT_NUMBER_PATTERN;
    public Pattern identifierPattern = DEFAULT_IDENTIFIER_PATTERN;
    public Pattern symbolPattern;

    public int currentPosition = 0;
    public String currentValue = "";
    public TokenType currentType = TokenType.BOF;
    public char currentQuote;

    protected final Scanner scanner;

    public Tokenizer(Scanner scanner, Iterable<String> symbols) {
      this.scanner = scanner;
      StringBuilder sb = new StringBuilder("\\G\\s*(");
      for (String symbol: symbols) {
        sb.append(Pattern.quote(symbol)).append('|');
      }
      sb.setCharAt(sb.length() - 1, ')');
      symbolPattern = Pattern.compile(sb.toString());
    }

    public void nextToken() {
      if (currentType == TokenType.EOF) {
        return;
      }
      String value;
      if ((value = scanner.findWithinHorizon(identifierPattern, 0)) != null) {
        currentType = TokenType.IDENTIFIER;
      } else if ((value = scanner.findWithinHorizon(numberPattern, 0)) != null) {
        currentType = TokenType.NUMBER;
      } else if ((value = scanner.findWithinHorizon(symbolPattern, 0)) != null) {
        currentType = TokenType.SYMBOL;
      } else if ((value = scanner.findWithinHorizon(EOF_PATTERN, 0)) != null) {
        currentType = TokenType.EOF;
      } else if (scanner.ioException() != null) {
        throw new RuntimeException(scanner.ioException());
      } else {
        throw new ParsingException("Unrecognized Token at position " + currentPosition + ": " +
          scanner.findWithinHorizon("\\s*.?.?.?.?.?.?.?.?.?.?.?.?.?.?.?.?.?.?.?.?", 0),
            currentPosition, null);
      }
      currentPosition += value.length();
      currentValue = value.trim();
    }

    @Override
    public String toString() {
      return currentType + " " + currentValue + " position: " + currentPosition;
    }
  }
}
