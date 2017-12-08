package ru.spbau.mit.interpreter

import ru.spbau.mit.ast.ASTVisitor
import ru.spbau.mit.ast.nodes.*
import ru.spbau.mit.ast.nodes.Number

open class InterpretingASTVisitor : ASTVisitor<Int?> {
    private val stack: MutableList<StackFrame> = mutableListOf()

    private class StackFrame(
            val variables: MutableMap<String,Int> = mutableMapOf(),
            val functions: MutableMap<String, FunctionDefinition> = mutableMapOf()
    )

    private fun stackDepth(): Int = stack.size - 1

    private fun addStackFrame(stackFrame: StackFrame = StackFrame()) =
            stack.add(stackFrame)

    private fun removeStackFrame() = stack.removeAt(stack.lastIndex)

    private fun findStackFrameForVariable(variable: String): StackFrame? =
            stack.lastOrNull { frame -> variable in frame.variables }

    private fun findFunction(function: String): FunctionDefinition? {
        val stackFrame = stack.lastOrNull { frame -> function in frame.functions }
        return stackFrame?.functions?.get(function)
    }

    open protected suspend fun interpretStatement(statement: Statement): Int? =
            statement.accept(this)

    private suspend fun interpretCondition(expression: Expression): Boolean =
            interpretExpression(expression) != 0

    private suspend fun interpretExpression(expression: Expression): Int =
            expression.accept(this)!!

    override suspend fun visitFile(file: File): Int? {
        addStackFrame()
        file.block.accept(this)
        removeStackFrame()
        return null
    }

    override suspend fun visitBlock(block: Block): Int? {
        for (statement in block.statements) {
            interpretStatement(statement)
                    ?.takeUnless { statement is Expression }
                    ?.let { return it }
        }
        return null
    }

    override suspend fun visitFunctionDefinition(functionDefinition: FunctionDefinition): Int? {
        if (functionDefinition.name in stack.last().functions) {
            throw FunctionRedefinitionException(
                    functionDefinition.position,
                    functionDefinition.name
            )
        }
        stack.last().functions[functionDefinition.name] = functionDefinition
        return null
    }

    override suspend fun visitParenthesizedBlock(parenthesizedBlock: ParenthesizedBlock): Int? {
        addStackFrame()
        val result = parenthesizedBlock.underlyingBlock.accept(this)
        removeStackFrame()
        return result
    }

    override suspend fun visitVariableDefinition(variableDefinition: VariableDefinition): Int? {
        if (variableDefinition.name in stack.last().variables) {
            throw VariableRedefinitionException(
                    variableDefinition.position,
                    variableDefinition.name
            )
        }
        stack.last().variables[variableDefinition.name] =
                interpretExpression(variableDefinition.value)
        return null
    }

    override suspend fun visitWhileCycle(whileCycle: WhileCycle): Int? {
        if (interpretCondition(whileCycle.condition)) {
            whileCycle.body.accept(this)?.let { return it }
            return interpretStatement(whileCycle)
        }
        return null
    }

    override suspend fun visitIfClause(ifClause: IfClause): Int? =
            if (interpretCondition(ifClause.condition)) {
                ifClause.thenBody.accept(this)
            } else {
                ifClause.elseBody?.accept(this)
            }

    override suspend fun visitVariableAssignment(variableAssignment: VariableAssignment): Int? {
        val stackFrame = findStackFrameForVariable(variableAssignment.name)
                ?: throw VariableUndefinedException(
                variableAssignment.position,
                variableAssignment.name
        )
        stackFrame.variables[variableAssignment.name] =
                interpretExpression(variableAssignment.newValue)
        return null
    }

    override suspend fun visitPrintlnCall(printlnCall: PrintlnCall): Int? {
        println(printlnCall.parameters
                .map { interpretExpression(it) }
                .joinToString(" ")
        )
        return null
    }

    override suspend fun visitReturnStatement(returnStatement: ReturnStatement): Int {
        if (stackDepth() == 0) {
            throw InvalidReturnStatementException(returnStatement.position)
        }
        return interpretExpression(returnStatement.expression)
    }

    override suspend fun visitFunctionCall(functionCall: FunctionCall): Int {
        val function = findFunction(functionCall.name)
                ?: throw FunctionUndefinedException(
                functionCall.position,
                functionCall.name
        )
        addStackFrame(StackFrame(
                function.parameterNames
                        .zip(
                                functionCall
                                        .parameters
                                        .map { this.interpretExpression(it) }
                        )
                        .toMap()
                        .toMutableMap()
        )
        )
        val callResult = function.body.accept(this)
        removeStackFrame()
        return callResult ?: 0
    }

    override suspend fun visitIdentifier(identifier: Identifier): Int =
            findStackFrameForVariable(identifier.text)
                    ?.variables
                    ?.get(identifier.text)
                    ?: throw VariableUndefinedException(
                    identifier.position,
                    identifier.text
            )

    override suspend fun visitNumber(number: Number): Int = number.value

    override suspend fun visitParenthesizedExpression(
            parenthesizedExpression: ParenthesizedExpression
    ): Int = interpretExpression(parenthesizedExpression.underlyingExpression)

    override suspend fun visitBinaryExpression(binaryExpression: BinaryExpression): Int =
            binaryExpression.operator(
                    interpretExpression(binaryExpression.left),
                    interpretExpression(binaryExpression.right)
            )
}