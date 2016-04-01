package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.type.Type;

import java.util.LinkedHashMap;
import java.util.Map;

public class Interface extends Statement implements Type {
  Map<String, Type> members = new LinkedHashMap<String, Type>();
  String name;

  public Interface(String name) {
    this.name = name;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return NO_RESULT;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    LinkedHashMap<String, Type> resolved = new LinkedHashMap<>();
    for (Map.Entry<String, Type> e : members.entrySet()) {
      resolved.put(e.getKey(), e.getValue().resolveType(context));
    }
    members = resolved;
  }

  @Override
  public void resolve(ParsingContext context) {
    // No-op
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("interface ").append(name).append(" {");
    if (members.size() > 0) {
      cp.indent();
      for (Map.Entry<String, Type> e : members.entrySet()) {
        cp.newLine();
        cp.append(e.getKey()).append(": ").append(e.getValue().name());
      }
      cp.outdent();
      cp.newLine();
    }
    cp.append("}");
  }

  public void addMember(String name, Type type) {
    members.put(name, type);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Type resolveType(ParsingContext context) {
    return this;
  }

  @Override
  public boolean assignableFrom(Type type) {
    return false;
  }

  @Override
  public String toString() {
    return "interface " + name;
  }
}
