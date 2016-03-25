
A simple [single-file](src/main/java/org/kobjects/expressionparser/ExpressionParser.java) configurable Java parser for mathematical expressions.

# Examples and demos

## Immediate evaluation

[Calculator.java](src/main/java/org/kobjects/expressionparser/demo/calculator/Calculator.java) in the demo package contains a simple self-contained use case directly interpreting the input.

The parser configuration supports simple mathematical expressions, and the processor just evaluates them immediately, without constructing an intermediate tree representation.

```
Expression? 5+2*-2^3^2
Result:     -1019.0
```

[SetDemo.java](src/main/java/org/kobjects/expressionparser/demo/sets/SetDemo.java) is similar to the calculator demo,
but illustrates the flexibility of the expression parser with a slightly more "atypical" expression language.

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

[TreeBuilder.java](src/main/java/org/kobjects/expressionparser/demo/cas/TreeBuilder.java) shows how to builds a tree from the input (using a [node factory](src/main/java/org/kobjects/expressionparser/demo/cas/tree/NodeFactory.java). The corresponding [demo app](src/main/java/org/kobjects/expressionparser/demo/cas/) is able to do simplifications and to compute the symbolic derivative. An extended tokenizer translates superscript digits.

```
Input?  derive(1/x, x)

Parsed: derive(1/x, x)

              ⎛1   ⎞
Equals: derive⎜─, x⎟
              ⎝x   ⎠

        (-derive(x, x))     ⎪                
Equals: ───────────────     ⎪ Reciprocal rule
              x²            ⎪                

        (-1)
Equals: ────
         x² 

        -1
Equals: ──
        x²

Flat:   -1/x²

```

## Integration with a "main" parser

The BASIC demo [parser](src/main/java/org/kobjects/expressionparser/demo/interpreter/Parser.java) is able to parse 70's BASIC programs. The rest of the [interpreter demo directory](src/main/java/org/kobjects/expressionparser/demo/interpreter/) contains some code to run them.

```
  **** EXPRESSION PARSER BASIC DEMO V1 ****

  251392K SYSTEM  252056464 BASIC BYTES FREE

READY.
print "Hello World"
Hello World

READY.
10 print "Hello World"
list

10 PRINT "Hello World"

READY.
run
Hello World

load "http://www.vintage-basic.net/bcg/superstartrek.bas"
run
                                    ,------*------,
                    ,-------------   '---  ------'
                     '-------- --'      / /
                         ,---' '-------/ /--,
                          '----------------'
                    THE USS ENTERPRISE --- NCC-1701
YOUR ORDERS ARE AS FOLLOWS:
     DESTROY THE 14 KLINGON WARSHIPS WHICH HAVE INVADED
   THE GALAXY BEFORE THEY CAN ATTACK FEDERATION HEADQUARTERS
   ON STARDATE 2328  THIS GIVES YOU 28 DAYS.  THERE ARE 
  4 STARBASES IN THE GALAXY FOR RESUPPLYING YOUR SHIP
YOUR MISSION BEGINS WITH YOUR STARSHIP LOCATED
IN THE GALACTIC QUADRANT, 'ALTAIR I'.
COMBAT AREA      CONDITION RED
   SHIELDS DANGEROUSLY LOW
---------------------------------
                                        STARDATE          2300
          *                             CONDITION          *RED*
         +K+                            QUADRANT          6,1
                                        SECTOR            8,2
                                        PHOTON TORPEDOES  10
                          *             TOTAL ENERGY      3000
                                        SHIELDS           0
     <*>                                KLINGONS REMAINING14
---------------------------------
COMMAND?
```

