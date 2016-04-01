package org.kobjects.expressionparser.demo.thinscript.expression;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Type;
import org.kobjects.expressionparser.demo.thinscript.type.Types;

public class Literal implements Expression {

  public static String quote(String s) {
    StringBuilder sb = new StringBuilder(s.length() + 2);
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\"': sb.append("\\\""); break;
        case '\n': sb.append("\\n"); break;
        default:
          sb.append(c);
      }
    }
    sb.append('"');
    return sb.toString();
  }

  final Object value;
  final String name;

  public Literal(Object value, String name) {
    this.value = value;
    this.name = name;
  }

  @Override
  public void assign(EvaluationContext context, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void print(CodePrinter cp) {
    if (name != null) {
      cp.append(name);
    } else if (value instanceof String) {
      cp.append(quote((String) value));
    } else {
      cp.append(value);
    }
  }

  @Override
  public Expression resolve(ParsingContext context) {
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return value;
  }

  @Override
  public boolean isAssignable() {
    return false;
  }


  @Override
  public Type type() {
    return Types.typeOf(value);
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
  }
}
