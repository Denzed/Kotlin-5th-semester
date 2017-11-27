package ru.spbau.mit.interpreter.ast.nodes

import ru.spbau.mit.interpreter.ast.ASTVisitor

abstract class ASTNode {
    abstract val position: Pair<Int,Int>

    abstract fun accept( visitor: ASTVisitor ): Int?
}

class File(
        override val position: Pair<Int,Int>,
        val block: Block
) : ASTNode() {
    override fun accept( visitor: ASTVisitor ): Int? {
        visitor.visitFile( this )
        return null
    }

    override fun toString(): String = block.toString()

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as File

        if ( block != other.block ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + block.hashCode()
        return result
    }
}

class Block(
        override val position: Pair<Int,Int>,
        val statements: List<Statement>
) : ASTNode() {
    override fun accept( visitor: ASTVisitor ): Int? = visitor.visitBlock( this )

    override fun toString(): String = statements.joinToString( "\n" )

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as Block

        if ( statements != other.statements ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + statements.hashCode()
        return result
    }
}

abstract class Expression: Statement() {
    abstract override fun accept( visitor: ASTVisitor ): Int
}

class WhileCycle(
        override val position: Pair<Int,Int>,
        val condition: Expression,
        val body: ParenthesizedBlock
) : Statement() {
    override fun accept( visitor: ASTVisitor ): Int? =
            visitor.visitWhileCycle( this )

    override fun toString(): String = "while ($condition) $body"

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as WhileCycle

        if ( condition != other.condition ) return false
        if ( body != other.body ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + condition.hashCode()
        result = 31 * result + body.hashCode()
        return result
    }
}

class IfClause(
        override val position: Pair<Int,Int>,
        val condition: Expression,
        val thenBody: ParenthesizedBlock,
        val elseBody: ParenthesizedBlock?
) : Statement() {
    override fun accept( visitor: ASTVisitor ): Int? =
            visitor.visitIfClause( this )

    override fun toString(): String {
        val elseBodyString = if ( elseBody != null ) "else $elseBody" else ""
        return "if ($condition) $thenBody $elseBodyString"
    }

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as IfClause

        if ( condition != other.condition ) return false
        if ( thenBody != other.thenBody ) return false
        if ( elseBody != other.elseBody ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + condition.hashCode()
        result = 31 * result + thenBody.hashCode()
        result = 31 * result + ( elseBody?.hashCode() ?: 0 )
        return result
    }
}

class VariableAssignment(
        override val position: Pair<Int,Int>,
        val name: String,
        val newValue: Expression
) : Statement() {
    override fun accept( visitor: ASTVisitor ): Int? {
        visitor.visitVariableAssignment( this )
        return null
    }

    override fun toString(): String = "$name = $newValue"

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as VariableAssignment

        if ( name != other.name ) return false
        if ( newValue != other.newValue ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + newValue.hashCode()
        return result
    }
}

class ReturnStatement(
        override val position: Pair<Int,Int>,
        val expression: Expression
) : Statement() {
    override fun accept( visitor: ASTVisitor ): Int =
            visitor.visitReturnStatement( this )

    override fun toString(): String = "return $expression"

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true
        if ( javaClass != other?.javaClass ) return false

        other as ReturnStatement

        if ( expression != other.expression ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + expression.hashCode()
        return result
    }
}