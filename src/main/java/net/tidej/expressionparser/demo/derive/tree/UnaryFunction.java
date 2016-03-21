package net.tidej.expressionparser.demo.derive.tree;

import net.tidej.expressionparser.demo.derive.string2d.String2d;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.tidej.expressionparser.demo.derive.tree.NodeFactory.c;
import static net.tidej.expressionparser.demo.derive.tree.NodeFactory.f;
import static net.tidej.expressionparser.demo.derive.tree.NodeFactory.neg;
import static net.tidej.expressionparser.demo.derive.tree.NodeFactory.rez;
import static net.tidej.expressionparser.demo.derive.tree.NodeFactory.var;

public class UnaryFunction extends Node {
  public static final Map<String,FunctionDefinition> DEFINITIONS = new HashMap<>();

  static {
    def("ln", rez(var("x")),
        var("e"), c(1),
        c(1), c(0)
    );
    def("sin", f("cos", var("x")),
        c(0), c(0)
    );
    def("cos", neg(f("sin", var("x"))),
        c(0), c(1)
    );
  }

  static void def(String name, Node derivative, Node... sustitutions) {
    DEFINITIONS.put(name, new FunctionDefinition(name, derivative, sustitutions));
  }

  final String name;
  final Node param;
  final FunctionDefinition definition;

  UnaryFunction(String name, Node param) {
    this.name = name;
    this.param = param;
    this.definition = DEFINITIONS.get(name);
  }

  @Override
  public Node simplify(Set<String> explanations) {
    Node simplified = param.simplify(explanations);
    if (simplified.equals(param)) {
      for (int i = 0; i < definition.substitutions.length; i += 2) {
        if (param.equals(definition.substitutions[i])) {
          return definition.substitutions[i + 1];
        }
      }
    }
    return new UnaryFunction(name, simplified);
  }

  @Override
  public Node substitute(String variable, Node replacement) {
    return f(name, param.substitute(variable, replacement));
  }

  @Override
  public String2d toString2d(Stringify type) {
    return String2d.concat(name, String2d.embrace('(', param.toString2d(type), ')'));
  }

  @Override
  public int getPrecedence() {
    // As we always add parens when serializing.
    return PRECEDENCE_PRIMARY;
  }

  static class FunctionDefinition {
    final String name;
    final Node derivative;
    final Node[] substitutions;
    FunctionDefinition(String name, Node derivative, Node... substitutions) {
      this.name = name;
      this.derivative = derivative;
      this.substitutions = substitutions;
    }
  }
}
