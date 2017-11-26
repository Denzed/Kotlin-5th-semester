package ru.spbau.mit.interpreter.ast.nodes

import ru.spbau.mit.interpreter.ast.ASTVisitor

abstract class Statement: ASTNode()

class FunctionDefinition(
        override val position: Pair<Int,Int>,
        val name: String,
        val parameterNames: List<String>,
        val body: Block
) : Statement() {
    override fun accept(visitor: ASTVisitor): Int? {
        visitor.visitFunctionDefinition(this)
        return null
    }

    override fun toString(): String {
        val parameterString = parameterNames.joinToString()
        val parenthesizedBody = ParenthesizedBlock(Pair(0, 0), body)
        return "fun $name($parameterString) $parenthesizedBody"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FunctionDefinition

        if (name != other.name) return false
        if (parameterNames != other.parameterNames) return false
        if (body != other.body) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + parameterNames.hashCode()
        result = 31 * result + body.hashCode()
        return result
    }
}

class ParenthesizedBlock(
        override val position: Pair<Int,Int>,
        val underlyingBlock: Block
) : Statement() {
    override fun accept(visitor: ASTVisitor): Int? =
            visitor.visitParenthesizedBlock(this)

    override fun toString(): String {
        val indentedBody =  "\n$underlyingBlock"
                .replace("\n", "\n\t")
        return "{$indentedBody\n}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParenthesizedBlock

        if (underlyingBlock != other.underlyingBlock) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + underlyingBlock.hashCode()
        return result
    }
}

class VariableDefinition(
        override val position: Pair<Int,Int>,
        val name: String,
        val value: Expression
) : Statement() {
    override fun accept(visitor: ASTVisitor): Int? {
        visitor.visitVariableDefinition(this)
        return null
    }

    override fun toString(): String = "val $name = $value"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VariableDefinition

        if (name != other.name) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

class PrintlnCall(
        override val position: Pair<Int,Int>,
        val parameters: List<Expression>
) : Statement() {
    override fun accept(visitor: ASTVisitor): Int? {
        visitor.visitPrintlnCall(this)
        return null
    }

    override fun toString(): String = "println(${parameters.joinToString()})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrintlnCall

        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }
}

