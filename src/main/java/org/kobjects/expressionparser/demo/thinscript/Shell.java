package org.kobjects.expressionparser.demo.thinscript;


import org.kobjects.expressionparser.demo.thinscript.parser.*;
import org.kobjects.expressionparser.demo.thinscript.statement.ExpressionStatement;
import org.kobjects.expressionparser.demo.thinscript.statement.ReturnStatement;
import org.kobjects.expressionparser.demo.thinscript.type.FunctionType;
import org.kobjects.expressionparser.demo.thinscript.statement.Classifier;
import org.kobjects.expressionparser.demo.thinscript.statement.Statement;
import org.kobjects.expressionparser.demo.thinscript.type.Types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

public class Shell {

  static Processor processor = new Processor();
  static org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext parsingContext = new org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext(null, null);

  static void load(String url) {
    try {
      InputStream is;
      if (url.indexOf(':') != -1) {
        URLConnection connection = new URL(url).openConnection();
        connection.setDoInput(true);
        is = connection.getInputStream();
      } else {
        System.out.println("resource: '" + url + "'");
        is = Shell.class.getResourceAsStream(url);
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

      org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext loadParsingContext = new org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext(parsingContext, null);
      Statement parsed = processor.process(loadParsingContext, reader);

      reader.close();
      is.close();

      System.out.println("Parsed: " + parsed);

      EvaluationContext loadEvaluationContext = new EvaluationContext(null, null);
      loadEvaluationContext.adjustLocals(loadParsingContext);
      parsed.eval(loadEvaluationContext);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  public static void main(String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    Classifier consoleClass = new Classifier(Classifier.Kind.CLASS, "Console");
    consoleClass.addMethod("log", new Applicable() {
      @Override
      public FunctionType type() {
        return new FunctionType(Types.VOID, Types.STRING);
      }

      @Override
      public Object apply(EvaluationContext context) {
        System.out.println(context.getLocal(0));
        return null;
      }
    });
    parsingContext.declareStatic("load", new Applicable() {
      @Override
      public FunctionType type() {
        return new FunctionType(Types.VOID, Types.STRING);
      }

      @Override
      public Object apply(EvaluationContext context) {
        load(String.valueOf(context.locals[0]));
        return null;
      }
    });

    parsingContext.declareStatic("console", new Instance(consoleClass));
    EvaluationContext evaluationContext = new EvaluationContext(null, null);

    while (true) {
      System.out.print("\nExpression?  ");
      String line = reader.readLine();
      if (!line.endsWith(";")) {
        line += ";";
      }
      try {
        Statement statement = processor.process(parsingContext, new StringReader(line));
        System.out.println("Processed:   " + statement);

        if (statement instanceof ExpressionStatement) {
          statement = new ReturnStatement(((ExpressionStatement) statement).expression);
        }

        evaluationContext.adjustLocals(parsingContext);

        System.out.println("Result:      " + statement.eval(evaluationContext));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
