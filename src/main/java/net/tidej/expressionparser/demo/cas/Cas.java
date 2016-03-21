package net.tidej.expressionparser.demo.cas;

import net.tidej.expressionparser.ExpressionParser;
import net.tidej.expressionparser.ExpressionParser.ParsingException;
import net.tidej.expressionparser.demo.cas.string2d.String2d;
import net.tidej.expressionparser.demo.cas.tree.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Cas {

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
        System.out.println("\nParsed: " + s + '\n');
        s = "Equals: " + s;
        Set<String> explanation = new LinkedHashSet<String>();
        while (true) {
          String t = String2d.concat("Equals: ", expr.toString2d(Node.Stringify.BLOCK)).toString();
          if (!s.equals(t)) {
            s = t;
            System.out.println(s + "\n");
          }
          Node simplified = expr.simplify(explanation);
          if (simplified.equals(expr)) {
            break;
          }
          expr = simplified;
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
