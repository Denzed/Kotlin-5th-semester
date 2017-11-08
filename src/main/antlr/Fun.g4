grammar Fun;

file
    :   block
    ;

block
    :   (  statement
        )*
    ;

bracedBlock
    :   CURLY_LPAREN block CURLY_RPAREN
    ;

CURLY_RPAREN
    : '}'
    ;

CURLY_LPAREN
    : '{'
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
    :   FUN_DEF identifier LPAREN parameterNames RPAREN bracedBlock
    ;

FUN_DEF
    : 'fun'
    ;

variableDefinition
    :   VAR_DEF identifier
        ( ASSIGN expression
        )?
    ;

ASSIGN
    : '='
    ;

VAR_DEF
    : 'var'
    ;

printlnCall
    :   PRINTLN_KW LPAREN parameters RPAREN
    ;

PRINTLN_KW
    : 'println'
    ;

parameterNames
    :   ( identifier COMMA
        )*
        identifier
    |
    ;

COMMA
    : ','
    ;

whileCycle
    :   WHILE_KW LPAREN expression RPAREN bracedBlock
    ;

WHILE_KW
    : 'while'
    ;

ifClause
    :   IF_KW LPAREN expression RPAREN bracedBlock
        ( ELSE_KW bracedBlock
        )?
    ;

IF_KW
    : 'if'
    ;

ELSE_KW
    : 'else'
    ;

variableAssignment
    :   identifier ASSIGN expression
    ;

returnStatement
    :   RETURN_KW expression
    ;

RETURN_KW
    : 'return'
    ;

expression
    :   binaryExpression
    |   atomicExpression
    ;

atomicExpression
    :   functionCall
    |   identifier
    |   literal
    |   bracedExpression
    ;

bracedExpression
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
    :   binaryExpressionOfPrecedence14
    ;

binaryExpressionOfPrecedence5
    :   left = atomicExpression
        op = ( MUL | DIV | MOD )
        right = binaryExpressionOfPrecedence5   #op5Expr
    |   atomicExpression                        #atom5Expr
    ;

MOD
    : '%'
    ;

DIV
    : '/'
    ;

MUL
    : '*'
    ;

binaryExpressionOfPrecedence6
    :   left = binaryExpressionOfPrecedence5
        op = ( ADD | SUB )
        right = binaryExpressionOfPrecedence6   #op6Expr
    |   binaryExpressionOfPrecedence5           #atom6Expr
    ;

SUB
    : '-'
    ;

ADD
    : '+'
    ;

binaryExpressionOfPrecedence8
    :   left = binaryExpressionOfPrecedence6
        op = ( LT | LE | GT | GE )
        right = binaryExpressionOfPrecedence6   #op8Expr
    |   binaryExpressionOfPrecedence6           #atom8Expr
    ;

GE
    : '>='
    ;

GT
    : '>'
    ;

LE
    : '<='
    ;

LT
    : '<'
    ;

binaryExpressionOfPrecedence9
    :   left = binaryExpressionOfPrecedence8
        op = ( EQ | NEQ )
        right = binaryExpressionOfPrecedence8   #op9Expr
    |   binaryExpressionOfPrecedence8           #atom9Expr
    ;

NEQ
    : '!='
    ;

EQ
    : '=='
    ;

binaryExpressionOfPrecedence13
    :   left = binaryExpressionOfPrecedence9
        op = LAND
        right = binaryExpressionOfPrecedence13  #op13Expr
    |   binaryExpressionOfPrecedence9           #atom13Expr
    ;

LAND
    : '&&'
    ;

binaryExpressionOfPrecedence14
    :   left = binaryExpressionOfPrecedence13
        op = LOR
        right = binaryExpressionOfPrecedence14  #op14Expr
    |   binaryExpressionOfPrecedence13          #atom14Expr
    ;

LOR
    : '||'
    ;

fragment UNDERSCORE
    : '_'
    ;

fragment DIGIT
    : '0'..'9'
    ;

fragment NON_ZERO_DIGIT
    : '1'..'9'
    ;

fragment LETTER
    : ('a'..'z')
    | ('A'..'Z')
    ;

RPAREN
    : ')'
    ;

LPAREN
    : '('
    ;

Number
    : NON_ZERO_DIGIT
            DIGIT*
    | '0'
    ;

WS
    : (' ' | '\t' | '\r'| '\n') -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;