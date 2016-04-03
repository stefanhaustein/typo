package org.kobjects.typo.parser;

import java.util.LinkedHashMap;

import org.kobjects.typo.expression.Function;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Types;

public class ParsingContext {
  public LinkedHashMap<String, LocalDeclaration> locals = new LinkedHashMap<>();
  private LinkedHashMap<String, Object> statics;
  ParsingContext parent;
  Function function;
  //LinkedHashMap<String, ParsingContext> subContexts = new LinkedHashMap<>();

  public ParsingContext(ParsingContext parent, String name) {
    this.parent = parent;
    this.statics = new LinkedHashMap<>();
    //parent.subContexts.put(name, this);
  }

  public ParsingContext(ParsingContext parent) {
    this(parent, (Function) null);
  }

  public ParsingContext(ParsingContext parent, Function function) {
    this.parent = parent;
    this.function = function;
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

  public LocalDeclaration resolveField(String name) {
    if (locals.containsKey(name)) {
      return locals.get(name);
    }
   /* if (self != null && self.members.containsKey(name)) {
      return self.members.get(name);
    }*/
    if (parent != null && parent.locals.containsKey(name) && function != null) {
      LocalDeclaration parentField = parent.resolveField(name);
      LocalDeclaration localField = declareLocal(parentField.name(), parentField.type());
      function.closures.add(new Function.Closure(parentField, localField));
      return localField;
    }

    return null;
  }

  public Object resolveStatic(String name) {
    Object result = statics.get(name);
    return result != null ? result : parent != null ? parent.resolveStatic(name) : null;
  }

  /*public ParsingContext getSubContext(String name) {
    return subContexts.get(name);
  }*/

  public static class LocalDeclaration {
    public String name;
    public Type type;
    public int localIndex;

    LocalDeclaration(String name, Type type, int localIndex) {
      this.name = name;
      this.type = type;
      this.localIndex = localIndex;
    }

    public String name() {
      return name;
    }

    public void set(EvaluationContext context, Object value) {
      context.locals[localIndex] = value;
    }

    public Type type() {
      return type;
    }

    public Object get(EvaluationContext context) {
      return context.locals[localIndex];
    }
  }
}
