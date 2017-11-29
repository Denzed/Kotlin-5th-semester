package ru.spbau.mit.interpreter.ast.nodes

import ru.spbau.mit.interpreter.ast.visitors.ASTVisitor
import ru.spbau.mit.parser.FunParser

abstract class Expression: Statement()

abstract class AtomicExpression: Expression()

data class FunctionCall(
        override val position: Pair<Int,Int>,
        val name: String,
        val parameters: List<Expression>
) : AtomicExpression() {
    override fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitFunctionCall(this)

    override fun toString(): String = "$name(${parameters.joinToString()})"
}

data class Identifier(
        override val position: Pair<Int,Int>,
        val text: String
) : AtomicExpression() {
    override fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitIdentifier(this)

    override fun toString(): String = text
}

abstract class Literal : AtomicExpression()

data class Number(
        override val position: Pair<Int,Int>,
        val value: Int
) : Literal() {
    override fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitNumber(this)

    override fun toString(): String = value.toString()
}

data class ParenthesizedExpression(
        override val position: Pair<Int,Int>,
        val underlyingExpression: Expression
) : AtomicExpression() {
    override fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitParenthesizedExpression(this)

    override fun toString(): String = "($underlyingExpression)"
}

data class BinaryExpression(
        override val position: Pair<Int,Int>,
        val left: Expression,
        val operator: Operator,
        val right: Expression
) : Expression() {
    override fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitBinaryExpression(this)

    override fun toString(): String = "$left ${operator.representation} $right"

    companion object {
        private val Boolean.int get() = if (this) 1 else 0

        enum class Operator(
                val operation: (Int,Int)->Int,
                val representation: String
        ) {
            ADD     (Int::plus,                           "+" ),
            SUB     (Int::minus,                           "-"),
            MUL     (Int::times,                          "*" ),
            DIV     (Int::div,                            "/" ),
            MOD     (Int::rem,                            "%" ),
            EQ      ( { l, r -> (l == r).int },           "=="),
            NEQ     ( { l, r -> (l != r).int },           "!="),
            LE      ( { l, r -> (l <= r).int },           "<="),
            LT      ( { l, r -> (l < r).int },            "<" ),
            GE      ( { l, r -> (l >= r).int },           ">="),
            GT      ( { l, r -> (l > r).int },            ">" ),
            LAND    ( { l, r -> (l != 0 && r != 0).int }, "&&"),
            LOR     ( { l, r -> (l != 0 || r != 0).int }, "||")
        }

        val operators = mapOf(
                FunParser.ADD   to Operator.ADD,
                FunParser.SUB   to Operator.SUB,
                FunParser.MUL   to Operator.MUL,
                FunParser.DIV   to Operator.DIV,
                FunParser.MOD   to Operator.MOD,
                FunParser.EQ    to Operator.EQ,
                FunParser.NEQ   to Operator.NEQ,
                FunParser.LE    to Operator.LE,
                FunParser.LT    to Operator.LT,
                FunParser.GE    to Operator.GE,
                FunParser.GT    to Operator.GT,
                FunParser.LAND  to Operator.LAND,
                FunParser.LOR   to Operator.LOR
        )
    }
}