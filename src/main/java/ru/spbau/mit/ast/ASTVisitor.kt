package ru.spbau.mit.ast

import ru.spbau.mit.ast.nodes.*
import ru.spbau.mit.ast.nodes.Number

interface ASTVisitor<out T> {
    // nodes
    suspend fun visitFile(file: File): T

    suspend fun visitBlock(block: Block): T

    // statements
    suspend fun visitFunctionDefinition(functionDefinition: FunctionDefinition): T

    suspend fun visitParenthesizedBlock(parenthesizedBlock: ParenthesizedBlock): T

    suspend fun visitVariableDefinition(variableDefinition: VariableDefinition): T

    suspend fun visitWhileCycle(whileCycle: WhileCycle): T

    suspend fun visitIfClause(ifClause: IfClause): T

    suspend fun visitVariableAssignment(variableAssignment: VariableAssignment): T

    suspend fun visitPrintlnCall(printlnCall: PrintlnCall): T

    // expressions
    suspend fun visitReturnStatement(returnStatement: ReturnStatement): T

    suspend fun visitFunctionCall(functionCall: FunctionCall): T

    suspend fun visitIdentifier(identifier: Identifier): T

    suspend fun visitNumber(number: Number): T

    suspend fun visitParenthesizedExpression(
            parenthesizedExpression: ParenthesizedExpression
    ): T

    suspend fun visitBinaryExpression(binaryExpression: BinaryExpression): T
}