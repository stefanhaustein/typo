package org.kobjects.expressionparser.demo.typo;

class Literal extends Node {

  static String quote(String s) {
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

  Literal(Object value) {
    super(Type.of(value));
    this.value = value;
  }

  public String toString() {
    return value instanceof String ? quote((String) value) : String.valueOf(value);
  }

  @Override
  public Object eval(EvaluationContext context) {
    return value;
  }
}
