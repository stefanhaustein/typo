package org.kobjects.typo.parser;

import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.typo.expression.Apply;
import org.kobjects.typo.expression.Expression;
import org.kobjects.typo.expression.Function;
import org.kobjects.typo.expression.Literal;
import org.kobjects.typo.expression.New;
import org.kobjects.typo.expression.ObjectLiteral;
import org.kobjects.typo.expression.Ternary;
import org.kobjects.typo.expression.UnresolvedIdentifier;
import org.kobjects.typo.expression.UnresolvedOperator;
import org.kobjects.typo.expression.UnresolvedProperty;
import org.kobjects.typo.statement.Block;
import org.kobjects.typo.statement.ClassifierDeclaration;
import org.kobjects.typo.statement.ExpressionStatement;
import org.kobjects.typo.statement.IfStatement;
import org.kobjects.typo.type.Interface;
import org.kobjects.typo.statement.LetStatement;
import org.kobjects.typo.statement.ReturnStatement;
import org.kobjects.typo.statement.Statement;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.statement.WhileStatement;
import org.kobjects.typo.type.ArrayType;
import org.kobjects.typo.type.FunctionType;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;
import org.kobjects.typo.type.UnresolvedType;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
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
    expressionParser.addPrimary("{");
    expressionParser.addOperators(ExpressionParser.OperatorType.SUFFIX, 18, ".");
    expressionParser.addApplyBrackets(17, "(", ",", ")");
    expressionParser.addOperators(ExpressionParser.OperatorType.PREFIX, 15, "-", "!", "~");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 14, "*", "/");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 13, "+", "-");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 11, "<", ">", "<=", ">=");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 10, "===", "==", "!=", "!==");
    expressionParser.addTernaryOperator(4, "?", ":");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 3, "=");
  }

  public ExpressionParser.Tokenizer createTokenizer(Reader reader) {
    return new ExpressionParser.Tokenizer(new Scanner(reader), expressionParser.getSymbols(), "{", "}", "[", "]", ";", ":", "=>");
  }

  public Statement parseBlock(ExpressionParser.Tokenizer tokenizer, Collection<NamedEntity> statics) {
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

  TsClass parseClass(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.consumeIdentifier();
    TsClass classifier = new TsClass(name);
    if (tokenizer.tryConsume("implements")) {
      do {
        classifier.addImplements(parseType(tokenizer));
      } while(tokenizer.tryConsume(","));
    }
    tokenizer.consume("{");
    while (!tokenizer.tryConsume("}")) {
      Set<TsClass.Modifier> modifiers = parseModifiers(
          tokenizer, EnumSet.allOf(TsClass.Modifier.class));
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
  Function parseFunction(TsClass owner, String name, ExpressionParser.Tokenizer tokenizer) {
    tokenizer.consume("(");
    ArrayList<FunctionType.Parameter> parameterList = new ArrayList<>();
    List<Statement> init = new ArrayList<>();
    boolean isConstructor = owner != null && name.equals("constructor");
    if (!tokenizer.tryConsume(")")) {
      Set<TsClass.Modifier> permittedModifiers = !isConstructor ?
          EnumSet.noneOf(TsClass.Modifier.class) : EnumSet.of(
          TsClass.Modifier.PUBLIC, TsClass.Modifier.PRIVATE, TsClass.Modifier.PROTECTED);
      do {
        Set<TsClass.Modifier> modifiers = parseModifiers(tokenizer, permittedModifiers);
            String parameterName = tokenizer.consumeIdentifier();
        tokenizer.consume(":");
        Type parameterType = parseType(tokenizer);
        if (!modifiers.isEmpty()) {
          owner.addField(modifiers, parameterName, parameterType, null);
          init.add(new ExpressionStatement(new UnresolvedOperator("=",
              new UnresolvedProperty(new UnresolvedIdentifier("this"), parameterName),
              new UnresolvedIdentifier(parameterName))));
        }
        parameterList.add(new FunctionType.Parameter(parameterName, parameterType));
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
        parameterList.toArray(new FunctionType.Parameter[parameterList.size()]),
        returnType,
        body);
    return fn;
  }

  Interface parseInterface(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.consumeIdentifier();
    Interface itf = new Interface(name);
    tokenizer.consume("{");
    while (!tokenizer.tryConsume("}")) {
      String memberName = tokenizer.consumeIdentifier();
      tokenizer.consume(":");
      Type type = parseType(tokenizer);
      tokenizer.consume(";");
      itf.addMember(memberName, type);
    }
    return itf;
  }

  private EnumSet<TsClass.Modifier> parseModifiers(
      ExpressionParser.Tokenizer tokenizer, Set<TsClass.Modifier> permitted) {
    EnumSet<TsClass.Modifier> result = EnumSet.noneOf(TsClass.Modifier.class);
    boolean added;
    do {
      added = false;
      for (TsClass.Modifier modifier : TsClass.Modifier.values()) {
        if (tokenizer.tryConsume(modifier.name().toLowerCase())) {
          if (!permitted.contains(modifier)) {
            throw new RuntimeException("Modifier '" + modifier.name().toLowerCase() + "' not permitted here.");
          }
          result.add(modifier);
          added = true;
        }
      }
    } while (added);
    return result;
  }

  private ObjectLiteral parseObjectLiteral(ExpressionParser.Tokenizer tokenizer) {
    List<String> names = new ArrayList<>();
    List<Expression> expressions = new ArrayList();
    if (!tokenizer.tryConsume("}")) {
      do {
        String name = tokenizer.consumeIdentifier();
        names.add(name);
        if (tokenizer.tryConsume(":")) {
          expressions.add(expressionParser.parse(tokenizer));
        } else {
          expressions.add(new UnresolvedIdentifier(name));
        }
      } while (tokenizer.tryConsume(","));
      tokenizer.consume("}");
    }
    return new ObjectLiteral(names.toArray(new String[names.size()]),
        expressions.toArray(new Expression[expressions.size()]));
  }

  private Statement parseStatement(ExpressionParser.Tokenizer tokenizer, Collection<NamedEntity> statics) {
    Statement result;
    if (tokenizer.tryConsume("{")) {
      result = parseBlock(tokenizer, null);
      tokenizer.consume("}");
    } else if (tokenizer.tryConsume("class")) {
      TsClass classifier = parseClass(tokenizer);
      if (statics == null) {
        throw new RuntimeException("Classes only permitted at top level.");
      }
      statics.add(classifier);
      result = new ClassifierDeclaration(classifier);
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
      Interface itf = parseInterface(tokenizer);
      if (statics == null) {
        throw new RuntimeException("Interfaces only permitted at top level.");
      }
      statics.add(itf);
      result = new ClassifierDeclaration(itf);
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
        statics.add((Function) expression);
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
    Type result;
    if (tokenizer.tryConsume("(")) {
      ArrayList<FunctionType.Parameter> args = new ArrayList<>();
      while(!tokenizer.tryConsume(")")) {
        String name = tokenizer.consumeIdentifier();
        tokenizer.consume(":");
        Type type = parseType(tokenizer);
        args.add(new FunctionType.Parameter(name, type));
      }
      tokenizer.consume("=>");
      Type returnType = parseType(tokenizer);
      result = new FunctionType(returnType, args.toArray(new FunctionType.Parameter[args.size()]));
    } else {
      String name = tokenizer.consumeIdentifier();
      if (name.equals("boolean")) {
        result = Types.BOOLEAN;
      } else if (name.equals("number")) {
        result = Types.NUMBER;
      } else if (name.equals("string")) {
        result = Types.STRING;
      } else if (name.equals("void")) {
        result = Types.VOID;
      } else {
        result = new UnresolvedType(name);
      }
    }
    while (tokenizer.tryConsume("[")) {
      tokenizer.consume("]");
      result = new ArrayType(result);
    }
    return result;
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
      if (name.equals("{")) {
        return parseObjectLiteral(tokenizer);
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
    public Expression prefixOperator(ExpressionParser.Tokenizer tokenizer, String name, Expression param) {
      return new UnresolvedOperator(name, param);
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
      return new Literal(Double.parseDouble(value), null);
    }

    @Override
    public Expression apply(ExpressionParser.Tokenizer tokenizer, Expression to, String bracket, List<Expression> parameterList) {
      return new Apply(to, parameterList.toArray(new Expression[parameterList.size()]));
    }
  }
}
