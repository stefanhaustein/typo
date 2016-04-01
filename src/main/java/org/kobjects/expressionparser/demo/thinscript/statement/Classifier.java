package org.kobjects.expressionparser.demo.thinscript.statement;

import org.kobjects.expressionparser.demo.thinscript.Applicable;
import org.kobjects.expressionparser.demo.thinscript.CodePrinter;
import org.kobjects.expressionparser.demo.thinscript.EvaluationContext;
import org.kobjects.expressionparser.demo.thinscript.Field;
import org.kobjects.expressionparser.demo.thinscript.Printable;
import org.kobjects.expressionparser.demo.thinscript.expression.Expression;
import org.kobjects.expressionparser.demo.thinscript.parser.ParsingContext;
import org.kobjects.expressionparser.demo.thinscript.expression.Function;
import org.kobjects.expressionparser.demo.thinscript.type.Type;
import org.kobjects.expressionparser.demo.thinscript.type.Types;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Classifier extends Statement implements Type {

  private static void printModifiers(CodePrinter cp, Set<Modifier> modifiers) {
    for (Modifier m: modifiers) {
      cp.append(m.name().toLowerCase());
      cp.append(' ');
    }
  }

  public enum Kind {CLASS, INTERFACE};
  public enum Modifier {PUBLIC, PRIVATE, PROTECTED, STATIC}

  final Kind kind;
  final String name;
  public Function constructor;

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
    if (constructor != null) {
      constructor.resolveSignatures(context);
    }
    for (Member member: members.values()) {
      if (member.initializer != null) {
        member.initializer.resolveSignatures(context);
      }
      if (member.implementation != null) {
        if (member.implementation instanceof Function) {
          ((Function) member.implementation).resolveSignatures(context);
        }
        member.type = member.implementation.type();
      } else {
        member.type = member.type.resolveType(context);
      }
    }
  }

  public Member addField(Set<Modifier> modifiers, String name, Type type, Expression initialValue) {
    Member member = new Member();
    member.modifiers = modifiers;
    member.name = name;
    member.type = type;
    if (member.isStatic()) {
      member.fieldIndex = -1;
    } else {
      member.fieldIndex = fieldCount++;
    }
    member.initializer = initialValue;
    members.put(name, member);
    return member;
  }

  public void addMethod(Set<Modifier> modifiers, String name, Applicable applicable) {
    Member member = new Member();
    member.modifiers = modifiers;
    member.name = name;
    member.implementation = applicable;
    member.fieldIndex = -1;
    members.put(name, member);
  }

  @Override
  public boolean assignableFrom(Type other) {
    return this == other || other == Types.NULL;
  }

  @Override
  public void resolve(ParsingContext context) {
    if (constructor != null) {
      constructor = constructor.resolve(context);
    }
    for (Member member: members.values()) {
      if (member.initializer != null) {
        member.initializer = member.initializer.resolve(context);
        if (member.isStatic()) {
          member.staticValue = member.initializer.eval(new EvaluationContext(null, null));
        }
      }
      if (member.implementation instanceof Function) {
        member.implementation = ((Function) member.implementation).resolve(context);
      }
    }
  }

  @Override
  public Object eval(EvaluationContext context) {
    return NO_RESULT;
  }

  @Override
  public Type resolveType(ParsingContext context) {
    return this;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("class ").append(name).append(" {");
    if (members.size() > 0 || constructor != null) {
      cp.indent();

      for (Member member : members.values()) {
        if (member.implementation == null) {
          cp.newLine();
          printModifiers(cp, member.modifiers);
          cp.append(member.name);
          cp.append(": ");
          cp.append(member.type.name());
        }
      }

      if (constructor != null) {
        cp.newLine();
        constructor.print(cp);
      }

      for (Member member : members.values()) {
        if (member.implementation != null) {
          cp.newLine();
          printModifiers(cp, member.modifiers);
          ((Printable) member.implementation).print(cp);
        }
      }
      cp.outdent();
      cp.newLine();
    }
    cp.append("}");
  }


  public static class Member implements Field {
    Set<Modifier> modifiers;
    String name;
    Type type;
    public int fieldIndex;
    public Applicable implementation;
    public Expression initializer;
    public Object staticValue;

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

    public boolean isStatic() {
      return modifiers.contains(Modifier.STATIC);
    }
  }
}
