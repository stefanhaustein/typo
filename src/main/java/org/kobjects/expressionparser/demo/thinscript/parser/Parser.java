package org.kobjects.expressionparser.demo.thinscript.parser;

import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.demo.thinscript.expression.Apply;
import org.kobjects.expressionparser.demo.thinscript.expression.Expression;
import org.kobjects.expressionparser.demo.thinscript.expression.Function;
import org.kobjects.expressionparser.demo.thinscript.expression.Literal;
import org.kobjects.expressionparser.demo.thinscript.expression.New;
import org.kobjects.expressionparser.demo.thinscript.expression.Ternary;
import org.kobjects.expressionparser.demo.thinscript.expression.UnresolvedIdentifier;
import org.kobjects.expressionparser.demo.thinscript.expression.UnresolvedOperator;
import org.kobjects.expressionparser.demo.thinscript.expression.UnresolvedProperty;
import org.kobjects.expressionparser.demo.thinscript.statement.Block;
import org.kobjects.expressionparser.demo.thinscript.statement.Classifier;
import org.kobjects.expressionparser.demo.thinscript.statement.ExpressionStatement;
import org.kobjects.expressionparser.demo.thinscript.statement.IfStatement;
import org.kobjects.expressionparser.demo.thinscript.statement.LetStatement;
import org.kobjects.expressionparser.demo.thinscript.statement.ReturnStatement;
import org.kobjects.expressionparser.demo.thinscript.statement.Statement;
import org.kobjects.expressionparser.demo.thinscript.statement.WhileStatement;
import org.kobjects.expressionparser.demo.thinscript.type.Type;
import org.kobjects.expressionparser.demo.thinscript.type.UnresolvedType;

import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

