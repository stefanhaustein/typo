package org.kobjects.expressionparser.demo.thin;

import java.util.LinkedHashMap;

import org.kobjects.expressionparser.demo.thin.ast.Classifier;
import org.kobjects.expressionparser.demo.thin.type.Type;
import org.kobjects.expressionparser.demo.thin.type.Types;

public class ParsingContext {
  public Classifier self;
  LinkedHashMap<String, LocalDeclaration> locals = new LinkedHashMap<>();
  // Move into classifiers
  LinkedHashMap<String, Type> types = new LinkedHashMap<>();

  public ParsingContext(Classifier self) {
    this.self = self;
  }

  public int addLocal(String name, Type type) {
    if (locals.containsKey(name)) {
      throw new RuntimeException("Duplicate variable '" + name + "'");
    }
    locals.put(name, new LocalDeclaration(name, type, locals.size()));
    return locals.size() - 1;
  }

  public Object resolve(String name) {
    if (locals.containsKey(name)) {
      return locals.get(name);
    }
    if (self.members.containsKey(name)) {
      return self.members.get(name);
    }
    return resolveType(name);
  }

  Type resolveType(String s) {
    if (types.containsKey(s)) {
      return types.get(s);
    }
    if (s.equals("number")) {
      return Type.NUMBER;
    }
    if (s.equals("string")) {
      return Type.STRING;
    }

    throw new RuntimeException("Unknown returnType: '" + s + "'");
  }

  public void declare(Type clazz) {
    types.put(clazz.name(), clazz);
  }

  public Type typeOf(Object o) {
    return Types.typeOf(o);
  }

  public static class LocalDeclaration {
    public String name;
    public Type type;
    public int localIndex;

    LocalDeclaration(String name, Type type, int localIndex) {
      this.name = name;
      this.type = type;
      this.localIndex = localIndex;
    }
  }

}
