package org.kobjects.typo;


import org.kobjects.typo.parser.Processor;
import org.kobjects.typo.runtime.*;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.type.Types;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.statement.ExpressionStatement;
import org.kobjects.typo.statement.ReturnStatement;
import org.kobjects.typo.statement.Statement;
import org.kobjects.typo.type.FunctionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;

public class TypoShell {

  static Processor processor = new Processor();
  static ParsingContext parsingContext = new ParsingContext(null);

  static void load(String url) {
    try {
      InputStream is;
      if (url.indexOf(':') != -1) {
        URLConnection connection = new URL(url).openConnection();
        connection.setDoInput(true);
        is = connection.getInputStream();
      } else {
        System.out.println("resource: '" + url + "'");
        is = TypoShell.class.getResourceAsStream(url);
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

      ParsingContext loadParsingContext = new ParsingContext(parsingContext);
      Statement parsed = processor.process(loadParsingContext, reader);

      reader.close();
      is.close();

      System.out.println("Parsed: " + CodePrinter.toString(parsed));

      EvaluationContext loadEvaluationContext = new EvaluationContext(null, 0);
      loadEvaluationContext.adjustLocals(loadParsingContext);
      parsed.eval(loadEvaluationContext);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    TsClass mathClass = new TsClass("Math");
    mathClass.addMethod(EnumSet.of(TsClass.Modifier.STATIC), "sqrt",
        new NativeFunction(Types.NUMBER, new FunctionType.Parameter("x", Types.NUMBER)) {

      @Override
      public Object apply(EvaluationContext context) {
        return Math.sqrt(((Number) context.getLocal(0)).doubleValue());
      }
    });
    mathClass.addMethod(EnumSet.of(TsClass.Modifier.STATIC), "floor", new NativeFunction(
        Types.NUMBER, new FunctionType.Parameter("x", Types.NUMBER)) {

      @Override
      public Object apply(org.kobjects.typo.runtime.EvaluationContext context) {
        return Math.floor(((Number) context.getLocal(0)).doubleValue());
      }
    });
    parsingContext.declareStatic("Math", mathClass);

    TsClass consoleClass = new TsClass("Console");
    consoleClass.addMethod(EnumSet.noneOf(TsClass.Modifier.class), "log", new NativeFunction(
        Types.VOID, new FunctionType.Parameter("s", Types.STRING)) {
      @Override
      public Object apply(org.kobjects.typo.runtime.EvaluationContext context) {
        System.out.println(context.getLocal(0));
        return null;
      }
    });
    parsingContext.declareStatic("load", new NativeFunction(
        Types.VOID, new FunctionType.Parameter("s", Types.STRING)) {
      @Override
      public Object apply(EvaluationContext context) {
        load(String.valueOf(context.locals[0]));
        return null;
      }
    });

    parsingContext.declareStatic("console", new org.kobjects.typo.runtime.Instance(consoleClass));
    EvaluationContext evaluationContext = new EvaluationContext(null, 0);

    while (true) {
      System.out.print("\nExpression?  ");
      String line = reader.readLine();
      if (!line.endsWith(";")) {
        line += ";";
      }
      try {
        Statement statement = processor.process(parsingContext, new StringReader(line));
        System.out.println("Processed:   " + CodePrinter.toString(statement));

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
