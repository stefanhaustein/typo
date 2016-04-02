package org.kobjects.typo.parser;

import java.util.LinkedHashMap;

import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.Field;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;

public class ParsingContext {
  public TsClass self;
  public LinkedHashMap<String, LocalDeclaration> locals = new LinkedHashMap<>();
  LinkedHashMap<String, Object> statics;

  public ParsingContext(ParsingContext parent, TsClass self) {
    this.self = self;
    if (parent != null) {
      statics = parent.statics;
    } else {
      statics = new LinkedHashMap<>();
      statics.put("boolean", Types.BOOLEAN);
      statics.put("number", Types.NUMBER);
      statics.put("string", Types.STRING);
      statics.put("void", Types.VOID);
    }
  }

  public LocalDeclaration declareLocal(String name, Type type) {
    if (locals.containsKey(name)) {
      throw new RuntimeException("Duplicate variable '" + name + "'");
    }
    LocalDeclaration declaration = new LocalDeclaration(name, type, locals.size());
    locals.put(name, declaration);
    return declaration;
  }

  public void declareStatic(String name, Object value) {
    statics.put(name, value);
  }

  public Field resolveField(String name) {
    if (locals.containsKey(name)) {
      return locals.get(name);
    }
    if (self != null && self.members.containsKey(name)) {
      return self.members.get(name);
    }
    return null;
  }

  public Object resolveStatic(String name) {
    return statics.get(name);
  }

  public static class LocalDeclaration implements Field {
    public String name;
    public Type type;
    public int localIndex;

    LocalDeclaration(String name, Type type, int localIndex) {
      this.name = name;
      this.type = type;
      this.localIndex = localIndex;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public void set(EvaluationContext context, Object value) {
      context.locals[localIndex] = value;
    }

    @Override
    public Type type() {
      return type;
    }

    @Override
    public Object get(EvaluationContext context) {
      return context.locals[localIndex];
    }
  }
}
