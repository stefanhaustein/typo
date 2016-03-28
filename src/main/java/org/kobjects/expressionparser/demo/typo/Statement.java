package org.kobjects.expressionparser.demo.typo;

class Statement {
  enum Kind {RETURN, EXPRESSION, TYPE_DECLARATION, LET};

  static Object NO_RETURN = new Object();

  int index;
  Kind kind;
  Node expression;
  Type type;

  Statement(Kind kind, Node node) {
    this.type = kind == Kind.RETURN ? node.type : Type.NONE;
    this.kind = kind;
    this.expression = node;
  }

  Statement(int index, Node node) {
    this.kind = Kind.LET;
    this.type = Type.NONE;
    this.index = index;
    this.expression = node;
  }

  Statement(Type type) {
    this.type = type;
    this.expression = null;
    this.kind = Kind.TYPE_DECLARATION;
  }

  public Object eval(EvaluationContext context) {
    switch (kind) {
      case RETURN:
        return expression.eval(context);
      case EXPRESSION:
        expression.eval(context);
        return NO_RETURN;
      case LET:
        context.setLocal(index, expression.eval(context));
      case TYPE_DECLARATION:
        return NO_RETURN;
      default:
        throw new UnsupportedOperationException(kind.name());
    }
  }

  public String toString() {
    switch (kind) {
      case TYPE_DECLARATION: return type.toString();
      case RETURN: return "return " + expression + ";";
      default:
        return expression + ";";
    }
  }
}