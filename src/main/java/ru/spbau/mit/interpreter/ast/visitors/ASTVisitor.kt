package ru.spbau.mit.interpreter.ast.visitors

import ru.spbau.mit.interpreter.ast.nodes.*
import ru.spbau.mit.interpreter.ast.nodes.Number

interface ASTVisitorVisitable {
    fun <T> accept(visitor: ASTVisitor<T>): T
}

interface ASTVisitor<out T> {
    fun visit(astNode: ASTNode): T = astNode.accept(this)

    fun visitFile(file: File): T

    fun visitBlock(block: Block): T

    fun visitStatement(statement: Statement): T = visit(statement)

    fun visitFunctionDefinition(functionDefinition: FunctionDefinition): T

    fun visitParenthesizedBlock(parenthesizedBlock: ParenthesizedBlock): T

    fun visitVariableDefinition(variableDefinition: VariableDefinition): T

    fun visitWhileCycle(whileCycle: WhileCycle): T

    fun visitIfClause(ifClause: IfClause): T

    fun visitVariableAssignment(variableAssignment: VariableAssignment): T

    fun visitPrintlnCall(printlnCall: PrintlnCall): T

    fun visitExpression(expression: Expression): T = visit(expression)

    fun visitReturnStatement(returnStatement: ReturnStatement): T

    fun visitFunctionCall(functionCall: FunctionCall): T

    fun visitIdentifier(identifier: Identifier): T

    fun visitNumber(number: Number): T

    fun visitParenthesizedExpression(
            parenthesizedExpression: ParenthesizedExpression
    ): T

    fun visitBinaryExpression(binaryExpression: BinaryExpression): T
}