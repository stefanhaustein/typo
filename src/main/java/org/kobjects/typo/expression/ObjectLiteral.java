package org.kobjects.typo.expression;

import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.Interface;
import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;

import java.util.LinkedHashMap;

public class ObjectLiteral extends ExpressionN {
  Interface type;
  String[] names;
  public ObjectLiteral(String[] names, Expression[] expressions) {
    super(expressions);
    this.names = names;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    super.resolve(context);
    type = new Interface(null);
    for (int i = 0; i < names.length; i++) {
      type.addMember(names[i], children[i].type());
    }
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<>();
    for (int i = 0; i < names.length; i++) {
      result.put(names[i], children[i].eval(context));
    }
    return result;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("{");
    if (children.length > 0) {
      cp.indent();
      cp.newLine();
      cp.append(names[0]);
      cp.append(": ");
      children[0].print(cp);
      for (int i = 1; i < children.length; i++) {
        cp.append(",");
        cp.newLine();
        cp.append(names[i]);
        cp.append(": ");
        children[i].print(cp);
      }
      cp.outdent();
      cp.newLine();
    }
    cp.append("}");
  }

  @Override
  public Interface type() {
    return type;
  }
}
