package org.kobjects.typo.expression;

import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;

public class Literal extends Expression {

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
  public Type type() {
    return Types.typeOf(value);
  }

}
