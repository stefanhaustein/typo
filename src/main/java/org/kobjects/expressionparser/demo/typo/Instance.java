package org.kobjects.expressionparser.demo.typo;

class Instance implements Typed {
  Classifier classifier;
  Object[] fields;

  Instance(Classifier classifier) {
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
