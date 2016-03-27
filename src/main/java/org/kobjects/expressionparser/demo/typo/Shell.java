package org.kobjects.expressionparser.demo.typo;


import org.kobjects.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

public class Shell {
  public static void main(String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    Parser parser = new Parser();

    Classifier rootClass = new Classifier(Classifier.Kind.CLASS, "Root");
    Classifier consoleClass = new Classifier(Classifier.Kind.CLASS, "Console");
    consoleClass.addMethod("log", new Applicable() {
      @Override
      public FunctionType type() {
        return new FunctionType(Type.VOID, Type.STRING);
      }

      @Override
      public Object apply(EvaluationContext context) {
        System.out.println(context.getLocal(0));
        return null;
      }
    });
    rootClass.addField("console", consoleClass);
    Instance console = consoleClass.newInstance(null);
    Instance root = rootClass.newInstance(null);
    root.setField(rootClass.members.get("console").fieldIndex, console);

    parser.parsingContext = new ParsingContext(rootClass);
    EvaluationContext evaluationContext = new EvaluationContext(root, null);


    while (true) {
      System.out.print("Expression? ");
      String line = reader.readLine();
      if (!line.endsWith(";")) {
        line += ";";
      }
      try {
        ExpressionParser.Tokenizer tokenizer = parser.createTokenizer(new StringReader(line));
        tokenizer.nextToken();
        Statement node = parser.parseStatement(tokenizer, true);
        System.out.println("Parsed:     " + node);
        System.out.println("Result:     " + node.eval(evaluationContext));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
