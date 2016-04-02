package org.kobjects.typo.type;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class Interface implements Classifier {
  Map<String, Type> properties = new LinkedHashMap<String, Type>();
  String name;
  boolean resolved;

  public Interface(String name) {
    this.name = name;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    LinkedHashMap<String, Type> resolved = new LinkedHashMap<>();
    for (Map.Entry<String, Type> e : properties.entrySet()) {
      resolved.put(e.getKey(), e.getValue().resolve(context));
    }
    properties = resolved;
    this.resolved = true;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("interface ").append(name).append(" {");
    if (properties.size() > 0) {
      cp.indent();
      for (Map.Entry<String, Type> e : properties.entrySet()) {
        cp.newLine();
        cp.append(e.getKey()).append(": ").append(e.getValue().name()).append(";");
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
    if (name != null) {
      return name;
    }
    StringBuilder sb = new StringBuilder("{");
    for (Map.Entry<String, Type> e : properties.entrySet()) {
      sb.append(e.getKey()).append(": ").append(e.getValue().name()).append("; ");
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public Type resolve(ParsingContext context) {
    if (name == null) {
      resolveSignatures(context);
    }
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

  public Type propertyType(String name) {
    return properties.get(name);
  }

  @Override
  public void resolveMembers(ParsingContext context) {
  }
}
