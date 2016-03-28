package org.kobjects.expressionparser.demo.typo;

import java.util.LinkedHashMap;

class ParsingContext {
  Classifier self;
  LinkedHashMap<String, LocalDeclaration> locals = new LinkedHashMap<>();
  // Move into classifiers
  LinkedHashMap<String, Type> types = new LinkedHashMap<>();

  ParsingContext(Classifier self) {
    this.self = self;
  }

  int addLocal(String name, Type type) {
    if (locals.containsKey(name)) {
      throw new RuntimeException("Duplicate variable '" + name + "'");
    }
    locals.put(name, new LocalDeclaration(name, type, locals.size()));
    return locals.size() - 1;
  }

  Object resolve(String name) {
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

    throw new RuntimeException("Unknown type: '" + s + "'");
  }

  public void declare(String name, Type clazz) {
    types.put(name, clazz);
  }

  static class LocalDeclaration {
    String name;
    Type type;
    int localIndex;
    LocalDeclaration(String name, Type type, int localIndex) {
      this.name = name;
      this.type = type;
      this.localIndex = localIndex;
    }
  }

}
