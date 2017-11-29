package ru.spbau.mit.interpreter.ast

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import ru.spbau.mit.interpreter.ast.nodes.*
import ru.spbau.mit.interpreter.ast.nodes.Number
import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

interface WithPosition {
    fun getStartPosition(context: ParserRuleContext): Pair<Int, Int> =
            Pair(context.start.line, context.start.charPositionInLine)
}

open class ASTBuilder : FunBaseVisitor<ASTNode>(), WithPosition {
    override fun visitFile(context: FunParser.FileContext): File =
            File(getStartPosition(context), context.block().accept(this) as Block)

    override fun visitParenthesizedBlock(
            context: FunParser.ParenthesizedBlockContext
    ): ParenthesizedBlock =
            ParenthesizedBlock(
                    getStartPosition(context),
                    context.block().accept(this) as Block
            )

    override fun visitBlock(context: FunParser.BlockContext): Block =
            Block(
                    getStartPosition(context),
                    context.statement().map {
                        statement -> statement.accept(this) as Statement
                    }
            )

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
                (context.parenthesizedBlock().accept(this) as ParenthesizedBlock)
                        .underlyingBlock
        )
    }

    override fun visitVariableDefinition(
            context: FunParser.VariableDefinitionContext
    ): VariableDefinition {
        val name = context.identifier().text
        val expression = context.expression().accept(this) as Expression
        return VariableDefinition(
                getStartPosition(context),
                name,
                expression
        )
    }

    override fun visitWhileCycle(context: FunParser.WhileCycleContext): WhileCycle {
        return WhileCycle(
                getStartPosition(context),
                context.expression().accept(this) as Expression,
                context.parenthesizedBlock().accept(this) as ParenthesizedBlock
        )
    }

    override fun visitIfClause(context: FunParser.IfClauseContext): IfClause {
        val condition = context.expression().accept(this) as Expression
        val thenBlock = context.parenthesizedBlock(0)
                .accept(this) as ParenthesizedBlock
        val elseBlock = context.parenthesizedBlock(1)
                ?.accept(this) as? ParenthesizedBlock
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
                context.expression().accept(this) as Expression
        )
    }

    override fun visitVariableAssignment(
            context: FunParser.VariableAssignmentContext
    ): VariableAssignment = VariableAssignment(
            getStartPosition(context),
            context.identifier().text,
            context.expression().accept(this) as Expression
    )

    override fun visitPrintlnCall(
            context: FunParser.PrintlnCallContext
    ): PrintlnCall {
        val parameters = context
                .parameters()
                .expression()
                .map { expression -> expression.accept(this) as Expression }

        return PrintlnCall(getStartPosition(context), parameters)
    }

    override fun visitParenthesizedExpression(
            context: FunParser.ParenthesizedExpressionContext
    ): ParenthesizedExpression = ParenthesizedExpression(
            getStartPosition(context),
            context.expression().accept(this) as Expression
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
                .map { expression -> expression.accept(this) as Expression }

        return FunctionCall(getStartPosition(context), name, parameters)
    }

    private fun getOperator(op: Token): BinaryExpression.Companion.Operator =
            BinaryExpression.operators[op.type]
                    ?: throw UnknownOperatorException(op)

    override fun visitLorExpr(
            context: FunParser.LorExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            context.left.accept(this) as Expression,
            getOperator(context.op),
            context.right.accept(this) as Expression
    )

    override fun visitLandExpr(
            context: FunParser.LandExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            context.left.accept(this) as Expression,
            getOperator(context.op),
            context.right.accept(this) as Expression
    )

    override fun visitEqExpr(
            context: FunParser.EqExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            context.left.accept(this) as Expression,
            getOperator(context.op),
            context.right.accept(this) as Expression
    )

    override fun visitIneqExpr(
            context: FunParser.IneqExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            context.left.accept(this) as Expression,
            getOperator(context.op),
            context.right.accept(this) as Expression
    )

    override fun visitAddExpr(
            context: FunParser.AddExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            context.left.accept(this) as Expression,
            getOperator(context.op),
            context.right.accept(this) as Expression
    )

    override fun visitMulExpr(
            context: FunParser.MulExprContext
    ): BinaryExpression = BinaryExpression(
            getStartPosition(context),
            context.left.accept(this) as Expression,
            getOperator(context.op),
            context.right.accept(this) as Expression
    )
}