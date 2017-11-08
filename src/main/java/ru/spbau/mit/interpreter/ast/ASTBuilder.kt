package ru.spbau.mit.interpreter.ast

import org.antlr.v4.runtime.ParserRuleContext
import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser

private fun getStartPosition(context: ParserRuleContext): Pair<Int,Int> {
    return Pair(context.start.line, context.start.charPositionInLine)
}

private val Boolean.int get() = if (this) 1 else 0

class ASTBuilder : FunBaseVisitor<ASTNode>() {
    private val expressionBuilder = ExpressionBuilder()

    override fun visitFile(context: FunParser.FileContext): File =
            File(getStartPosition(context), visitBlock(context.block()))

    override fun visitBracedBlock(context: FunParser.BracedBlockContext): BracedBlock =
            BracedBlock(getStartPosition(context), visitBlock(context.block()))

    override fun visitBlock(context: FunParser.BlockContext): Block =
            Block(getStartPosition(context),
                    context.statement()
                            .map(this::visitStatement))

    override fun visitStatement(context: FunParser.StatementContext): Statement {
        val result = visitChildren(context)
        return when (result) {
            is Statement -> result
            else -> throw UnknownStatementException(getStartPosition(context))
        }
    }

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
                visitBracedBlock(context.bracedBlock()).underlyingBlock)
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
                visitBracedBlock(context.bracedBlock()))
    }

    override fun visitIfClause(context: FunParser.IfClauseContext): IfClause {
        val condition = visitExpression(context.expression())
        val thenBlock = visitBracedBlock(context.bracedBlock(0))
        val elseBlock = if (context.bracedBlock(1) != null) {
            visitBracedBlock(context.bracedBlock(1))
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

    override fun visitExpression(context: FunParser.ExpressionContext): Expression {
        return expressionBuilder.visitExpression(context)
    }
}

internal class ExpressionBuilder : FunBaseVisitor<Expression>() {
    override fun visitExpression(context: FunParser.ExpressionContext): Expression {
        return visitChildren(context)
    }

    override fun visitBracedExpression(context: FunParser.BracedExpressionContext): BracedExpression {
        return BracedExpression(getStartPosition(context), visitExpression(context.expression()))
    }

    override fun visitLiteral(context: FunParser.LiteralContext): Literal {
        return Number(getStartPosition(context), context.number().text.toInt())
    }

    override fun visitIdentifier(context: FunParser.IdentifierContext): Identifier {
        return Identifier(getStartPosition(context), context.text)
    }

    override fun visitFunctionCall(context: FunParser.FunctionCallContext): FunctionCall {
        val name = context.identifier().text
        val arguments = context
                .parameters()
                .expression()
                .map { subContext -> visitExpression(subContext) }

        return FunctionCall(getStartPosition(context), name, arguments)
    }

    override fun visitOp14Expr(context: FunParser.Op14ExprContext): BinaryExpression {
        val left = visit(context.left)
        val right = visit(context.right)
        val op = context.op
        return when (op.type) {
            FunParser.LOR -> BinaryExpression(
                    getStartPosition(context),
                    left,
                    Pair({ l: Int, r: Int -> (l != 0 || r != 0).int }, "||"),
                    right)
            else -> throw UnknownOperatorException(op)
        }
    }

    override fun visitOp13Expr(context: FunParser.Op13ExprContext): BinaryExpression {
        val left = visit(context.left)
        val right = visit(context.right)
        val op = context.op
        return when (op.type) {
            FunParser.LAND -> BinaryExpression(
                    getStartPosition(context),
                    left,
                    Pair({ l: Int, r: Int -> (l != 0 && r != 0).int }, "&&"),
                    right)
            else -> throw UnknownOperatorException(op)
        }
    }

    override fun visitOp9Expr(context: FunParser.Op9ExprContext): BinaryExpression {
        val left = visit(context.left)
        val right = visit(context.right)
        val op: (Int,Int) -> Int = when (context.op.type) {
            FunParser.EQ -> { l, r -> (l == r).int }
            FunParser.NEQ -> { l, r -> (l != r).int }
            else -> throw UnknownOperatorException(context.op)
        }
        return BinaryExpression(getStartPosition(context), left, Pair(op, context.op.text), right)
    }

    override fun visitOp8Expr(context: FunParser.Op8ExprContext): BinaryExpression {
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

    override fun visitOp6Expr(context: FunParser.Op6ExprContext): BinaryExpression {
        val left = visit(context.left)
        val right = visit(context.right)
        val op: (Int,Int) -> Int = when (context.op.type) {
            FunParser.ADD -> { l, r -> l + r }
            FunParser.SUB -> { l, r -> l - r }
            else -> throw UnknownOperatorException(context.op)
        }
        return BinaryExpression(getStartPosition(context), left, Pair(op, context.op.text), right)
    }

    override fun visitOp5Expr(context: FunParser.Op5ExprContext): BinaryExpression {
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