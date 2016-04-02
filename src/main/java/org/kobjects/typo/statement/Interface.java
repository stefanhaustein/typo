package org.kobjects.typo.statement;

import org.kobjects.typo.EvaluationContext;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class Interface extends Statement implements Type {
  Map<String, Type> properties = new LinkedHashMap<String, Type>();
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
    for (Map.Entry<String, Type> e : properties.entrySet()) {
      resolved.put(e.getKey(), e.getValue().resolveType(context));
    }
    properties = resolved;
  }

  @Override
  public void resolve(ParsingContext context) {
    // No-op
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("interface ").append(name).append(" {");
    if (properties.size() > 0) {
      cp.indent();
      for (Map.Entry<String, Type> e : properties.entrySet()) {
        cp.newLine();
        cp.append(e.getKey()).append(": ").append(e.getValue().name());
      }
      cp.outdent();
      cp.newLine();
    }
    cp.append("}");
  }

  public void addMember(String name, Type type) {
    properties.put(name, type);
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

  public Type getType(String name) {
    return properties.get(name);
  }
}
