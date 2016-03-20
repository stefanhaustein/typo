package net.tidej.expressionparser.demo.derive.tree;

import net.tidej.expressionparser.demo.derive.string2d.String2d;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Function extends Node {
  static final Map<String,FunctionDefinition> DEFINITIONS = new HashMap<>();

  static {
    def("ln", 1);
    def("exp", 1);
    def("derive", 2);
  }

  static void def(String name, int parameterCount) {
    DEFINITIONS.put(name, new FunctionDefinition(name, parameterCount));
  }

  final String name;
  final Node[] param;
  final FunctionDefinition definition;

  Function(String name, Node... param) {
    this.name = name;
    this.param = param;
    this.definition = DEFINITIONS.get(name);
    if (definition != null && definition.parmeterCount != param.length) {
      throw new IllegalArgumentException("Expected " + definition.parmeterCount
          + " parameters, but got " + param.length);
    }
  }

  public Node simplify(Set<String> explanations) {
    Node[] simplified = new Node[param.length];
    boolean changed = false;
    for (int i = 0; i < param.length; i++) {
      simplified[i] = param[i].simplify(explanations);
      changed = changed || !param[i].equals(simplified[i]);
    }
    if (!changed) {
      if (simplified.length == 1) {
        boolean isConst = simplified[0] instanceof Constant;
        double paramVal = isConst ? ((Constant) simplified[0]).value : Double.NaN;
        if (name.equals("ln")) {
          if (isConst) {
            return new Constant(Math.log(paramVal));
          }
        }
        if (name.equals("exp")) {
          if (isConst) {
            return new Constant(Math.exp(paramVal));
          }
        }
      }
      if (name.equals("derive")) {
        return simplified[0].derive(simplified[1].toString(), explanations);
      }
    }
    return new Function(name, simplified);
  }

  @Override
  public Node derive(String to, Set<String> explanations) {
    if (name.equals("ln")) {
      return NodeFactory.mul(NodeFactory.derive(param[0], to), NodeFactory.rez(param[0]));
    }
    if (name.equals("exp")) {
      return NodeFactory.mul(NodeFactory.f("exp", param[0]), NodeFactory.derive(param[0], to));
    }
    throw new RuntimeException("Don't know how to derive '" + name + "'");
  }

  @Override
  public String2d toString2d(Stringify type) {
    String2d.Builder sb = new String2d.Builder();
    for (int i = 0; i < param.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(param[i].toString2d(type));
    }
    String2d content = sb.build();
    return String2d.concat(name + ' ', String2d.embrace('(', content, ')'));
  }

  @Override
  public int getPrecedence() {
    return 10;
  }

  static class FunctionDefinition {
    final String name;
    final int parmeterCount;
    FunctionDefinition(String name, int parmeterCount) {
      this.name = name;
      this.parmeterCount = parmeterCount;
    }
  }
}
