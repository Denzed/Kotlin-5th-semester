package ru.spbau.mit.interpreter.ast.nodes

import ru.spbau.mit.interpreter.ast.visitors.ASTVisitor

abstract class ASTNode {
    abstract val position: Pair<Int,Int>

    abstract fun <T> accept(visitor: ASTVisitor<T>): T
}

data class File(
        override val position: Pair<Int,Int>,
        val block: Block
) : ASTNode() {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitFile(this)

    override fun toString(): String = block.toString()
}

data class Block(
        override val position: Pair<Int,Int>,
        val statements: List<Statement>
) : ASTNode() {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitBlock(this)

    override fun toString(): String = statements.joinToString("\n")
}

data class WhileCycle(
        override val position: Pair<Int,Int>,
        val condition: Expression,
        val body: ParenthesizedBlock
) : Statement() {
    override fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitWhileCycle(this)

    override fun toString(): String = "while ($condition) $body"
}

data class IfClause(
        override val position: Pair<Int,Int>,
        val condition: Expression,
        val thenBody: ParenthesizedBlock,
        val elseBody: ParenthesizedBlock?
) : Statement() {
    override fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitIfClause(this)

    override fun toString(): String {
        val elseBodyString = if (elseBody != null) "else $elseBody" else ""
        return "if ($condition) $thenBody $elseBodyString"
    }
}

data class VariableAssignment(
        override val position: Pair<Int,Int>,
        val name: String,
        val newValue: Expression
) : Statement() {
    override fun <T> accept(visitor: ASTVisitor<T>): T =
        visitor.visitVariableAssignment(this)

    override fun toString(): String = "$name = $newValue"
}

data class ReturnStatement(
        override val position: Pair<Int,Int>,
        val expression: Expression
) : Statement() {
    override fun <T> accept(visitor: ASTVisitor<T>): T =
            visitor.visitReturnStatement(this)

    override fun toString(): String = "return $expression"
}