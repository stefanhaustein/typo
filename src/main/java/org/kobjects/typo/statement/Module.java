package org.kobjects.typo.statement;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.parser.NamedEntity;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.EvaluationContext;

import java.util.Collection;

public class Module extends SimpleStatement implements NamedEntity {

  String name;
  Collection<NamedEntity> namedEntities;
  public ParsingContext parsingContext;
  public EvaluationContext evaluationContext;

  public Module(String name, Statement[] children, Collection<NamedEntity> namedEntities) {
    super(null, children);
    this.name = name;
    this.namedEntities = namedEntities;
  }

  @Override
  public Object eval(EvaluationContext context) {
    evaluationContext = new EvaluationContext(null, parsingContext.locals.size());
    for (int i = 0; i < children.length; i++) {
      children[i].eval(evaluationContext);
    }
    return NO_RESULT;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("module ").append(name).append(" {");
    cp.indent();
    for (int i = 0; i < children.length; i++) {
      cp.newLine();
      children[i].print(cp);
    }
    cp.outdent();
    cp.newLine();
    cp.append("}");
    cp.newLine();
  }

  @Override
  public void resolve(ParsingContext context) {
    for (Statement child: children) {
      child.resolve(parsingContext);
    }
  }

  @Override
  public void resolveSignatures(ParsingContext context) {
    for (NamedEntity entity: namedEntities) {
      entity.resolveSignatures(parsingContext);
    }
  }

  @Override
  public void declareStatics(ParsingContext context) {
    context.declareStatic(name, this);
    parsingContext = new ParsingContext(context, name());
    for(NamedEntity entity: namedEntities) {
      entity.declareStatics(parsingContext);
    }
  }
}
