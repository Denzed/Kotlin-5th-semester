package ru.spbau.mit.interpreter.ast

import org.antlr.v4.runtime.Token

open class ParseException(
        position: Pair<Int, Int>,
        message: String
) : Exception("Could not parse: $message at $position")

class UnknownStatementException(
        position: Pair<Int, Int>
) : ParseException(position,
        "unknown statement")

class UnknownOperatorException(
        operator: Token
) : ParseException(Pair(operator.line, operator.charPositionInLine),
        "unknown operator \"${operator.text}\"")

open class InterpretException(
        position: Pair<Int, Int>,
        message: String
) : Exception("Could not interpret: $message at $position")

class ZeroDivisionException(
        position: Pair<Int, Int>
) : InterpretException(position, "zero division")

class FunctionRedefinitionException(
        position: Pair<Int, Int>,
        name: String
) : InterpretException(position, "redefined function \"$name\"")

class VariableRedefinitionException(
        position: Pair<Int, Int>,
        name: String
) : InterpretException(position, "redefined variable \"$name\"")

class FunctionUndefinedException(
        position: Pair<Int, Int>,
        name: String
) : InterpretException(position, "undefined function \"$name\"")

class VariableUndefinedException(
        position: Pair<Int, Int>,
        name: String
) : InterpretException(position, "undefined variable \"$name\"")

class InvalidReturnStatementException(
        position: Pair<Int, Int>
) : InterpretException(position, "\"return\" outside of function")