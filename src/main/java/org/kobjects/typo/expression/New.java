package org.kobjects.typo.expression;


import org.kobjects.typo.runtime.Instance;
import org.kobjects.typo.type.TsClass;
import org.kobjects.typo.type.Type;
import org.kobjects.typo.io.CodePrinter;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.parser.ParsingContext;

public class New extends ExpressionN {
  TsClass tsClass;  // Filled on resolve only
  Type type;
  public New(Type type, Expression... child) {
    super(child);
    this.type = type;
  }

  @Override
  public void print(CodePrinter cp) {
    cp.append("new ");
    cp.append(type.name());
    cp.append('(');
    if (children.length > 0) {
      children[0].print(cp);
      for (int i = 1; i < children.length; i++) {
        cp.append(", ");
        children[i].print(cp);
      }
    }
    cp.append(')');
  }

  @Override
  public Expression resolve(ParsingContext context) {
    super.resolve(context);
    type = type.resolve(context);
    if (!(type instanceof TsClass)) {
      throw new RuntimeException("'" + type + "' must be a class for new.");
    }
    tsClass = (TsClass) type;

    if (tsClass.constructor == null) {
      if (children.length != 0) {
        throw new RuntimeException("No constructor arguments expected.");
      }
    } else {
      Function constructor = tsClass.constructor;
      constructor.type().assertSignature(childTypes(), CodePrinter.toString(this));
    }

    return this;
  }

  @Override
  public Object eval(EvaluationContext context) {
    Object instance = null;
    try {
      instance = tsClass.instanceClass != null ? tsClass.instanceClass.newInstance() : new Instance(tsClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    if (instance instanceof Instance) {
      for (TsClass.Member member : tsClass.members.values()) {
        if (member.fieldIndex != -1) {
          if (member.initializer != null) {
            // TODO(haustein): Move to ctor instead!
            ((Instance) instance).setField(member.fieldIndex, member.initializer.eval(null));
          }
        }
      }
    }

    if (tsClass.constructor != null) {
      EvaluationContext newContext = tsClass.constructor.createContext(instance);
      for (int i = 0; i < children.length; i++) {
        newContext.setLocal(i, children[i].eval(context));
      }
      tsClass.constructor.apply(newContext);
    }
    return instance;
  }

  @Override
  public TsClass type() {
    return tsClass;
  }
}
