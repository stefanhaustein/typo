package net.tidej.expressionparser.demo.sets;

import net.tidej.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Demo for set expression operators.
 */
public class SetDemo {

  static class SetProcessor extends ExpressionParser.Processor<Object> {

    private Set<Object> assertSet(Object o) {
      if (!(o instanceof Set)) {
        throw new RuntimeException("Set expected for " + o);
      }
      return (Set<Object>) o;
    }

    @Override
    public Object infix(String name, Object left, Object right) {
      if (name.equals("\u2229")) {  // intersection
        assertSet(left).retainAll(assertSet(right));
        return left;
      }
      if (name.equals("\u222a")) {  // union
        assertSet(left).addAll(assertSet(right));
        return left;
      }
      if (name.equals("\u2216") || name.equals("\\")) {  // set minus
        assertSet(left).removeAll(assertSet(right));
        return left;
      }
      throw new UnsupportedOperationException(name);
    }

    @Override
    public Object number(String value) {
      return Double.parseDouble(value);
    }

    @Override
    public Object string(String value) {
      return value;
    }

    @Override
    public Object primarySymbol(String name) {
      if (name.equals("\u2205")){
        return new LinkedHashSet<Object>();
      }
      throw new UnsupportedOperationException("Symbol: " + name);
    }

    @Override
    public Object identifier(String name) {
      return name;
    }

    @Override
    public Object group(String paren, List<Object> elements) {
      if (paren.equals("(")) {
        return elements.get(0);
      }
      if (paren.equals("{")) {
        LinkedHashSet<Object> set = new LinkedHashSet<>();
        set.addAll(elements);
        return set;
      }
      if (paren.equals("|")) {
        Object o = elements.get(0);
        if (o instanceof Set) {
          return ((Set) o).size();
        }
        if (o instanceof Double) {
          return Math.abs((Double) o);
        }
        throw new RuntimeException("Can't apply || to " + o);
      }
      return super.group(paren, elements);
    }
  }

  public static void main(String[] args) throws IOException {
    System.out.println("Operators: \u2229 \u222a \u2216 \u2205");
    ExpressionParser<Object> parser = new ExpressionParser<>(new SetProcessor());
    parser.addGroupBrackets(2, "(", null, ")");
    parser.addGroupBrackets(2, "{", ",", "}");
    parser.addGroupBrackets(2, "|", null, "|");
    parser.addOperators(ExpressionParser.OperatorType.INFIX, 1, "\u2229");
    parser.addOperators(ExpressionParser.OperatorType.INFIX, 0, "\u222a", "\u2216", "\\");
    parser.addPrimarySymbols("\u2205");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.print("Expression? ");
      String input = reader.readLine();
      if (input == null || input.isEmpty()) {
        break;
      }
      try {
        System.out.println("Result:     " + parser.parse(input).toString().replace('[', '{').replace(']', '}'));
      } catch (ExpressionParser.ParsingException e) {
        System.out.print("Error -----");
        for (int i = 0; i < e.position; i++) {
          System.out.print("-");
        }
        System.out.println("^: " + e.getMessage());
      } catch (RuntimeException e) {
        System.out.println("Error: " + e.toString());
      }
    }
  }
}
