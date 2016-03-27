package org.kobjects.expressionparser.demo.typo;

import java.util.LinkedHashMap;
import java.util.Map;

class Classifier extends Type {
  enum Kind {CLASS, INTERFACE};

  final Kind kind;
  int fieldCount;
  Map<String, Member> members = new LinkedHashMap<>();

  Classifier(Kind kind, String name) {
    super(name, null);
    this.kind = kind;
  }

  void addField(String name, Type type) {
    Member member = new Member();
    member.name = name;
    member.type = type;
    member.fieldIndex = fieldCount++;
    members.put(name, member);
  }

  void addMethod(String name, Applicable f) {
    Member member = new Member();
    member.name = name;
    member.type = f.type();
    member.implementation = f;
    member.fieldIndex = -1;
    members.put(name, member);
  }

  Instance newInstance(EvaluationContext context, Object... children) {
    return new Instance(this);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("class ");
    sb.append(name);
    sb.append(" {");
    for (Map.Entry<String,Member> e: members.entrySet()) {
      sb.append(e.getKey());
      sb.append(": ");
      sb.append(e.getValue());
      sb.append("; ");
    }
    sb.append("}");
    return sb.toString();
  }

  static class Member {
    String name;
    Type type;
    int fieldIndex;
    Applicable implementation;
  }
}
