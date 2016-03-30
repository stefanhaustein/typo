package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.type.Type;
import org.kobjects.expressionparser.demo.thin.type.Types;

class Literal implements Expression {

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

  public Literal(Object value) {
    this.value = value;
  }

  public String toString() {
    return value instanceof String
        ? quote((String) value) : String.valueOf(value);
  }

  @Override
  public Expression resolve(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    return this;
  }

  @Override
  public Object eval(org.kobjects.expressionparser.demo.thin.EvaluationContext context) {
    return value;
  }

  @Override
  public Type type() {
    return Types.typeOf(value);
  }

  @Override
  public void resolveSignatures(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
  }
}
