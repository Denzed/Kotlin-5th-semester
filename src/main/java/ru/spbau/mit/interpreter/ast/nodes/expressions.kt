package ru.spbau.mit.interpreter.ast.nodes

import ru.spbau.mit.interpreter.ast.ASTVisitor
import ru.spbau.mit.parser.FunParser

abstract class AtomicExpression: Expression()

class FunctionCall(
        override val position: Pair<Int,Int>,
        val name: String,
        val parameters: List<Expression>
) : AtomicExpression() {
    override fun accept( visitor: ASTVisitor ): Int =
            visitor.visitFunctionCall( this )

    override fun toString(): String = "$name(${parameters.joinToString()})"

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as FunctionCall

        if ( name != other.name ) return false
        if ( parameters != other.parameters ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }
}

class Identifier(
        override val position: Pair<Int,Int>,
        val text: String
) : AtomicExpression() {
    override fun accept( visitor: ASTVisitor ): Int = visitor.visitIdentifier( this )

    override fun toString(): String = text

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as Identifier

        if ( text != other.text ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}

abstract class Literal : AtomicExpression()

class Number(
        override val position: Pair<Int,Int>,
        val value: Int
) : Literal() {
    override fun accept( visitor: ASTVisitor ): Int =
            visitor.visitNumber( this )

    override fun toString(): String = value.toString()

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as Number

        if ( value != other.value ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + value
        return result
    }
}

class ParenthesizedExpression(
        override val position: Pair<Int,Int>,
        val underlyingExpression: Expression
) : AtomicExpression() {
    override fun accept( visitor: ASTVisitor ): Int =
            visitor.visitParenthesizedExpression( this )

    override fun toString(): String = "($underlyingExpression)"

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as ParenthesizedExpression

        if ( underlyingExpression != other.underlyingExpression ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + underlyingExpression.hashCode()
        return result
    }
}

class BinaryExpression(
        override val position: Pair<Int,Int>,
        val left: Expression,
        val operator: Pair<(Int, Int) -> Int, String>,
        val right: Expression
) : Expression() {
    override fun accept( visitor: ASTVisitor ): Int =
            visitor.visitBinaryExpression( this )

    override fun toString(): String = "$left ${operator.second} $right"

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as BinaryExpression

        if ( left != other.left ) return false
        if ( operator.second != other.operator.second ) return false
        if ( right != other.right ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + left.hashCode()
        result = 31 * result + operator.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }

    companion object {
        private val Boolean.int get() = if (this) 1 else 0

        val operators = mapOf<Int,Pair<(Int,Int)->Int,String>>(
                FunParser.ADD   to Pair( Int::plus,                          "+"    ),
                FunParser.SUB   to Pair( Int::minus,                         "-"    ),
                FunParser.MUL   to Pair( Int::times,                         "*"    ),
                FunParser.DIV   to Pair( Int::div,                           "/"    ),
                FunParser.MOD   to Pair( Int::rem,                           "%"    ),
                FunParser.EQ    to Pair( { l, r -> (l == r).int },           "=="   ),
                FunParser.NEQ   to Pair( { l, r -> (l != r).int },           "!="   ),
                FunParser.LE    to Pair( { l, r -> (l <= r).int },           "<="   ),
                FunParser.LT    to Pair( { l, r -> (l < r).int },            "<"    ),
                FunParser.GE    to Pair( { l, r -> (l >= r).int },           ">="   ),
                FunParser.GT    to Pair( { l, r -> (l > r).int },            ">"    ),
                FunParser.LAND  to Pair( { l, r -> (l != 0 && r != 0).int }, "&&"   ),
                FunParser.LOR   to Pair( { l, r -> (l != 0 || r != 0).int }, "||"   )
        )
    }
}