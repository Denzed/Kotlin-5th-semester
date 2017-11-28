package ru.spbau.mit.interpreter.ast

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import ru.spbau.mit.interpreter.ast.nodes.*
import ru.spbau.mit.interpreter.ast.nodes.Number
import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

private fun getStartPosition(context: ParserRuleContext): Pair<Int,Int> =
        Pair(context.start.line, context.start.charPositionInLine)

private fun getOperator(op: Token): Pair<(Int, Int)->Int,String> =
        BinaryExpression.operators[op.type] ?: throw UnknownOperatorException(op)

object ASTBuilder : FunBaseVisitor<ASTNode>() {
    override fun visitFile(context: FunParser.FileContext): File =
            File(getStartPosition(context), visitBlock(context.block()))

    override fun visitParenthesizedBlock(
            context: FunParser.ParenthesizedBlockContext
    ): ParenthesizedBlock =
            ParenthesizedBlock(
                    getStartPosition(context),
                    visitBlock(context.block())
            )

    override fun visitBlock(context: FunParser.BlockContext): Block =
            Block(
                    getStartPosition(context),
                    context.statement().map(this::visitStatement)
            )

    override fun visitStatement(context: FunParser.StatementContext): Statement =
            visitChildren(context) as? Statement ?:
                    throw UnknownStatementException(getStartPosition(context))

    override fun visitFunctionDefinition(
            context: FunParser.FunctionDefinitionContext
    ): FunctionDefinition {
        val name = context.identifier().text
        val argumentNames = context
                .parameterNames()
                .identifier()
                .map(FunParser.IdentifierContext::getText)
        return FunctionDefinition(
                getStartPosition(context),
                name,
                argumentNames,
                visitParenthesizedBlock(context.parenthesizedBlock())
                        .underlyingBlock
        )
    }

    override fun visitVariableDefinition(
            context: FunParser.VariableDefinitionContext
    ): VariableDefinition {
        val name = context.identifier().text
        val expression = visitExpression(context.expression())
        return VariableDefinition(
                getStartPosition(context),
                name,
                expression
        )
    }

    override fun visitWhileCycle(context: FunParser.WhileCycleContext): WhileCycle {
        return WhileCycle(
                getStartPosition(context),
                visitExpression(context.expression()),
                visitParenthesizedBlock(context.parenthesizedBlock())
        )
    }

    override fun visitIfClause(context: FunParser.IfClauseContext): IfClause {
        val condition = visitExpression(context.expression())
        val thenBlock = visitParenthesizedBlock(context.parenthesizedBlock(0))
        val elseBlock = context.parenthesizedBlock(1)
                ?.let(this::visitParenthesizedBlock)
        return IfClause(
                getStartPosition(context),
                condition,
                thenBlock,
                elseBlock
        )
    }

    override fun visitReturnStatement(
            context: FunParser.ReturnStatementContext
    ): ReturnStatement {
        return ReturnStatement(
                getStartPosition(context),
                visitExpression(context.expression())
        )
    }

    override fun visitVariableAssignment(
            context: FunParser.VariableAssignmentContext
    ): VariableAssignment = VariableAssignment(
            getStartPosition(context),
            context.identifier().text,
            visitExpression(context.expression())
    )

    override fun visitPrintlnCall(
            context: FunParser.PrintlnCallContext
    ): PrintlnCall {
        val parameters = context
                .parameters()
                .expression()
                .map(this::visitExpression)

        return PrintlnCall(getStartPosition(context), parameters)
    }

    override fun visitExpression(
            context: FunParser.ExpressionContext
    ): Expression = ExpressionBuilder.visitExpression(context)
}

object ExpressionBuilder : FunBaseVisitor<Expression>() {
    override fun visitParenthesizedExpression(
            context: FunParser.ParenthesizedExpressionContext
    ): ParenthesizedExpression = ParenthesizedExpression(
            getStartPosition(context),
            visitExpression(context.expression())
    )

    override fun visitLiteral(context: FunParser.LiteralContext): Literal =
            Number(getStartPosition(context), context.number().text.toInt())

    override fun visitIdentifier(
            context: FunParser.IdentifierContext
    ): Identifier = Identifier(getStartPosition(context), context.text)

    override fun visitFunctionCall(
            context: FunParser.FunctionCallContext
    ): FunctionCall {
        val name = context.identifier().text
        val parameters = context
                .parameters()
                .expression()
                .map(this::visitExpression)

        return FunctionCall(getStartPosition(context), name, parameters)
    }

    override fun visitLorExpr(
            context: FunParser.LorExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            visit(context.left),
            getOperator(context.op),
            visit(context.right)
    )

    override fun visitLandExpr(
            context: FunParser.LandExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            visit(context.left),
            getOperator(context.op),
            visit(context.right)
    )

    override fun visitEqExpr(
            context: FunParser.EqExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            visit(context.left),
            getOperator(context.op),
            visit(context.right)
    )

    override fun visitIneqExpr(
            context: FunParser.IneqExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            visit(context.left),
            getOperator(context.op),
            visit(context.right)
    )

    override fun visitAddExpr(
            context: FunParser.AddExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            visit(context.left),
            getOperator(context.op),
            visit(context.right)
    )

    override fun visitMulExpr(
            context: FunParser.MulExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            visit(context.left),
            getOperator(context.op),
            visit(context.right)
    )
}