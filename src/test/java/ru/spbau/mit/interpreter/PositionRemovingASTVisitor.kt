package ru.spbau.mit.interpreter

import ru.spbau.mit.interpreter.ast.ASTVisitor
import ru.spbau.mit.interpreter.ast.nodes.*
import ru.spbau.mit.interpreter.ast.nodes.Number

object PositionRemovingASTVisitor : ASTVisitor<ASTNode> {
    val blankPosition = Pair(-1, -1)

    override fun visitFile(file: File): File =
            File(blankPosition, visitBlock(file.block))

    override fun visitBlock(block: Block): Block =
            Block(blankPosition, block.statements.map(this::visitStatement))

    override fun visitStatement(statement: Statement): Statement =
            visit(statement) as Statement

    override fun visitFunctionDefinition(
            functionDefinition: FunctionDefinition
    ): FunctionDefinition = functionDefinition.copy(
            position = blankPosition,
            body = visitBlock(functionDefinition.body)
    )

    override fun visitParenthesizedBlock(
            parenthesizedBlock: ParenthesizedBlock
    ): ParenthesizedBlock = ParenthesizedBlock(
            blankPosition,
            visitBlock(parenthesizedBlock.underlyingBlock)
    )

    override fun visitVariableDefinition(
            variableDefinition: VariableDefinition
    ): ASTNode = variableDefinition.copy(
            position = blankPosition,
            value = visitExpression(variableDefinition.value)
    )

    override fun visitWhileCycle(whileCycle: WhileCycle): WhileCycle =
            WhileCycle(
                    blankPosition,
                    visitExpression(whileCycle.condition),
                    visitParenthesizedBlock(whileCycle.body)
            )

    override fun visitIfClause(ifClause: IfClause): IfClause =
            IfClause(
                    blankPosition,
                    visitExpression(ifClause.condition),
                    visitParenthesizedBlock(ifClause.thenBody),
                    ifClause.elseBody?.let(this::visitParenthesizedBlock)
            )

    override fun visitVariableAssignment(
            variableAssignment: VariableAssignment
    ): VariableAssignment = variableAssignment.copy(
            position = blankPosition,
            newValue = visitExpression(variableAssignment.newValue)
    )

    override fun visitPrintlnCall(printlnCall: PrintlnCall): PrintlnCall =
            PrintlnCall(
                    blankPosition,
                    printlnCall.parameters.map(this::visitExpression)
            )

    override fun visitReturnStatement(returnStatement: ReturnStatement): ReturnStatement =
            ReturnStatement(
                    blankPosition,
                    visitExpression(returnStatement.expression)
            )

    override fun visitExpression(expression: Expression): Expression =
            visit(expression) as Expression

    override fun visitFunctionCall(functionCall: FunctionCall): FunctionCall =
            functionCall.copy(
                    position = blankPosition,
                    parameters = functionCall.parameters.map(this::visitExpression)
            )

    override fun visitIdentifier(identifier: Identifier): Identifier =
            identifier.copy(position = blankPosition)

    override fun visitNumber(number: Number): ASTNode =
            number.copy(position = blankPosition)

    override fun visitParenthesizedExpression(
            parenthesizedExpression: ParenthesizedExpression
    ): ParenthesizedExpression = ParenthesizedExpression(
            blankPosition,
            visitExpression(parenthesizedExpression.underlyingExpression)
    )

    override fun visitBinaryExpression(
            binaryExpression: BinaryExpression
    ): BinaryExpression = binaryExpression.copy(
            position = blankPosition,
            left = visitExpression(binaryExpression.left),
            right = visitExpression(binaryExpression.right)
    )
}