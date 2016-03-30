package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.demo.thin.type.Type;
import org.kobjects.expressionparser.demo.thin.type.UnresolvedType;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Parser {
  ExpressionProcessor expressionProcessor = new ExpressionProcessor();
  ExpressionParser<Expression> expressionParser = new ExpressionParser<>(expressionProcessor);
  {
    expressionParser.addPrimary("function");
    expressionParser.addPrimary("new");
    expressionParser.addOperators(ExpressionParser.OperatorType.SUFFIX, 4, ".");
    expressionParser.addApplyBrackets(3, "(", ",", ")");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 2, "*", "/");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 1, "+", "-");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 0, "=");
  }

  public ExpressionParser.Tokenizer createTokenizer(Reader reader) {
    return new ExpressionParser.Tokenizer(new Scanner(reader), expressionParser.getSymbols(), "{", "}", ";", ":");
  }

  public Statement parseBlock(ExpressionParser.Tokenizer tokenizer, Map<String, Object> statics) {
    List<Statement> result = new ArrayList<>();
    while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
        !tokenizer.currentValue.equals("}")) {
      if (tokenizer.tryConsume(";")) {
        continue;
      }
      Statement statement = parseStatement(tokenizer, statics);
      result.add(statement);
    }
    if (result.size() == 1) {
      return result.get(0);
    }
    return new Statement(result.toArray(new Statement[result.size()]));
  }

  Classifier parseClass(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.consumeIdentifier();
    Classifier classifier = new Classifier(Classifier.Kind.CLASS, name);
    tokenizer.consume("{");
    while (!tokenizer.tryConsume("}")) {
      String memberName = tokenizer.consumeIdentifier();
      if (tokenizer.tryConsume(":")) {
        Type type = parseType(tokenizer);
        classifier.addField(memberName, type);
        tokenizer.consume(";");
      } else if (tokenizer.currentValue.equals("(")) {
        Function fn = parseFunction(tokenizer, false);
        classifier.addMethod(memberName, fn);
      }
    }
    return classifier;
  }

  Function parseFunction(ExpressionParser.Tokenizer tokenizer, boolean method) {
    String functionName = null;
    if (!method && !tokenizer.tryConsume("(")) {
      functionName = tokenizer.consumeIdentifier();
      tokenizer.consume("(");
    }
    ArrayList<Function.Parameter> parameterList = new ArrayList<>();
    if (!tokenizer.tryConsume(")")) {
      do {
        String parameterName = tokenizer.consumeIdentifier();
        tokenizer.consume(":");
        Type parameterType = parseType(tokenizer);
        parameterList.add(new Function.Parameter(parameterName, parameterType));
      } while(tokenizer.tryConsume(","));
      tokenizer.consume(")");
    }

    tokenizer.consume(":");
    Type returnType = parseType(tokenizer);

    tokenizer.consume("{");

    Statement body = parseBlock(tokenizer, null);
    tokenizer.consume("}");

    Function fn = new Function(functionName,
        parameterList.toArray(new Function.Parameter[parameterList.size()]),
        returnType,
        body);
    return fn;
  }

  Statement parseInterface(ExpressionParser.Tokenizer tokenizer) {
    throw new RuntimeException("NYI");
  }

  private Statement parseStatement(ExpressionParser.Tokenizer tokenizer, Map<String, Object> statics) {
    Statement result;
    if (tokenizer.tryConsume("let")) {
      result = parseLet(tokenizer);
    } else if (tokenizer.tryConsume("class")) {
      Classifier classifier = parseClass(tokenizer);
      if (statics == null) {
        throw new RuntimeException("Classes only permitted at top level.");
      }
      statics.put(classifier.name, classifier);
      result = new Statement(Statement.Kind.CLASSIFIER, classifier);
    } else if (tokenizer.tryConsume("interface")) {
      result = parseInterface(tokenizer);
    } else if (tokenizer.tryConsume("return")) {
      result = new Statement(Statement.Kind.RETURN,
          expressionParser.parse(tokenizer));
    } else {
      Expression expression = expressionParser.parse(tokenizer);
      if (expression instanceof Function
          && ((Function) expression).name != null) {
        if (statics == null) {
          throw new RuntimeException("Named functions only permitted at top level.");
        }
        statics.put(((Function) expression).name, expression);
      }
      result = new Statement(Statement.Kind.EXPRESSION, expression);
    }
    tokenizer.tryConsume(";");
/*    if (!tokenizer.tryConsume(";") && !cli) {
      throw tokenizer.exception("Semicolon expected after statement.", null);
    } */
    return result;
  }

  private Statement parseLet(ExpressionParser.Tokenizer tokenizer) {
    String target = tokenizer.consumeIdentifier();
    tokenizer.consume("=");
    Expression expr = expressionParser.parse(tokenizer);
    return new Statement(Statement.Kind.LET,
        new UnresolvedOperator("=", new UnresolvedIdentifier(target), expr));
  }

  Expression parseNew(ExpressionParser.Tokenizer tokenizer) {
    Type type = parseType(tokenizer);
    tokenizer.consume("(");
    ArrayList<Expression> args = new ArrayList<>();
    if (!tokenizer.currentValue.equals(")")) {
      do {
        args.add(expressionParser.parse(tokenizer));
      } while (tokenizer.tryConsume(","));
    }
    tokenizer.consume(")");
    return new New(type, args.toArray(new Expression[args.size()]));
  }

  Type parseType(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.consumeIdentifier();
    return new UnresolvedType(name);
  }

  class ExpressionProcessor extends ExpressionParser.Processor<Expression> {
    @Override
    public Expression primary(String name, ExpressionParser.Tokenizer tokenizer) {
      if (name.equals("function")) {
        return parseFunction(tokenizer, false);
      }
      if (name.equals("new")) {
        return parseNew(tokenizer);
      }
      throw new RuntimeException("NYI");
    }

    @Override
    public Expression identifier(String name) {
      return new UnresolvedIdentifier(name);
    }

    @Override
    public Expression infixOperator(String name,Expression left, Expression right) {
      return new UnresolvedOperator(name, left, right);
    }

    @Override
    public Expression suffixOperator(String name, Expression param, ExpressionParser.Tokenizer tokenizer) {
      if (name.equals(".")) {
        String propertyName = tokenizer.consumeIdentifier();
        return new UnresolvedProperty(param, propertyName);
      }
      return super.suffixOperator(name, param, tokenizer);
    }

    @Override
    public Expression stringLiteral(String rawValue) {
      return new Literal(ExpressionParser.unquote(rawValue));
    }

    @Override
    public Expression numberLiteral(String value) {
      return new Literal(Double.parseDouble(value));
    }

    @Override
    public Expression apply(Expression to, String bracket, List<Expression> parameterList) {
      return new Apply(to, parameterList.toArray(new Expression[parameterList.size()]));
    }
  }
}
