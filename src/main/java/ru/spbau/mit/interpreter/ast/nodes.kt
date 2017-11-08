package ru.spbau.mit.interpreter.ast

abstract class ASTNode {
    abstract val position: Pair<Int,Int>

    abstract fun accept(visitor: ASTVisitor): Int?
}

class File(override val position: Pair<Int,Int>,
           val block: Block
) : ASTNode() {
    override fun accept(visitor: ASTVisitor): Int? {
        visitor.visitFile(this)
        return null
    }

    override fun toString(): String {
        return block.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as File

        if (block != other.block) return false

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
    override fun accept(visitor: ASTVisitor): Int? {
        return visitor.visitBlock(this)
    }

    override fun toString(): String {
        return statements.joinToString("\n")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Block

        if (statements != other.statements) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + statements.hashCode()
        return result
    }
}

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
        val bracedBody = BracedBlock(Pair(0, 0), body)
        return "fun $name($parameterString) $bracedBody"
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

class BracedBlock(
        override val position: Pair<Int,Int>,
        val underlyingBlock: Block
) : Statement() {
    override fun accept(visitor: ASTVisitor): Int? {
        return visitor.visitBracedBlock(this)
    }

    override fun toString(): String {
        val indentedBody =  "\n$underlyingBlock"
                .replace("\n", "\n\t")
        return "{$indentedBody\n}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BracedBlock

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

    override fun toString(): String {
        return "val $name = $value"
    }

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

    override fun toString(): String {
        return "println(${parameters.joinToString()})"
    }

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


abstract class Expression: Statement() {
    abstract override fun accept(visitor: ASTVisitor): Int
}

class WhileCycle(
        override val position: Pair<Int,Int>,
        val condition: Expression,
        val body: BracedBlock
) : Statement() {
    override fun accept(visitor: ASTVisitor): Int? {
        return visitor.visitWhileCycle(this)
    }

    override fun toString(): String {
        return "while ($condition) $body"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WhileCycle

        if (condition != other.condition) return false
        if (body != other.body) return false

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
        val thenBody: BracedBlock,
        val elseBody: BracedBlock?
) : Statement() {
    override fun accept(visitor: ASTVisitor): Int? {
        return visitor.visitIfClause(this)
    }

    override fun toString(): String {
        val elseBodyString = if (elseBody != null) "else $elseBody" else ""
        return "if ($condition) $thenBody $elseBodyString"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfClause

        if (condition != other.condition) return false
        if (thenBody != other.thenBody) return false
        if (elseBody != other.elseBody) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + condition.hashCode()
        result = 31 * result + thenBody.hashCode()
        result = 31 * result + (elseBody?.hashCode() ?: 0)
        return result
    }
}

class VariableAssignment(
        override val position: Pair<Int,Int>,
        val name: String,
        val newValue: Expression
) : Statement() {
    override fun accept(visitor: ASTVisitor): Int? {
        visitor.visitVariableAssignment(this)
        return null
    }

    override fun toString(): String {
        return "$name = $newValue"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VariableAssignment

        if (name != other.name) return false
        if (newValue != other.newValue) return false

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
    override fun accept(visitor: ASTVisitor): Int {
        return visitor.visitReturnStatement(this)
    }

    override fun toString(): String {
        return "return $expression"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReturnStatement

        if (expression != other.expression) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + expression.hashCode()
        return result
    }
}

abstract class AtomicExpression: Expression()

class FunctionCall(
        override val position: Pair<Int,Int>,
        val name: String,
        val parameters: List<Expression>
) : AtomicExpression() {
    override fun accept(visitor: ASTVisitor): Int {
        return visitor.visitFunctionCall(this)
    }

    override fun toString(): String {
        return "$name(${parameters.joinToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FunctionCall

        if (name != other.name) return false
        if (parameters != other.parameters) return false

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
    override fun accept(visitor: ASTVisitor): Int {
        return visitor.visitIdentifier(this)
    }

    override fun toString(): String {
        return text
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Identifier

        if (text != other.text) return false

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
    override fun accept(visitor: ASTVisitor): Int {
        return visitor.visitNumber(this)
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Number

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + value
        return result
    }
}

class BracedExpression(
        override val position: Pair<Int,Int>,
        val underlyingExpression: Expression
) : AtomicExpression() {
    override fun accept(visitor: ASTVisitor): Int {
        return visitor.visitBracedExpression(this)
    }

    override fun toString(): String {
        return "($underlyingExpression)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BracedExpression

        if (underlyingExpression != other.underlyingExpression) return false

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
    override fun accept(visitor: ASTVisitor): Int {
        return visitor.visitBinaryExpression(this)
    }

    override fun toString(): String {
        return "$left ${operator.second} $right"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinaryExpression

        if (left != other.left) return false
        if (operator.second != other.operator.second) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + left.hashCode()
        result = 31 * result + operator.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }
}
