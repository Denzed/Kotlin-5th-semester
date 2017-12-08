package ru.spbau.mit.ast

import org.antlr.v4.runtime.Token

open class ParseException(
        position: Pair<Int, Int>,
        message: String
) : Exception("Could not parse: $message at $position")

class UnknownOperatorException(
        operator: Token
) : ParseException(Pair( operator.line, operator.charPositionInLine),
        "unknown operator \"${operator.text}\"" )