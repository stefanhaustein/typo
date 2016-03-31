package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.Applicable;
import org.kobjects.expressionparser.demo.thin.EvaluationContext;
import org.kobjects.expressionparser.demo.thin.Field;
import org.kobjects.expressionparser.demo.thin.Instance;
import org.kobjects.expressionparser.demo.thin.ParsingContext;
import org.kobjects.expressionparser.demo.thin.type.MetaType;
import org.kobjects.expressionparser.demo.thin.type.Type;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class Classifier implements Expression, Type {

  public enum Kind {CLASS, INTERFACE};
  public enum Modifier {STATIC, PUBLIC}

  final Kind kind;
  final String name;

  public Map<String, Member> members = new LinkedHashMap<>();
  public int fieldCount;

  public Classifier(Kind kind, String name) {
    this.kind = kind;
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    for (Member member: members.values()) {
      if (member.implementation != null) {
        if (member.implementation instanceof Function) {
          ((Function) member.implementation).resolveSignatures(context);
        }
        member.type = member.implementation.type();
      } else {
        member.type.resolveType(context);
      }
    }
  }

  public void assign(EvaluationContext context, Object value) {
    throw new UnsupportedOperationException();
  }

  public Member addField(String name, Type type) {
    Member member = new Member();
    member.name = name;
    member.type = type;
    member.fieldIndex = fieldCount++;
    members.put(name, member);
    return member;
  }

  public void addMethod(String name, Applicable applicable) {
    Member member = new Member();
    member.name = name;
    member.implementation = applicable;
    member.fieldIndex = -1;
    members.put(name, member);
  }

  @Override
  public boolean isAssignable() {
    return false;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return this;
  }

  public Instance newInstance(EvaluationContext context, Object... args) {
    return new Instance(this);
  }

  @Override
  public Type resolveType(ParsingContext context) {
    return this;
  }

  @Override
  public Type type() {
    return new MetaType(this);
  }


  public static class Member implements Field {
    EnumSet<Modifier> modifiers;
    String name;
    Type type;
    public int fieldIndex;
    Applicable implementation;

    @Override
    public String name() {
      return name;
    }

    @Override
    public void set(EvaluationContext context, Object value) {
      context.self.setField(fieldIndex, value);
    }

    @Override
    public Type type() {
      return type;
    }

    @Override
    public Object get(EvaluationContext context) {
      return context.self.fields[fieldIndex];
    }
  }
}
