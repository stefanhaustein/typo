package org.kobjects.expressionparser.demo.thin;

import java.util.LinkedHashMap;

import org.kobjects.expressionparser.demo.thin.ast.Classifier;
import org.kobjects.expressionparser.demo.thin.type.Type;

public class ParsingContext {
  public Classifier self;
  LinkedHashMap<String, LocalDeclaration> locals = new LinkedHashMap<>();
  LinkedHashMap<String, Object> statics;

  public ParsingContext(ParsingContext parent, Classifier self) {
    this.self = self;
    if (parent != null) {
      statics = parent.statics;
    } else {
      statics = new LinkedHashMap<>();
      statics.put("number", Type.NUMBER);
      statics.put("string", Type.STRING);
      statics.put("void", Type.VOID);
    }
  }

  public int declareLocal(String name, Type type) {
    if (locals.containsKey(name)) {
      throw new RuntimeException("Duplicate variable '" + name + "'");
    }
    locals.put(name, new LocalDeclaration(name, type, locals.size()));
    return locals.size() - 1;
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
