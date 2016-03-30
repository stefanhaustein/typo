package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.demo.thin.type.Type;
import org.kobjects.expressionparser.demo.thin.type.UnresolvedType;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
  ExpressionProcessor expressionProcessor = new ExpressionProcessor();
  ExpressionParser<org.kobjects.expressionparser.demo.thin.ast.Expression> expressionParser = new ExpressionParser<>(expressionProcessor);
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

  public org.kobjects.expressionparser.demo.thin.ast.Statement parseBlock(ExpressionParser.Tokenizer tokenizer,
                                List<org.kobjects.expressionparser.demo.thin.ast.Classifier> newClasses) {
    List<org.kobjects.expressionparser.demo.thin.ast.Statement> result = new ArrayList<>();
    while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
        !tokenizer.currentValue.equals("}")) {
      if (tokenizer.tryConsume(";")) {
        continue;
      }
      org.kobjects.expressionparser.demo.thin.ast.Statement statement = parseStatement(tokenizer);
      if (statement.kind == org.kobjects.expressionparser.demo.thin.ast.Statement.Kind.CLASSIFIER) {
        if (newClasses == null) {
          throw new RuntimeException("class permitted at top level only.");
        }
        newClasses.add((org.kobjects.expressionparser.demo.thin.ast.Classifier) statement.expression);
      }
      result.add(statement);
    }
    if (result.size() == 1) {
      return result.get(0);
    }
    return new org.kobjects.expressionparser.demo.thin.ast.Statement(result.toArray(new org.kobjects.expressionparser.demo.thin.ast.Statement[result.size()]));
  }

  org.kobjects.expressionparser.demo.thin.ast.Classifier parseClass(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.consumeIdentifier();
    org.kobjects.expressionparser.demo.thin.ast.Classifier classifier = new org.kobjects.expressionparser.demo.thin.ast.Classifier(org.kobjects.expressionparser.demo.thin.ast.Classifier.Kind.CLASS, name);
    tokenizer.consume("{");
    while (!tokenizer.tryConsume("}")) {
      org.kobjects.expressionparser.demo.thin.ast.Classifier.Member member = new org.kobjects.expressionparser.demo.thin.ast.Classifier.Member();
      member.name = tokenizer.consumeIdentifier();
      if (tokenizer.tryConsume(":")) {
        member.type = parseType(tokenizer);
        tokenizer.consume(";");
      } else if (tokenizer.currentValue.equals("(")) {
        member.implementation = parseFunction(tokenizer, false);
      }
      classifier.addMember(member);
    }
    return classifier;
  }

  org.kobjects.expressionparser.demo.thin.ast.Function parseFunction(ExpressionParser.Tokenizer tokenizer, boolean method) {
    String functionName = null;
    if (!method && !tokenizer.tryConsume("(")) {
      functionName = tokenizer.consumeIdentifier();
      tokenizer.consume("(");
    }
    ArrayList<org.kobjects.expressionparser.demo.thin.ast.Function.Parameter> parameterList = new ArrayList<>();
    if (!tokenizer.tryConsume(")")) {
      do {
        String parameterName = tokenizer.consumeIdentifier();
        tokenizer.consume(":");
        Type parameterType = parseType(tokenizer);
        parameterList.add(new org.kobjects.expressionparser.demo.thin.ast.Function.Parameter(parameterName, parameterType));
      } while(tokenizer.tryConsume(","));
      tokenizer.consume(")");
    }

    tokenizer.consume(":");
    Type returnType = parseType(tokenizer);

    tokenizer.consume("{");

    org.kobjects.expressionparser.demo.thin.ast.Statement body = parseBlock(tokenizer, null);
    tokenizer.consume("}");

    org.kobjects.expressionparser.demo.thin.ast.Function fn = new org.kobjects.expressionparser.demo.thin.ast.Function(functionName,
        parameterList.toArray(new org.kobjects.expressionparser.demo.thin.ast.Function.Parameter[parameterList.size()]),
        returnType,
        body);
    return fn;
  }

  org.kobjects.expressionparser.demo.thin.ast.Statement parseInterface(ExpressionParser.Tokenizer tokenizer) {
    throw new RuntimeException("NYI");
  }

  private org.kobjects.expressionparser.demo.thin.ast.Statement parseStatement(ExpressionParser.Tokenizer tokenizer) {
    org.kobjects.expressionparser.demo.thin.ast.Statement result;
    if (tokenizer.tryConsume("let")) {
      result = parseLet(tokenizer);
    } else if (tokenizer.tryConsume("class")) {
      org.kobjects.expressionparser.demo.thin.ast.Classifier classifier = parseClass(tokenizer);

      result = new org.kobjects.expressionparser.demo.thin.ast.Statement(org.kobjects.expressionparser.demo.thin.ast.Statement.Kind.CLASSIFIER, classifier);
    } else if (tokenizer.tryConsume("interface")) {
      result = parseInterface(tokenizer);
    } else if (tokenizer.tryConsume("return")) {
      result = new org.kobjects.expressionparser.demo.thin.ast.Statement(org.kobjects.expressionparser.demo.thin.ast.Statement.Kind.RETURN,
          expressionParser.parse(tokenizer));
    } else {
      result = new org.kobjects.expressionparser.demo.thin.ast.Statement(org.kobjects.expressionparser.demo.thin.ast.Statement.Kind.EXPRESSION,
          expressionParser.parse(tokenizer));
    }
    tokenizer.tryConsume(";");
/*    if (!tokenizer.tryConsume(";") && !cli) {
      throw tokenizer.exception("Semicolon expected after statement.", null);
    } */
    return result;
  }

  private org.kobjects.expressionparser.demo.thin.ast.Statement parseLet(ExpressionParser.Tokenizer tokenizer) {
    String target = tokenizer.consumeIdentifier();
    tokenizer.consume("=");
    org.kobjects.expressionparser.demo.thin.ast.Expression expr = expressionParser.parse(tokenizer);
    return new org.kobjects.expressionparser.demo.thin.ast.Statement(org.kobjects.expressionparser.demo.thin.ast.Statement.Kind.LET,
        new org.kobjects.expressionparser.demo.thin.ast.UnresolvedOperator("=", new org.kobjects.expressionparser.demo.thin.ast.UnresolvedIdentifier(target), expr));
  }

  org.kobjects.expressionparser.demo.thin.ast.Expression parseNew(ExpressionParser.Tokenizer tokenizer) {
    Type type = parseType(tokenizer);
    tokenizer.consume("(");
    ArrayList<org.kobjects.expressionparser.demo.thin.ast.Expression> args = new ArrayList<>();
    if (!tokenizer.currentValue.equals(")")) {
      do {
        args.add(expressionParser.parse(tokenizer));
      } while (tokenizer.tryConsume(","));
    }
    tokenizer.consume(")");
    return new org.kobjects.expressionparser.demo.thin.ast.New(type, args.toArray(new org.kobjects.expressionparser.demo.thin.ast.Expression[args.size()]));
  }

  Type parseType(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.consumeIdentifier();
    return new UnresolvedType(name);
  }

  class ExpressionProcessor extends ExpressionParser.Processor<org.kobjects.expressionparser.demo.thin.ast.Expression> {
    @Override
    public org.kobjects.expressionparser.demo.thin.ast.Expression primary(String name, ExpressionParser.Tokenizer tokenizer) {
      if (name.equals("function")) {
        return parseFunction(tokenizer, false);
      }
      if (name.equals("new")) {
        return parseNew(tokenizer);
      }
      throw new RuntimeException("NYI");
    }

    @Override
    public org.kobjects.expressionparser.demo.thin.ast.Expression identifier(String name) {
      return new org.kobjects.expressionparser.demo.thin.ast.UnresolvedIdentifier(name);
    }

    @Override
    public org.kobjects.expressionparser.demo.thin.ast.Expression infixOperator(String name, org.kobjects.expressionparser.demo.thin.ast.Expression left, org.kobjects.expressionparser.demo.thin.ast.Expression right) {
      return new org.kobjects.expressionparser.demo.thin.ast.UnresolvedOperator(name, left, right);
    }

    @Override
    public org.kobjects.expressionparser.demo.thin.ast.Expression suffixOperator(String name, org.kobjects.expressionparser.demo.thin.ast.Expression param, ExpressionParser.Tokenizer tokenizer) {
      if (name.equals(".")) {
        String propertyName = tokenizer.consumeIdentifier();
        return new org.kobjects.expressionparser.demo.thin.ast.UnresolvedProperty(param, propertyName);
      }
      return super.suffixOperator(name, param, tokenizer);
    }

    @Override
    public org.kobjects.expressionparser.demo.thin.ast.Expression stringLiteral(String rawValue) {
      return new org.kobjects.expressionparser.demo.thin.ast.Literal(ExpressionParser.unquote(rawValue));
    }

    @Override
    public org.kobjects.expressionparser.demo.thin.ast.Expression numberLiteral(String value) {
      return new org.kobjects.expressionparser.demo.thin.ast.Literal(Double.parseDouble(value));
    }

    @Override
    public org.kobjects.expressionparser.demo.thin.ast.Expression apply(org.kobjects.expressionparser.demo.thin.ast.Expression to, String bracket, List<org.kobjects.expressionparser.demo.thin.ast.Expression> parameterList) {
      return new org.kobjects.expressionparser.demo.thin.ast.Apply(to, parameterList.toArray(new org.kobjects.expressionparser.demo.thin.ast.Expression[parameterList.size()]));
    }
  }
}
