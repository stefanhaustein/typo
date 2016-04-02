package org.kobjects.typo.expression;

import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.statement.Interface;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;

import java.util.LinkedHashMap;

public class ObjectLiteral extends Node {

  String[] names;
  public ObjectLiteral(String[] names, Expression[] expressions) {
    super(null, expressions);
    this.names = names;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    resolveChildren(context);
    Interface type = new Interface(null);
    for (int i = 0; i < names.length; i++) {
      type.addMember(names[i], children[i].type());
    }
    this.type = type;
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
      children[0].print(cp);
      for (int i = 1; i < children.length; i++) {
        cp.append(",");
        cp.newLine();
        cp.append(names[i]);
        children[i].print(cp);
      }
      cp.outdent();
      cp.newLine();
    }
    cp.append("}");
  }
}
