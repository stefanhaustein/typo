package org.kobjects.expressionparser.demo.thin.ast;

import org.kobjects.expressionparser.demo.thin.type.MetaType;
import org.kobjects.expressionparser.demo.thin.type.Type;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class Classifier implements org.kobjects.expressionparser.demo.thin.ast.Expression, Type {


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

  public String name() {
    return name;
  }

  @Override
  public void resolveSignatures(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
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

  public Member addField(String name, Type type) {
    Member member = new Member();
    member.name = name;
    member.type = type;
    member.fieldIndex = fieldCount++;
    members.put(name, member);
    return member;
  }

  public void addMethod(String name, org.kobjects.expressionparser.demo.thin.Applicable applicable) {
    Member member = new Member();
    member.name = name;
    member.implementation = applicable;
    member.fieldIndex = -1;
    members.put(name, member);
  }

  public void addMember(Member member) {
    members.put(member.name, member);
  }


  @Override
  public org.kobjects.expressionparser.demo.thin.ast.Expression resolve(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    return this;
  }

  @Override
  public Object eval(org.kobjects.expressionparser.demo.thin.EvaluationContext context) {
    return this;
  }

  public org.kobjects.expressionparser.demo.thin.Instance newInstance(org.kobjects.expressionparser.demo.thin.EvaluationContext context, Object... args) {
    return new org.kobjects.expressionparser.demo.thin.Instance(this);
  }

  @Override
  public Type resolveType(org.kobjects.expressionparser.demo.thin.ParsingContext context) {
    return this;
  }

  @Override
  public Type type() {
    return new MetaType(this);
  }


  public static class Member {
    EnumSet<Modifier> modifiers;
    String name;
    Type type;
    public int fieldIndex;
    org.kobjects.expressionparser.demo.thin.Applicable implementation;
  }
}
