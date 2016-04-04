package org.kobjects.typo;


import org.kobjects.typo.parser.Processor;
import org.kobjects.typo.runtime.*;
import org.kobjects.typo.type.ArrayType;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.type.Types;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.statement.ExpressionStatement;
import org.kobjects.typo.statement.ReturnStatement;
import org.kobjects.typo.statement.Statement;
import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.io.Ansi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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

      System.out.println("Parsed: " + org.kobjects.typo.io.CodePrinter.toString(parsed));

      EvaluationContext loadEvaluationContext = new EvaluationContext(null, 0);
      loadEvaluationContext.adjustLocals(loadParsingContext);
      parsed.eval(loadEvaluationContext);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    TsClass imageDataClass = new TsClass("ImageData");
    imageDataClass.addField(EnumSet.noneOf(TsClass.Modifier.class), "width", Types.NUMBER, null);
    imageDataClass.addField(EnumSet.noneOf(TsClass.Modifier.class), "height", Types.NUMBER, null);
    imageDataClass.addField(EnumSet.noneOf(TsClass.Modifier.class), "data", new ArrayType(Types.NUMBER), null);
    parsingContext.declareStatic("ImageData", imageDataClass);

    imageDataClass.constructor = new NativeFunction(imageDataClass, imageDataClass,
        new FunctionType.Parameter("width", Types.NUMBER),
        new FunctionType.Parameter("height", Types.NUMBER)) {
      @Override
      public Object apply(EvaluationContext context) {
        Instance instance = (Instance) context.getLocal(thisIndex);
        Number width = (Number) context.getLocal(0);
        Number height = (Number) context.getLocal(1);
        instance.setField(0, width);
        instance.setField(1, height);
        int dataSize = width.intValue() * height.intValue() * 4;
        ArrayList<Double> data = new ArrayList<Double>(dataSize);
        for (int i = 0; i < dataSize; i++) {
          data.add(0.0);
        }
        instance.setField(2, data);  // Slightly insane
        return instance;
      }
    };

    TsClass mathClass = new TsClass("Math");
    mathClass.addMethod(EnumSet.of(TsClass.Modifier.STATIC), "sqrt", new NativeFunction(
        null, Types.NUMBER, new FunctionType.Parameter("x", Types.NUMBER)) {
      @Override
      public Object apply(EvaluationContext context) {
        return Math.sqrt(((Number) context.getLocal(0)).doubleValue());
      }
    });
    mathClass.addMethod(EnumSet.of(TsClass.Modifier.STATIC), "floor", new NativeFunction(
        null, Types.NUMBER, new FunctionType.Parameter("x", Types.NUMBER)) {
      @Override
      public Object apply(org.kobjects.typo.runtime.EvaluationContext context) {
        return Math.floor(((Number) context.getLocal(0)).doubleValue());
      }
    });
    parsingContext.declareStatic("Math", mathClass);

    TsClass consoleClass = new TsClass("Console");
    consoleClass.addMethod(EnumSet.noneOf(TsClass.Modifier.class), "log", new NativeFunction(
        consoleClass, Types.VOID, new FunctionType.Parameter("s", Types.ANY)) {
      @Override
      public Object apply(org.kobjects.typo.runtime.EvaluationContext context) {
        Object o = context.getLocal(0);
        if (o instanceof Instance && ((Instance) o).type() == imageDataClass) {
          Instance instance = (Instance) o;
          int width = ((Number) instance.fields[0]).intValue();
          int height = ((Number) instance.fields[1]).intValue();
          List<Double> data = (List<Double>) instance.fields[2];
          StringBuilder sb = new StringBuilder("\n");
          int p0 = 0;
          int p1 = 4 * width;
          for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x++) {
              sb.append(Ansi.fgColor(data.get(p0).byteValue(), data.get(p0 + 1).byteValue(), data.get(p0 + 2).byteValue()));
              if (p1 < data.size()) {
                sb.append(Ansi.bgColor(data.get(p1).byteValue(), data.get(p1 + 1).byteValue(), data.get(p1 + 2).byteValue()));
              }
              sb.append("\u2580"); // Upper half block;
              p0 += 4;
              p1 += 4;
            }
            p0 += 4 * width;
            p1 += 4 * width;
            sb.append("\u001B[0m\n");
          }
          o = sb.toString();
        }
        System.out.println(String.valueOf(o));
        return null;
      }
    });
    parsingContext.declareStatic("load", new NativeFunction(
        null, Types.VOID, new FunctionType.Parameter("s", Types.STRING)) {
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
        System.out.println("Processed:   " + org.kobjects.typo.io.CodePrinter.toString(statement));

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
