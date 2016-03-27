package org.kobjects.expressionparser.demo.typo;

class Statement {
  enum Kind {RETURN, EXPRESSION, DECLARATION};

  static Object NO_RETURN = new Object();

  Kind kind;
  Node expression;
  Type type;

  Statement(Kind kind, Node node) {
    this.type = kind == Kind.RETURN ? node.type : Type.NONE;
    this.kind = kind;
    this.expression = node;
  }

  Statement(Type type) {
    this.type = type;
    this.expression = null;
    this.kind = Kind.DECLARATION;
  }

  public Object eval(EvaluationContext context) {
    switch (kind) {
      case RETURN:
        return expression.eval(context);
      case EXPRESSION:
        expression.eval(context);
        return NO_RETURN;
      case DECLARATION:
        return NO_RETURN;
      default:
        throw new UnsupportedOperationException(kind.name());
    }
  }

  public String toString() {
    switch (kind) {
      case DECLARATION: return type.toString();
      case RETURN: return "return " + expression + ";";
      default:
        return expression + ";";
    }
  }
}