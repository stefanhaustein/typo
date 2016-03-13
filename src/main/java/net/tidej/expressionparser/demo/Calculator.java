package net.tidej.expressionparser.demo;

import net.tidej.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Calculator {

  /**
   * Processes the calls from the parser directly to a Double value.
   */
  static class DoubleProcessor implements ExpressionParser.Processor<Double> {
    @Override
    public Double infix(String name, Double left, Double right) {
      switch (name.charAt(0)) {
        case '+': return left + right;
        case '-': return left - right;
        case '*': return left * right;
        case '/': return left / right;
        case '^': return Math.pow(left, right);
        default:
          throw new IllegalArgumentException();
      }
    }

    @Override
    public Double prefix(String name, Double argument) {
      return name.equals("-") ? -argument : argument;
    }

    @Override
    public Double suffix(String name, Double argument) {
      throw new UnsupportedOperationException(name);
    }

    @Override
    public Double number(String value) {
      return Double.parseDouble(value);
    }

    @Override
    public Double string(char quote, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Double identifier(String name) {
      try {
        return Math.class.getDeclaredField(name).getDouble(null);
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }

    @Override
    public Double group(String paren, List<Double> elements) {
      return elements.get(0);
    }

    @Override
    public Double call(String identifier, String bracket, List<Double> arguments) {
      Class<?>[] argTypes = new Class<?>[arguments.size()];
      Object[] args = new Object[arguments.size()];
      for (int i = 0; i < argTypes.length; i++) {
        argTypes[i] = Double.TYPE;
        args[i] = arguments.get(i);
      }
      try {
        return (Double) Math.class.getMethod(identifier, argTypes).invoke(null, args);
      } catch (Exception e) {
        throw new UnsupportedOperationException(e);
      }
    }

    @Override
    public Double apply(Double base, String bracket, List<Double> arguments) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Registers operations and precedences.
   */
  public static void main(String[] args) throws IOException {
    ExpressionParser<Double> parser = new ExpressionParser<Double>(new DoubleProcessor());
    parser.addCallBrackets("(", ",", ")");
    parser.addGroupBrackets(0, "(", null, ")");
    parser.addInfixRtlOperators(1, "^");
    parser.addPrefixOperators(2, "+", "-");
    parser.addInfixOperators(3, "*", "/");
    parser.addInfixOperators(4, "+", "-");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.print("Expression? ");
      String input = reader.readLine();
      if (input == null || input.isEmpty()) {
        break;
      }
      try {
        System.out.println("Result:    " + parser.parse(input));
      } catch (Exception e) {
        e.printStackTrace();
//        System.out.println("Error:     " + e.getMessage());
      }
    }
  }
}
