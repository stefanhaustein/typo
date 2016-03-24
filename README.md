
A simple [single-file](src/main/java/org/kobjects/expressionparser/ExpressionParser.java) configurable Java parser for mathematical expressions.

# Examples

## Immediate evaluation

[Calculator.java](src/main/java/org/kobjects/expressionparser/demo/calculator/Calculator.java) in the demo package contains a simple self-contained use case directly interpreting the input.

[SetDemo.java](src/main/java/org/kobjects/expressionparser/demo/sets/SetDemo.java) is similar to the calculator demo,
but illustrates the parser flexibility with a slightly more "atypical" expression language.

Example output from [SetDemo.java]:

```
Operators: ∩ ∪ ∖
Expression? | {A, B, B, C}|
Result:     3
Expression? {1, 2, 3} ∪ {3, 4, 5} 
Result:     {1.0, 2.0, 3.0, 4.0, 5.0}
Expression? {1, 2} ∩ {2, 3} 
Result:     {2.0}
Expression? | {A, B, C} \ {A, X, Y} |
Result:     2
```

## Tree building

[CasDemo.java](src/main/java/org/kobjects/expressionparser/demo/cas/CasDemo.java) builds a tree from the input and is able to do interpreter simplifications. It's also able to compute the symbolic derivative.
