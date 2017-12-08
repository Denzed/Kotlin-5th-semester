package ru.spbau.mit.ast.nodes

import ru.spbau.mit.ast.ASTVisitor

abstract class Statement: ASTNode()

data class FunctionDefinition(
        override val position: Pair<Int,Int>,
        val name: String,
        val parameterNames: List<String>,
        val body: Block
) : Statement() {
    override suspend fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitFunctionDefinition(this)

    override fun toString(): String {
        val parameterString = parameterNames.joinToString()
        val parenthesizedBody = ParenthesizedBlock(Pair(0, 0), body)
        return "fun $name($parameterString) $parenthesizedBody"
    }
}

data class ParenthesizedBlock(
        override val position: Pair<Int,Int>,
        val underlyingBlock: Block
) : Statement() {
    override suspend fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitParenthesizedBlock(this)

    override fun toString(): String {
        val indentedBody =  "\n$underlyingBlock"
                .replace("\n", "\n\t")
        return "{$indentedBody\n}"
    }
}

data class VariableDefinition(
        override val position: Pair<Int,Int>,
        val name: String,
        val value: Expression
) : Statement() {
    override suspend fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitVariableDefinition(this)

    override fun toString(): String = "val $name = $value"
}

data class PrintlnCall(
        override val position: Pair<Int,Int>,
        val parameters: List<Expression>
) : Statement() {
    override suspend fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitPrintlnCall(this)

    override fun toString(): String = "println(${parameters.joinToString()})"
}