package ru.spbau.mit.interpreter.ast

import org.antlr.v4.runtime.ParserRuleContext
import ru.spbau.mit.interpreter.ast.nodes.*
import ru.spbau.mit.interpreter.ast.nodes.Number
import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

private fun getStartPosition(context: ParserRuleContext): Pair<Int,Int> =
        Pair(context.start.line, context.start.charPositionInLine)

private val Boolean.int get() = if (this) 1 else 0

object ASTBuilder : FunBaseVisitor<ASTNode>() {
    override fun visitFile(context: FunParser.FileContext): File =
            File(getStartPosition(context), visitBlock(context.block()))

    override fun visitParenthesizedBlock(context: FunParser.ParenthesizedBlockContext): ParenthesizedBlock =
            ParenthesizedBlock(getStartPosition(context), visitBlock(context.block()))

    override fun visitBlock(context: FunParser.BlockContext): Block =
            Block(getStartPosition(context),
                    context.statement()
                            .map(this::visitStatement))

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
                visitParenthesizedBlock(context.parenthesizedBlock()).underlyingBlock)
    }

    override fun visitVariableDefinition(
            context: FunParser.VariableDefinitionContext
    ): VariableDefinition {
        val name = context.identifier().text
        val expression = visitExpression(context.expression())
        return VariableDefinition(
                getStartPosition(context),
                name,
                expression)
    }

    override fun visitWhileCycle(context: FunParser.WhileCycleContext): WhileCycle {
        return WhileCycle(
                getStartPosition(context),
                visitExpression(context.expression()),
                visitParenthesizedBlock(context.parenthesizedBlock()))
    }

    override fun visitIfClause(context: FunParser.IfClauseContext): IfClause {
        val condition = visitExpression(context.expression())
        val thenBlock = visitParenthesizedBlock(context.parenthesizedBlock(0))
        val elseBlock = if (context.parenthesizedBlock(1) != null) {
            visitParenthesizedBlock(context.parenthesizedBlock(1))
        } else {
            null
        }
        return IfClause(
                getStartPosition(context),
                condition,
                thenBlock,
                elseBlock)
    }

    override fun visitReturnStatement(
            context: FunParser.ReturnStatementContext
    ): ReturnStatement {
        return ReturnStatement(
                getStartPosition(context),
                visitExpression(context.expression()))
    }

    override fun visitVariableAssignment(
            context: FunParser.VariableAssignmentContext
    ): VariableAssignment {
        val identifier = context.identifier().text
        val expression = visitExpression(context.expression())

        return VariableAssignment(
                getStartPosition(context),
                identifier,
                expression)
    }

    override fun visitPrintlnCall(context: FunParser.PrintlnCallContext): PrintlnCall {
        val arguments = context
                .parameters()
                .expression()
                .map { subContext -> visitExpression(subContext) }

        return PrintlnCall(getStartPosition(context), arguments)
    }

    override fun visitExpression(context: FunParser.ExpressionContext): Expression =
            ExpressionBuilder.visitExpression(context)
}

object ExpressionBuilder : FunBaseVisitor<Expression>() {
    override fun visitParenthesizedExpression(context: FunParser.ParenthesizedExpressionContext): ParenthesizedExpression =
            ParenthesizedExpression(getStartPosition(context), visitExpression(context.expression()))

    override fun visitLiteral(context: FunParser.LiteralContext): Literal =
            Number(getStartPosition(context), context.number().text.toInt())

    override fun visitIdentifier(context: FunParser.IdentifierContext): Identifier =
            Identifier(getStartPosition(context), context.text)

    override fun visitFunctionCall(context: FunParser.FunctionCallContext): FunctionCall {
        val name = context.identifier().text
        val arguments = context
                .parameters()
                .expression()
                .map { subContext -> visitExpression(subContext) }

        return FunctionCall(getStartPosition(context), name, arguments)
    }

    override fun visitLorExpr(context: FunParser.LorExprContext): BinaryExpression {
        val left = visit(context.left)
        val right = visit(context.right)
        val op = context.op
        if (op.type != FunParser.LOR) {
            throw UnknownOperatorException(op)
        }
        return BinaryExpression(
                getStartPosition(context),
                left,
                Pair({ l: Int, r: Int -> (l != 0 || r != 0).int }, "||"),
                right
        )
    }

    override fun visitLandExpr(context: FunParser.LandExprContext): BinaryExpression {
        val left = visit(context.left)
        val right = visit(context.right)
        val op = context.op
        if (op.type != FunParser.LAND) {
            throw UnknownOperatorException(op)
        }
        return BinaryExpression(
                getStartPosition(context),
                left,
                Pair({ l: Int, r: Int -> (l != 0 && r != 0).int }, "&&"),
                right
        )
    }

    override fun visitEqExpr(context: FunParser.EqExprContext): BinaryExpression {
        val left = visit(context.left)
        val right = visit(context.right)
        val op: (Int,Int) -> Int = when (context.op.type) {
            FunParser.EQ -> { l, r -> (l == r).int }
            FunParser.NEQ -> { l, r -> (l != r).int }
            else -> throw UnknownOperatorException(context.op)
        }
        return BinaryExpression(getStartPosition(context), left, Pair(op, context.op.text), right)
    }

    override fun visitIneqExpr(context: FunParser.IneqExprContext): BinaryExpression {
        val left = visit(context.left)
        val right = visit(context.right)
        val op: (Int,Int) -> Int = when (context.op.type) {
            FunParser.LE -> { l, r -> (l <= r).int }
            FunParser.LT -> { l, r -> (l < r).int }
            FunParser.GE -> { l, r -> (l >= r).int }
            FunParser.GT -> { l, r -> (l > r).int }
            else -> throw UnknownOperatorException(context.op)
        }
        return BinaryExpression(getStartPosition(context), left, Pair(op, context.op.text), right)
    }

    override fun visitAddExpr(context: FunParser.AddExprContext): BinaryExpression {
        val left = visit(context.left)
        val right = visit(context.right)
        val op: (Int,Int) -> Int = when (context.op.type) {
            FunParser.ADD -> { l, r -> l + r }
            FunParser.SUB -> { l, r -> l - r }
            else -> throw UnknownOperatorException(context.op)
        }
        return BinaryExpression(getStartPosition(context), left, Pair(op, context.op.text), right)
    }

    override fun visitMulExpr(context: FunParser.MulExprContext): BinaryExpression {
        fun checkDivisor(divisor: Int) {
            if (divisor == 0) {
                throw ZeroDivisionException(
                        Pair(context.op.line, context.op.charPositionInLine)
                )
            }
        }

        val left = visit(context.left)
        val right = visit(context.right)
        val op: (Int,Int) -> Int = when (context.op.type) {
            FunParser.MUL -> { l, r -> l * r }
            FunParser.DIV -> { l, r -> checkDivisor(r); l / r }
            FunParser.MOD -> { l, r -> checkDivisor(r); l % r }
            else -> throw UnknownOperatorException(context.op)
        }
        return BinaryExpression(getStartPosition(context), left, Pair(op, context.op.text), right)
    }
}