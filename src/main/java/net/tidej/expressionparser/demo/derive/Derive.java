package net.tidej.expressionparser.demo.derive;

import net.tidej.expressionparser.ExpressionParser;
import net.tidej.expressionparser.ExpressionParser.ParsingException;
import net.tidej.expressionparser.demo.derive.tree.Node;
import net.tidej.expressionparser.demo.derive.tree.TreeBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Derive {

  public static void main(String[] args) throws IOException {
    ExpressionParser<Node> parser = TreeBuilder.createParser();
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.print("Expression?   ");
      String input = reader.readLine();
      if (input == null || input.isEmpty()) {
        break;
      }
      try {
        Node expr = parser.parse(input);
        String s = expr.toString();
        System.out.println("Parsed:       " + expr);
        Set<String> explanation = new LinkedHashSet<String>();
        Node simplified = expr.simplify(explanation);
        while(!expr.equals(simplified)) {
          expr = simplified;
          String t = simplified.toString();
          if (!s.equals(t)) {
            System.out.println("Simplified:   " + t + (explanation.size() == 0 ? "" : ("     " + explanation)));
            s = t;
          }
          explanation.clear();
          simplified = expr.simplify(explanation);
        }
      } catch (ParsingException e) {
        char[] fill = new char[e.position + 8];
        Arrays.fill(fill, '-');
        System.out.println("Error " + new String(fill) + "^: " + e.getMessage());
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }
  }
}