class Parser {
  ExpressionProcessor expressionProcessor = new ExpressionProcessor();
  ExpressionParser<Expression> expressionParser = new ExpressionParser<>(expressionProcessor);
  {
    // Source:
    // https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Operators/Operator_Precedence
    expressionParser.addGroupBrackets("(", null, ")");
    expressionParser.addPrimary("function");
    expressionParser.addPrimary("new");
    expressionParser.addOperators(ExpressionParser.OperatorType.SUFFIX, 18, ".");
    expressionParser.addApplyBrackets(17, "(", ",", ")");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 14, "*", "/");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 13, "+", "-");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 11, "<", ">", "<=", ">=");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 10, "===", "==", "!=", "!==");
    expressionParser.addTernaryOperator(4, "?", ":");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 3, "=");
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
    return new Block(result.toArray(new Statement[result.size()]));
  }

  Classifier parseClass(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.consumeIdentifier();
    Classifier classifier = new Classifier(Classifier.Kind.CLASS, name);
    tokenizer.consume("{");
    while (!tokenizer.tryConsume("}")) {
      Set<Classifier.Modifier> modifiers = parseModifiers(
          tokenizer, EnumSet.allOf(Classifier.Modifier.class));
      String memberName = tokenizer.consumeIdentifier();
      if (tokenizer.tryConsume(":")) {
        Expression initialValue = null;
        Type type = parseType(tokenizer);
        if (tokenizer.tryConsume("=")) {
          initialValue = expressionParser.parse(tokenizer);
        }
        classifier.addField(modifiers, memberName, type, initialValue);
        tokenizer.consume(";");
      } else if (tokenizer.currentValue.equals("(")) {
        Function fn = parseFunction(classifier, memberName, tokenizer);
        if (memberName.equals("constructor")) {
          classifier.constructor = fn;
        } else {
          classifier.addMethod(modifiers, memberName, fn);
        }
      }
    }
    return classifier;
  }

  // Precondition: on '('
  // Postcondition: '}' consumed.
  Function parseFunction(Classifier owner, String name, ExpressionParser.Tokenizer tokenizer) {
    tokenizer.consume("(");
    ArrayList<Function.Parameter> parameterList = new ArrayList<>();
    List<Statement> init = new ArrayList<>();
    boolean isConstructor = owner != null && name.equals("constructor");
    if (!tokenizer.tryConsume(")")) {
      Set<Classifier.Modifier> permittedModifiers = !isConstructor ?
          EnumSet.noneOf(Classifier.Modifier.class) : EnumSet.of(
          Classifier.Modifier.PUBLIC, Classifier.Modifier.PRIVATE, Classifier.Modifier.PROTECTED);
      do {
        Set<Classifier.Modifier> modifiers = parseModifiers(tokenizer, permittedModifiers);
            String parameterName = tokenizer.consumeIdentifier();
        tokenizer.consume(":");
        Type parameterType = parseType(tokenizer);
        if (!modifiers.isEmpty()) {
          owner.addField(modifiers, parameterName, parameterType, null);
          init.add(new ExpressionStatement(new UnresolvedOperator("=",
              new UnresolvedProperty(new UnresolvedIdentifier("this"), parameterName),
              new UnresolvedIdentifier(parameterName))));
        }
        parameterList.add(new Function.Parameter(parameterName, parameterType));
      } while(tokenizer.tryConsume(","));
      tokenizer.consume(")");
    }

    Type returnType;
    if (isConstructor) {
      returnType = owner;
    } else {
      tokenizer.consume(":");
      returnType = parseType(tokenizer);
    }

    tokenizer.consume("{");

    Statement body = parseBlock(tokenizer, null);
    if (init.size() > 0) {
      if (body instanceof Block) {
        for (Statement s : ((Block) body).children) {
          init.add(s);
        }
      } else {
        init.add(body);
      }
      body = new Block(init.toArray(new Statement[init.size()]));
    }

    tokenizer.consume("}");

    Function fn = new Function(owner, name,
        parameterList.toArray(new Function.Parameter[parameterList.size()]),
        returnType,
        body);
    return fn;
  }

  private EnumSet<Classifier.Modifier> parseModifiers(
      ExpressionParser.Tokenizer tokenizer, Set<Classifier.Modifier> permitted) {
    EnumSet<Classifier.Modifier> result = EnumSet.noneOf(Classifier.Modifier.class);
    while (true) {
      Classifier.Modifier modifier;
      if (tokenizer.tryConsume("static")) {
        modifier = Classifier.Modifier.STATIC;
      } else if (tokenizer.tryConsume("public")) {
        modifier = Classifier.Modifier.PUBLIC;
      } else {
        break;
      }
      if (!permitted.contains(modifier)) {
        throw new RuntimeException("Modifier '" + modifier.name().toLowerCase() + "' not permitted here.");
      }
      result.add(modifier);
    }
    return result;
  }

  Statement parseInterface(ExpressionParser.Tokenizer tokenizer) {
    throw new RuntimeException("NYI");
  }

  private Statement parseStatement(ExpressionParser.Tokenizer tokenizer, Map<String, Object> statics) {
    Statement result;
    if (tokenizer.tryConsume("{")) {
      result = parseBlock(tokenizer, null);
      tokenizer.consume("}");
    } else if (tokenizer.tryConsume("class")) {
      Classifier classifier = parseClass(tokenizer);
      if (statics == null) {
        throw new RuntimeException("Classes only permitted at top level.");
      }
      statics.put(classifier.name(), classifier);
      result = classifier;
    } else if (tokenizer.tryConsume("if")) {
      tokenizer.consume("(");
      Expression condition = expressionParser.parse(tokenizer);
      tokenizer.consume(")");

      Statement thenBranch = parseStatement(tokenizer, null);
      if (tokenizer.tryConsume("else")) {
        Statement elseBranch = parseStatement(tokenizer, null);
        result = new IfStatement(condition, thenBranch, elseBranch);
      } else {
        result = new IfStatement(condition, thenBranch);
      }
    } else if (tokenizer.tryConsume("interface")) {
      result = parseInterface(tokenizer);
    } else if (tokenizer.tryConsume("let") || tokenizer.tryConsume("var")) {
      result = parseLet(tokenizer);
    } else if (tokenizer.tryConsume("return")) {
      result = new ReturnStatement(expressionParser.parse(tokenizer));
    } else if (tokenizer.tryConsume("while")) {
      tokenizer.consume("(");
      Expression condition = expressionParser.parse(tokenizer);
      tokenizer.consume(")");
      Statement body = parseStatement(tokenizer, null);
      result = new WhileStatement(condition, body);
    } else {
      Expression expression = expressionParser.parse(tokenizer);
      if (expression instanceof Function
          && ((Function) expression).name() != null) {
        if (statics == null) {
          throw new RuntimeException("Named functions only permitted at top level.");
        }
        statics.put(((Function) expression).name(), expression);
      }
      result = new ExpressionStatement(expression);
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
    return new LetStatement(target, expr);
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
    public Expression primary(ExpressionParser.Tokenizer tokenizer, String name) {
      if (name.equals("function")) {
        String functionName = null;
        if (!tokenizer.currentValue.equals("(")) {
          functionName = tokenizer.consumeIdentifier();
        }
        return parseFunction(null, functionName, tokenizer);
      }
      if (name.equals("new")) {
        return parseNew(tokenizer);
      }
      throw new RuntimeException("NYI");
    }

    @Override
    public Expression group(ExpressionParser.Tokenizer tokenizer, String open, List<Expression> list) {
      return list.get(0);
    }

    @Override
    public Expression identifier(ExpressionParser.Tokenizer tokenizer, String name) {
      if (name.equals("true")) {
        return new Literal(true, null);
      } else if (name.equals("false")) {
        return new Literal(false, null);
      } else if (name.equals("null")) {
        return new Literal(null, null);
      } else if (name.equals("Infinity")) {
        return new Literal(Double.POSITIVE_INFINITY, "Infinity");
      }
      return new UnresolvedIdentifier(name);
    }

    @Override
    public Expression infixOperator(ExpressionParser.Tokenizer tokenizer, String name, Expression left, Expression right) {
      return new UnresolvedOperator(name, left, right);
    }

    @Override
    public Expression suffixOperator(ExpressionParser.Tokenizer tokenizer, String name, Expression param) {
      if (name.equals(".")) {
        String propertyName = tokenizer.consumeIdentifier();
        return new UnresolvedProperty(param, propertyName);
      }
      return super.suffixOperator(tokenizer, name, param);
    }

    @Override
    public Expression stringLiteral(ExpressionParser.Tokenizer tokenizer, String rawValue) {
      return new Literal(ExpressionParser.unquote(rawValue), null);
    }

    @Override
    public Expression ternaryOperator(ExpressionParser.Tokenizer tokenizer, String name,
                                      Expression left, Expression middle, Expression right) {
      return new Ternary(left, middle, right);
    }

    @Override
    public Expression numberLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
      double d = Double.parseDouble(value);
      if (value.matches("[0-9]+") && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
        return new Literal((int) d, null);
      }
      return new Literal(d, null);
    }

    @Override
    public Expression apply(ExpressionParser.Tokenizer tokenizer, Expression to, String bracket, List<Expression> parameterList) {
      return new Apply(to, parameterList.toArray(new Expression[parameterList.size()]));
    }
  }
}
