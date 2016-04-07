package org.kobjects.typo.type;

import org.kobjects.typo.expression.Function;
import org.kobjects.typo.parser.ParsingContext;
import org.kobjects.typo.runtime.EvaluationContext;
import org.kobjects.typo.runtime.NativeFunction;

import java.util.EnumSet;
import java.util.List;

public class ArrayType extends TsClass {

  public Type elementType;
  boolean resolved;

  public ArrayType(Type elementType) {
    super("Array", List.class);
    this.elementType = elementType;
    Member length = new TsClass.Member() {
      public Object get(Object instance) {
        return Double.valueOf(((List) instance).size());
      }
    };
    length.name = "length";
    length.type = Types.NUMBER;
    members.put("length", length);

    NativeFunction reduce = new NativeFunction(this, "reduce", Types.ANY,
        new FunctionType.Parameter("callback", Types.ANY /*
            new FunctionType(elementType,
                new FunctionType.Parameter("previousValue", Types.ANY),
                new FunctionType.Parameter("currentValue", elementType),
                new FunctionType.Parameter("currentIndex", Types.NUMBER),
                new FunctionType.Parameter("array", this)) */ ),
        new FunctionType.Parameter("initialValue", Types.ANY)) {
      @Override
      public Object apply(EvaluationContext context) {
        Function reduce = (Function) context.getLocal(0);
        int parameterCount = reduce.parameters.length;
        Object value = context.getLocal(1);
        List list = (List) context.getLocal(2);
        for (int i = 0; i < list.size(); i++) {
          EvaluationContext reduceContext = reduce.createContext(null);
          reduceContext.setLocal(0, value);
          reduceContext.setLocal(1, list.get(i));
          if (parameterCount > 2) {
            reduceContext.setLocal(2, (double) i);
            if (parameterCount > 3) {
              reduceContext.setLocal(3, list);
            }
          }
          value = reduce.apply(reduceContext);
        }
        return value;
      }
    };
    reduce.type = null;  // Make sure types get resolved.
    addMethod(EnumSet.noneOf(Modifier.class), reduce);
  }

  @Override
  public String name() {
    return elementType.name() + "[]";
  }

  @Override
  public ArrayType resolve(ParsingContext context) {
    if (!resolved) {
      resolved = true;
      elementType = elementType.resolve(context);
      resolveSignatures(context);
      resolveMembers(context);
      super.resolve(context);
    }
    return this;
  }

  @Override
  public boolean assignableFrom(Type type) {
    return false;
  }
}
