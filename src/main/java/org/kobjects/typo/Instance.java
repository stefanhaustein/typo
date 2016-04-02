package org.kobjects.typo;

import org.kobjects.typo.statement.TsClass;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.type.Typed;

public class Instance implements Typed {
  TsClass classifier;
  public Object[] fields;

  public Instance(TsClass classifier) {
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
