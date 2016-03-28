package org.kobjects.expressionparser.demo.typo;


import org.kobjects.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Shell {

  static Parser parser = new Parser();
  static EvaluationContext evaluationContext;

  static void load(String url) {
    try {
      InputStream is;
      if (url.indexOf(':') != -1) {
        URLConnection connection = new URL(url).openConnection();
        connection.setDoInput(true);
        is = connection.getInputStream();
      } else {
        System.out.println("Url: " + url);
        is = Shell.class.getResourceAsStream(url);
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

      ExpressionParser.Tokenizer tokenizer = parser.createTokenizer(reader);
      tokenizer.nextToken();
      ArrayList<Statement> result = new ArrayList<Statement>();
      parser.parseBody(tokenizer, result);

      reader.close();
      is.close();

      adjustLocals();

      for (Statement s: result) {
        System.out.println("Exec: " + s);
        s.eval(evaluationContext);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static void adjustLocals() {
    if (parser.parsingContext.locals.size() > evaluationContext.locals.length) {
      Object[] newLocals = new Object[parser.parsingContext.locals.size()];
      System.arraycopy(evaluationContext.locals, 0, newLocals, 0, evaluationContext.locals.length);
      evaluationContext.locals = newLocals;
    }

  }

  public static void main(String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

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
    rootClass.addMethod("load", new Applicable() {
      @Override
      public FunctionType type() {
        return new FunctionType(Type.VOID, Type.STRING);
      }

      @Override
      public Object apply(EvaluationContext context) {
        load(String.valueOf(context.locals[0]));
        return null;
      }
    });
    rootClass.addField("console", consoleClass);
    Instance console = consoleClass.newInstance(null);
    Instance root = rootClass.newInstance(null);
    root.setField(rootClass.members.get("console").fieldIndex, console);

    parser.parsingContext = new ParsingContext(rootClass);
    evaluationContext = new EvaluationContext(root, null);

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

        adjustLocals();

        System.out.println("Result:     " + node.eval(evaluationContext));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
