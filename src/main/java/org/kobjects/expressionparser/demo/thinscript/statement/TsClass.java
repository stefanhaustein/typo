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

public class TsClass extends Statement implements Type {

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

  public TsClass(String name) {
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
      if (member.staticValue instanceof Function) {
        ((Function) member.staticValue).resolveSignatures(context);
        member.type = Types.typeOf(member.staticValue);
        if (member.type == null) {
          throw new RuntimeException("WTF?");
        }
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
    member.staticValue = applicable;
    member.type = applicable.type();  // For builtins.
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
      if (member.staticValue instanceof Function) {
        member.staticValue = ((Function) member.staticValue).resolve(context);
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
        if (!(member.staticValue instanceof Applicable)) {
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
        if (member.staticValue instanceof Applicable) {
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

  public static class Member implements Field {
    Set<Modifier> modifiers;
    String name;
    Type type;
    public int fieldIndex;
    public Expression initializer;
    public Object staticValue;

    @Override
    public String name() {
      return name;
    }

    @Override
    public void set(EvaluationContext context, Object value) {
      if (fieldIndex == -1) {
        staticValue = value;
      } else {
        context.self.setField(fieldIndex, value);
      }
    }

    @Override
    public Type type() {
      return type;
    }

    @Override
    public Object get(EvaluationContext context) {
      return fieldIndex == -1 ? staticValue : context.self.fields[fieldIndex];
    }

    public boolean isStatic() {
      return modifiers.contains(Modifier.STATIC);
    }
  }
}
