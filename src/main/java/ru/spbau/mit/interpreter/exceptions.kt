package ru.spbau.mit.interpreter

open class InterpretException(
        position: Pair<Int, Int>,
        message: String
) : Exception("Could not interpret: $message at $position")

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