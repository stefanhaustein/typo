package org.kobjects.typo.type;

import org.kobjects.typo.expression.Expression;
import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.StaticMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class Interface extends Classifier {
  Map<String, Interface.Member> members = new LinkedHashMap<>();
  String name;
  boolean resolved;

  public Interface(String name) {
    this.name = name;
  }

  public void addMember(String name, Type type) {
    members.put(name, new Interface.Member(name, type));
  }


  @Override
  public boolean assignableFrom(Type type) {
    if (type == this || type == Types.NULL) {
      return true;
    }
    if (!(type instanceof Classifier)) {
      return false;
    }
    Classifier other = (Classifier) type;
    for (Member own : members.values()) {
      if (!own.type.assignableFrom(other.member(own.name).type())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Interface)) {
      return false;
    }
    Interface other = (Interface) o;
    return assignableFrom(other) && other.assignableFrom(this);
  }

  @Override
  public Interface.Member member(String name) {
    return members.get(name);
  }

  @Override
  public String name() {
    if (name != null) {
      return name;
    }
    StringBuilder sb = new StringBuilder("{");
    for (Interface.Member member : members.values()) {
      sb.append(member.name()).append(": ").append(member.type().name()).append("; ");
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("interface ").append(name).append(" {");
    if (members.size() > 0) {
      cp.indent();
      for (Interface.Member member : members.values()) {
        cp.newLine();
        cp.append(member.name()).append(": ").append(member.type().name()).append(";");
      }
      cp.outdent();
      cp.newLine();
    }
    cp.append("}");
  }

  @Override
  public Type resolve(ParsingContext context) {
    if (name == null) {
      resolveSignatures(context);
    }
    return this;
  }

  @Override
  public void resolveMembers(ParsingContext context) {
  }


  @Override
  public void resolveSignatures(ParsingContext context) {
    for (Interface.Member member: members.values()) {
      member.type = member.type.resolve(context);
    }
    this.resolved = true;
  }

  @Override
  public String toString() {
    return "interface " + name;
  }

  static class Member implements Classifier.Member {
    private final String name;
    private Type type;

    private Member(String name, Type type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public Type type() {
      return type;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public void set(Object instance, Object value) {
      if (instance instanceof StaticMap) {
        ((StaticMap) instance).set(name, value);
      } else {
        Classifier.Member member = ((Classifier) Types.typeOf(instance)).member(name);
        member.set(instance, value);
      }
    }

    @Override
    public Object get(Object instance) {
      if (instance instanceof StaticMap) {
        return ((StaticMap) instance).get(name);
      } else {
        try {
          Classifier.Member member = ((Classifier) Types.typeOf(instance)).member(name);
          return member.get(instance);
        } catch (Exception e) {
          System.out.println("get " + name + " of instance: " + instance);
          return null;
        }

      }
    }
  }
}
