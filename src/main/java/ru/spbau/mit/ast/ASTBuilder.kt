package ru.spbau.mit.ast

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import ru.spbau.mit.ast.nodes.*
import ru.spbau.mit.ast.nodes.Number
import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

val blankPosition = Pair(-1, -1)

interface WithBlankPosition : WithPosition {
    override fun getStartPosition(
            context: ParserRuleContext
    ): Pair<Int, Int> = blankPosition
}

interface WithPosition {
    fun getStartPosition(context: ParserRuleContext): Pair<Int, Int> =
            Pair(context.start.line, context.start.charPositionInLine)
}

open class ASTBuilder : FunBaseVisitor<ASTNode>(), WithPosition {
    private val blockBuilder = BlockBuilder()
    private val statementBuilder = StatementBuilder()
    private val expressionBuilder = ExpressionBuilder()

    override fun visitFile(context: FunParser.FileContext): File =
            File(getStartPosition(context), context.block().accept(blockBuilder))

    private inner class BlockBuilder : FunBaseVisitor<Block>() {
        override fun visitBlock(context: FunParser.BlockContext): Block =
                Block(
                        getStartPosition(context),
                        context.statement().map { statement ->
                            statement.accept(statementBuilder)
                        }
                )
    }

    override fun visitExpression(context: FunParser.ExpressionContext): Expression =
            context.accept(expressionBuilder)

    private inner class StatementBuilder : FunBaseVisitor<Statement>() {
        private val parenthesizedBlockBuilder = ParenthesizedBlockBuilder()

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
                    context
                            .parenthesizedBlock()
                            .accept(parenthesizedBlockBuilder)
                            .underlyingBlock
            )
        }

        override fun visitVariableDefinition(
                context: FunParser.VariableDefinitionContext
        ): VariableDefinition {
            val name = context.identifier().text
            val expression = context.expression().accept(expressionBuilder)
            return VariableDefinition(
                    getStartPosition(context),
                    name,
                    expression
            )
        }

        override fun visitWhileCycle(context: FunParser.WhileCycleContext): WhileCycle {
            return WhileCycle(
                    getStartPosition(context),
                    context.expression().accept(expressionBuilder),
                    context.parenthesizedBlock().accept(parenthesizedBlockBuilder)
            )
        }

        override fun visitIfClause(context: FunParser.IfClauseContext): IfClause {
            val condition = context.expression().accept(expressionBuilder)
            val thenBlock = context.parenthesizedBlock(0)
                    .accept(parenthesizedBlockBuilder)
            val elseBlock = context.parenthesizedBlock(1)
                    ?.accept(parenthesizedBlockBuilder)
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
                    context.expression().accept(expressionBuilder)
            )
        }

        override fun visitVariableAssignment(
                context: FunParser.VariableAssignmentContext
        ): VariableAssignment = VariableAssignment(
                getStartPosition(context),
                context.identifier().text,
                context.expression().accept(expressionBuilder)
        )

        override fun visitPrintlnCall(
                context: FunParser.PrintlnCallContext
        ): PrintlnCall {
            val parameters = context
                    .parameters()
                    .expression()
                    .map { expression -> expression.accept(expressionBuilder) }

            return PrintlnCall(getStartPosition(context), parameters)
        }

        override fun visitExpression(context: FunParser.ExpressionContext): Expression =
                context.accept(expressionBuilder)

        private inner class ParenthesizedBlockBuilder : FunBaseVisitor<ParenthesizedBlock>() {
            override fun visitParenthesizedBlock(
                    context: FunParser.ParenthesizedBlockContext
            ): ParenthesizedBlock = ParenthesizedBlock(
                    getStartPosition(context),
                    context.block().accept(blockBuilder)
            )
        }
    }

    private inner class ExpressionBuilder : FunBaseVisitor<Expression>() {
        override fun visitParenthesizedExpression(
                context: FunParser.ParenthesizedExpressionContext
        ): ParenthesizedExpression = ParenthesizedExpression(
                getStartPosition(context),
                context.expression().accept(this)
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
                    .map { expression -> expression.accept(this) }

            return FunctionCall(getStartPosition(context), name, parameters)
        }

        private fun getOperator(op: Token): BinaryExpression.Companion.Operator =
                BinaryExpression.operators[op.type]
                    ?: throw UnknownOperatorException(op)


        override fun visitLorExpr(
                context: FunParser.LorExprContext
        ): BinaryExpression = BinaryExpression(
                getStartPosition(context),
                context.left.accept(this),
                getOperator(context.op),
                context.right.accept(this)
        )

        override fun visitLandExpr(
                context: FunParser.LandExprContext
        ): BinaryExpression = BinaryExpression(
                getStartPosition(context),
                context.left.accept(this),
                getOperator(context.op),
                context.right.accept(this)
        )

        override fun visitEqExpr(
                context: FunParser.EqExprContext
        ): BinaryExpression = BinaryExpression(
                getStartPosition(context),
                context.left.accept(this),
                getOperator(context.op),
                context.right.accept(this)
        )

        override fun visitIneqExpr(
                context: FunParser.IneqExprContext
        ): BinaryExpression = BinaryExpression(
                getStartPosition(context),
                context.left.accept(this),
                getOperator(context.op),
                context.right.accept(this)
        )

        override fun visitAddExpr(
                context: FunParser.AddExprContext
        ): BinaryExpression = BinaryExpression(
                getStartPosition(context),
                context.left.accept(this),
                getOperator(context.op),
                context.right.accept(this)
        )

        override fun visitMulExpr(
                context: FunParser.MulExprContext
        ): BinaryExpression = BinaryExpression(
                getStartPosition(context),
                context.left.accept(this),
                getOperator(context.op),
                context.right.accept(this)
        )
    }
}