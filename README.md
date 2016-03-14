
A simple single-file configurable Java parser for mathematical expressions.

Calculator.java in the demo package contains a simple use case directly interpreting the input; Derive.java a more complex one building a tree. 

Example output from the [SetDemo.java](src/main/java/net/tidej/expressionparser/demo/sets/SetDemo.java):

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
