# expression-parser

This program can evaluate mathematical expressions written in infix or postfix using recursive decent parsing. 
The supported operations are listed below.

## Valid Operations
```
Unary Ops:
    '~'       bitwise inverse
    '!'       unary negation
Binary Ops:
    '+'       add
    '-'       subtract
    '*'       multiply
    '/'       divide
    '%'       modulus
    '&'       bitwise and
    '|'       bitwise or
    '^'       bitwise xor
Numbers:
    [0-9]+    32-bit decimal integer value
    '_'       previous result
Infix Only:
    '('       left parenthesis
    ')'       right parenthesis
```
