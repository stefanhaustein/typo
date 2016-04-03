package org.kobjects.typo.expression;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.statement.Module;
import org.kobjects.typo.type.Type;

public class ModuleProperty extends Expression {

  Module module;
  ParsingContext.LocalDeclaration local;

  public ModuleProperty(Module module, ParsingContext.LocalDeclaration local) {
    this.module = module;
    this.local = local;
  }

  @Override
  public Expression resolve(ParsingContext context) {
    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return module.evaluationContext.getLocal(local.localIndex);
  }

  @Override
  public Type type() {
    return local.type();
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append(module.name());
    cp.append('.');
    cp.append(local.name());
  }
}
