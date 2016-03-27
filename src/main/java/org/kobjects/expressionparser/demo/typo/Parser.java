package org.kobjects.expressionparser.demo.typo;


import org.kobjects.expressionparser.ExpressionParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Parser {
  ExpressionProcessor expressionProcessor = new ExpressionProcessor();
  ExpressionParser<Node> expressionParser = new ExpressionParser<>(expressionProcessor);
  {
    expressionParser.addPrimary("function");
    expressionParser.addPrimary("new");
    expressionParser.addOperators(ExpressionParser.OperatorType.SUFFIX , 3, ".");
    expressionParser.addApplyBrackets(2, "(", ",", ")");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 1, "*", "/");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 0, "+", "-");
  }
  ParsingContext parsingContext;

  public ExpressionParser.Tokenizer createTokenizer(StringReader reader) {
    return new ExpressionParser.Tokenizer(new Scanner(reader), expressionParser.getSymbols(), "{", "}", ";", ":");
  }

  Type parseBody(ExpressionParser.Tokenizer tokenizer, List<Statement> result) {
    Type type = null;
    while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
        !tokenizer.currentValue.equals("}")) {
      if (tokenizer.tryConsume(";")) {
        continue;
      }
      Statement statement = parseStatement(tokenizer, false);
      if (statement.kind == Statement.Kind.RETURN) {
        if (type != null && statement.type != type) {
          throw new RuntimeException("Inconsistent return type.");
        }
        type = statement.type;
      }
      result.add(statement);
    }
    return type == null ? Type.VOID : type;
  }

  Classifier parseClass(ExpressionParser.Tokenizer tokenizer) {
    Classifier clazz = new Classifier(Classifier.Kind.CLASS, tokenizer.consumeIdentifier());
    tokenizer.consume("{");
    while (!tokenizer.tryConsume("}")) {
      String memberName = tokenizer.consumeIdentifier();
      if (tokenizer.tryConsume(":")) {
        clazz.addField(memberName, parseType(tokenizer));
        tokenizer.consume(";");
      } else if (tokenizer.currentValue.equals("(")) {
        clazz.addMethod(memberName, parseFunction(tokenizer));
      }
    }
    parsingContext.declare(clazz.name, clazz);
    return clazz;
  }

  Node parseDot(Node base, String name) {
    Classifier.Member member =
        ((Classifier) base.type).members.get(name);
    return new PropertyAccess(base, member);
  }

  Function parseFunction(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.consume("(");
    ArrayList<Function.Parameter> parameterList = new ArrayList<>();
    if (!tokenizer.tryConsume(")")) {
      do {
        String name = tokenizer.consumeIdentifier();
        tokenizer.consume(":");
        Type type = parsingContext.resolveType(tokenizer.consumeIdentifier());
        parameterList.add(new Function.Parameter(name, type));
      } while(tokenizer.tryConsume(","));
      tokenizer.consume(")");
    }
    tokenizer.consume("{");

    ParsingContext savedContext = parsingContext;
    parsingContext = new ParsingContext(parsingContext.self);
    for (Function.Parameter parameter : parameterList) {
      parsingContext.addLocal(parameter.name, parameter.type);
    }

    List<Statement> body = new ArrayList<>();
    Type type = parseBody(tokenizer, body);
    tokenizer.consume("}");

    parsingContext = savedContext;
    return new Function(type,
        parameterList.toArray(new Function.Parameter[parameterList.size()]),
        body.toArray(new Statement[body.size()]));
  }

  Statement parseInterface(ExpressionParser.Tokenizer tokenizer) {
    throw new RuntimeException("NYI");
  }

  Statement parseStatement(ExpressionParser.Tokenizer tokenizer, boolean cli) {
    Statement result;
   /* if (tokenizer.tryConsume("function")) {
      result = new Statement(Statement.Kind.EXPRESSION, parseFunction(context, tokenizer));
    } else */
    if (tokenizer.tryConsume("class")) {
      result = new Statement(parseClass(tokenizer));
    } else if (tokenizer.tryConsume("interface")) {
      result = parseInterface(tokenizer);
    } else if (tokenizer.tryConsume("return")) {
      result = new Statement(Statement.Kind.RETURN,
          expressionParser.parse(tokenizer));
    } else {
      result = new Statement(cli ? Statement.Kind.RETURN : Statement.Kind.EXPRESSION,
          expressionParser.parse(tokenizer));
    }
    if (!tokenizer.tryConsume(";") && !cli) {
      throw tokenizer.exception("Semicolon expected after statement.", null);
    }
    return result;
  }

  Node parseNew(ExpressionParser.Tokenizer tokenizer) {
    String className = tokenizer.consumeIdentifier();
    Classifier clazz = (Classifier) parsingContext.resolveType(className);
    tokenizer.consume("(");
    ArrayList<Node> args = new ArrayList<>();
    if (!tokenizer.currentValue.equals(")")) {
      do {
        args.add(expressionParser.parse(tokenizer));
      } while (tokenizer.tryConsume(","));
    }
    tokenizer.consume(")");
    return new New(clazz, args.toArray(new Node[args.size()]));
  }

  Type parseType(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.consumeIdentifier();
    return parsingContext.resolveType(name);
  }


  class ExpressionProcessor extends ExpressionParser.Processor<Node> {
    @Override
    public Node primary(String name, ExpressionParser.Tokenizer tokenizer) {
      if (name.equals("function")) {
        return new Literal(parseFunction(tokenizer));
      }
      if (name.equals("new")) {
        return parseNew(tokenizer);
      }
      throw new RuntimeException("NYI");
    }

    @Override
    public Node identifier(String name) {
      Object o = parsingContext.resolve(name);
      if (o instanceof LocalVariable) {
        LocalVariable var = (LocalVariable) o;
        return new LocalVariable(var.name, var.type, var.index);
      }
      if (o instanceof Classifier.Member) {
        Classifier.Member member = (Classifier.Member) o;
        return new PropertyAccess(new This(parsingContext.self), member);
      }
      if (o instanceof Classifier) {
        return new Literal(o);
      }

      throw new RuntimeException("Undeclared variable: " + name);
  }

    @Override
    public Node infixOperator(String name, Node left, Node right) {
      Wasm.Op op;
      switch(name.charAt(0)) {
        case '+': op = Wasm.Op.F64Add; break;
        case '-': op = Wasm.Op.F64Sub; break;
        case '*': op = Wasm.Op.F64Mul; break;
        case '/': op = Wasm.Op.F64Div; break;
        default:
          return super.infixOperator(name, left, right);
      }
      return new Operator(op, left, right);
    }

    @Override
    public Node suffixOperator(String name, Node param, ExpressionParser.Tokenizer tokenizer) {
      if (name.equals(".")) {
        String propertyName = tokenizer.consumeIdentifier();
        return parseDot(param, propertyName);
      }
      return super.suffixOperator(name, param, tokenizer);
    }

    @Override
    public Node stringLiteral(String rawValue) {
      return new Literal(ExpressionParser.unquote(rawValue));
    }

    @Override
    public Node numberLiteral(String value) {
      return new Literal(Double.parseDouble(value));
    }

    @Override
    public Node apply(Node to, String bracket, List<Node> parameterList) {
      return new Apply(to, parameterList.toArray(new Node[parameterList.size()]));
    }
  }
}
