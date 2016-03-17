package net.tidej.expressionparser.demo.derive;

import net.tidej.expressionparser.ExpressionParser;
import net.tidej.expressionparser.demo.derive.tree.Node;
import net.tidej.expressionparser.demo.derive.tree.TreeBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        System.out.println("Parsed:       " + expr);
        expr = expr.simplify();
        System.out.println("Simplified:   " + expr);
        Node derived = expr.derive("x");
        System.out.println("Derived to x: " + derived);
        System.out.println("Simplified:   " + derived.simplify());
      } catch (Exception e) {
        System.out.println("Error:     " + e.getMessage());
      }
    }
  }
}
