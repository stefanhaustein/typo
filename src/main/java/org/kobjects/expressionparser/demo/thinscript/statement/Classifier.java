package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.Applicable;
import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.Field;
import org.kobjects.expressionparser.demo.thinscript.Instance;
import org.kobjects.expressionparser.demo.thinscript.Printable;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.expression.Function;
import org.kobjects.expressionparser.demo.thinscript.type.MetaType;
import org.kobjects.expressionparser.demo.thinscript.type.Type;
import org.kobjects.expressionparser.demo.thinscript.type.Typed;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class Classifier extends Statement implements Type, Typed {

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
  public void resolve(ParsingContext context) {
  }

  @Override
  public Object eval(EvaluationContext context) {
    return NO_RESULT;
  }

  public Instance newInstance(EvaluationContext context, Object... args) {
    return new Instance(this);
  }

  @Override
  public Type resolveType(ParsingContext context) {
    return this;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("class ").append(name).append(" {");
    if (members.size() > 0) {
      cp.indent();
      for (Member member : members.values()) {
        cp.newLine();
        if (member.implementation == null) {
          cp.append(member.name);
          cp.append(": ");
          cp.append(member.type);
        } else {
          ((Printable) member.implementation).print(cp);
        }
      }
      cp.outdent();
      cp.newLine();
    }
    cp.append("}");
  }

  public Type type() {
    return new MetaType(this);
  }


  public static class Member implements Field {
    EnumSet<Modifier> modifiers;
    String name;
    Type type;
    public int fieldIndex;
    public Applicable implementation;

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
