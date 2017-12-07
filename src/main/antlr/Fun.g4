grammar Fun;

file
    :   block
    ;

block
    :   (  statement
        )*
    ;

parenthesizedBlock
    :   CURLY_LPAREN block CURLY_RPAREN
    ;

CURLY_RPAREN
    :   '}'
    ;

CURLY_LPAREN
    :   '{'
    ;

statement
    :   functionDefinition
    |   variableDefinition
    |   printlnCall
    |   expression
    |   whileCycle
    |   ifClause
    |   variableAssignment
    |   returnStatement
    ;

functionDefinition
    :   FUN_DEF identifier LPAREN parameterNames RPAREN parenthesizedBlock
    ;

FUN_DEF
    :   'fun'
    ;

variableDefinition
    :   VAR_DEF identifier
        ( ASSIGN expression
        )?
    ;

ASSIGN
    :   '='
    ;

VAR_DEF
    :   'var'
    ;

printlnCall
    :   PRINTLN_KW LPAREN parameters RPAREN
    ;

PRINTLN_KW
    :   'println'
    ;

parameterNames
    :   ( identifier COMMA
        )*
        identifier
    |
    ;

COMMA
    :   ','
    ;

whileCycle
    :   WHILE_KW LPAREN expression RPAREN parenthesizedBlock
    ;

WHILE_KW
    :   'while'
    ;

ifClause
    :   IF_KW LPAREN expression RPAREN parenthesizedBlock
        ( ELSE_KW parenthesizedBlock
        )?
    ;

IF_KW
    :   'if'
    ;

ELSE_KW
    :   'else'
    ;

variableAssignment
    :   identifier ASSIGN expression
    ;

returnStatement
    :   RETURN_KW expression
    ;

RETURN_KW
    :   'return'
    ;

expression
    :   binaryExpression
    |   atomicExpression
    ;

atomicExpression
    :   functionCall
    |   identifier
    |   literal
    |   parenthesizedExpression
    ;

parenthesizedExpression
    :   LPAREN expression RPAREN
    ;

functionCall
    :   identifier LPAREN parameters RPAREN
    ;

parameters
    :   ( expression COMMA
        )*
        expression
    |
    ;

identifier
    :   IDENTIFIER
    ;

IDENTIFIER
    :   ( LETTER
        | UNDERSCORE
        )
        ( LETTER
        | DIGIT
        | UNDERSCORE
        )*
    ;

literal
    :   number
    ;

number
    :   Number
    ;

binaryExpression
    :   logicalOrExpression
    ;

multiplicativeExpression
    :   left = atomicExpression
        op = ( MUL | DIV | MOD )
        right = multiplicativeExpression   #mulExpr
    |   atomicExpression                   #atomicExpr
    ;

MOD
    :   '%'
    ;

DIV
    :   '/'
    ;

MUL
    :   '*'
    ;

additiveExpression
    :   left = multiplicativeExpression
        op = ( ADD | SUB )
        right = additiveExpression   #addExpr
    |   multiplicativeExpression     #addAtomicExpr
    ;

SUB
    :   '-'
    ;

ADD
    :   '+'
    ;

inequalityExpression
    :   left = additiveExpression
        op = ( LT | LE | GT | GE )
        right = additiveExpression   #ineqExpr
    |   additiveExpression           #ineqAtomicExpr
    ;

GE
    :   '>='
    ;

GT
    :   '>'
    ;

LE
    :   '<='
    ;

LT
    :   '<'
    ;

equalityExpression
    :   left = inequalityExpression
        op = ( EQ | NEQ )
        right = inequalityExpression   #eqExpr
    |   inequalityExpression           #eqAtomicExpr
    ;

NEQ
    :   '!='
    ;

EQ
    :   '=='
    ;

logicalAndExpression
    :   left = equalityExpression
        op = LAND
        right = logicalAndExpression #landExpr
    |   equalityExpression           #landAtomicExpr
    ;

LAND
    :   '&&'
    ;

logicalOrExpression
    :   left = logicalAndExpression
        op = LOR
        right = logicalOrExpression  #lorExpr
    |   logicalAndExpression         #lorAtomicExpr
    ;

LOR
    :   '||'
    ;

fragment UNDERSCORE
    :   '_'
    ;

fragment DIGIT
    :   '0'..'9'
    ;

fragment NON_ZERO_DIGIT
    :   '1'..'9'
    ;

fragment LETTER
    :   ('a'..'z')
    |   ('A'..'Z')
    ;

RPAREN
    :   ')'
    ;

LPAREN
    :   '('
    ;

Number
    :   NON_ZERO_DIGIT
        DIGIT*
    |   '0'
    ;

WS
    :   (' ' | '\t' | '\r'| '\n') -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;