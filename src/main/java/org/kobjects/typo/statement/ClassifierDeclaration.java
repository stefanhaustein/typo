package org.kobjects.typo.statement;

import org.kobjects.typo.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.type.Classifier;

public class ClassifierDeclaration extends Statement {
  Classifier classifier;

  public ClassifierDeclaration(Classifier classifier) {
    this.classifier = classifier;
  }

  @Override
  public Object eval(EvaluationContext context) {
    return NO_RESULT;
  }

  @Override
  public void resolve(ParsingContext context) {
    classifier.resolveMembers(context);
  }

  @Override
  public void print(CodePrinter cp) {
    classifier.print(cp);
  }
}
