package org.kobjects.expressionparser.demo.thin;

import org.kobjects.expressionparser.demo.thin.ast.Classifier;
import org.kobjects.expressionparser.demo.thin.type.Type;
import org.kobjects.expressionparser.demo.thin.type.Typed;

public class Instance implements Typed {
  Classifier classifier;
  public Object[] fields;

  public Instance(Classifier classifier) {
    this.classifier = classifier;
    fields = new Object[classifier.fieldCount];
  }

  @Override
  public Type type() {
    return classifier;
  }

  public void setField(int fieldIndex, Object value) {
    fields[fieldIndex] = value;
  }
}
