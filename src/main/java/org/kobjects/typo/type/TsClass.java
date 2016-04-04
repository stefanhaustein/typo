package org.kobjects.typo.type;

import org.kobjects.typo.io.Printable;
import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.expression.Expression;
import org.kobjects.typo.expression.Function;
import org.kobjects.typo.parser.ParsingContext;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TsClass extends Classifier {

  private static void printModifiers(CodePrinter cp, Set<Modifier> modifiers) {
    for (Modifier m: modifiers) {
      cp.append(m.name().toLowerCase());
      cp.append(' ');
    }
  }

  public enum Modifier {PUBLIC, PRIVATE, PROTECTED, STATIC}

  final String name;
  public Function constructor;

  public Map<String, Member> members = new LinkedHashMap<>();
  public int fieldCount;

  public Set<Type> interfaces = new LinkedHashSet<Type>();

  public TsClass(String name) {
    this.name = name;
  }

  public void addImplements(Type type) {
    interfaces.add(type);
  }

  @Override
  public String name() {
    return name;
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

  public void addMethod(Set<Modifier> modifiers, String name, Function function) {
    Member member = new Member();
    member.modifiers = modifiers;
    member.name = name;
    member.staticValue = function;
    member.type = function.type();  // For builtins.
    member.fieldIndex = -1;
    members.put(name, member);
  }

  @Override
  public boolean assignableFrom(Type other) {
    return this == other || other == Types.NULL;
  }

  @Override
  public void resolveMembers(ParsingContext context) {
    if (constructor != null) {
      constructor = constructor.resolve(context);
    }
    for (Member member: members.values()) {
      if (member.initializer != null) {
        // TODO: This won't work for non-static initializers; they should be moved to the ctor
        // instead.
        member.initializer = member.initializer.resolve(context);
        if (member.isStatic()) {
          member.staticValue = member.initializer.eval(new EvaluationContext(null, 0));
        }
      }
      if (member.staticValue instanceof Function) {
        member.staticValue = ((Function) member.staticValue).resolve(context);
      }
    }
  }

  @Override
  public Type propertyType(String key) {
    return members.get(key).type();
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    if (constructor != null) {
      constructor.resolveSignatures(context);
    }
    for (Member member: members.values()) {
      if (member.staticValue instanceof Function) {
        ((Function) member.staticValue).resolveSignatures(context);
        member.type = Types.typeOf(member.staticValue);
      } else {
        member.type = member.type.resolve(context);
      }
    }
  }

  @Override
  public Type resolve(ParsingContext context) {
    // Member types are resolved in resolveSignatures already.
    return this;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("class ").append(name).append(" {");
    if (members.size() > 0 || constructor != null) {
      cp.indent();

      for (Member member : members.values()) {
        if (!(member.staticValue instanceof Function)) {
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
        if (member.staticValue instanceof Function) {
          cp.newLine();
          printModifiers(cp, member.modifiers);
          ((Printable) member.staticValue).print(cp);
        }
      }
      cp.outdent();
      cp.newLine();
    }
    cp.append("}");
  }

  @Override
  public String toString() {
    return "class " + name;
  }

  public static class Member {
    Set<Modifier> modifiers;
    String name;
    Type type;
    public int fieldIndex;
    public Expression initializer;
    public Object staticValue;

    public String name() {
      return name;
    }

    public void set(EvaluationContext context, Object value) {
      if (fieldIndex == -1) {
        staticValue = value;
      } else {
        context.self.setField(fieldIndex, value);
      }
    }

    public Type type() {
      return type;
    }

    public Object get(EvaluationContext context) {
      return fieldIndex == -1 ? staticValue : context.self.fields[fieldIndex];
    }

    public boolean isStatic() {
      return modifiers.contains(Modifier.STATIC);
    }
  }
}
