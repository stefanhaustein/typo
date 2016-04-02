package org.kobjects.expressionparser.demo.thinscript.runtime;

import org.kobjects.expressionparser.demo.thinscript.statement.Interface;
import org.kobjects.expressionparser.demo.thinscript.type.Typed;

import java.util.LinkedHashMap;

public class StaticMap implements Typed {

  Interface type;
  LinkedHashMap<String, Object> properties = new LinkedHashMap<>();

  public StaticMap(Interface type) {
    this.type = type;
  }

  public Object get(String name) {
    return properties.get(name);
  }

  public void set(String name, Object value) {
    properties.put(name, value);
  }

  @Override
  public Interface type() {
    return type;
  }
}
