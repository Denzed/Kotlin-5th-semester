package ru.spbau.mit.interpreter.ast.visitors

import ru.spbau.mit.interpreter.ast.*
import ru.spbau.mit.interpreter.ast.nodes.*
import ru.spbau.mit.interpreter.ast.nodes.Number

class InterpretingASTVisitor : ASTVisitor<Int?> {
    private val stack: MutableList<StackFrame> = mutableListOf(StackFrame())

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

    private fun visitCondition(expression: Expression): Boolean =
            visitExpression(expression) != 0

    override fun visitExpression(expression: Expression): Int =
            expression.accept(this)!!

    override fun visitFile(file: File): Int? = visit(file.block)

    override fun visitBlock(block: Block): Int? =
            block.statements
                    .asSequence()
                    .filter { statement -> statement !is Identifier }
                    .map { statement -> visit(statement) }
                    .firstOrNull { returnValue -> returnValue != null }

    override fun visitFunctionDefinition(functionDefinition: FunctionDefinition): Int? {
        if (functionDefinition.name in stack.last().functions) {
            throw FunctionRedefinitionException(
                    functionDefinition.position,
                    functionDefinition.name
            )
        }
        stack.last().functions[functionDefinition.name] = functionDefinition
        return null
    }

    override fun visitParenthesizedBlock(parenthesizedBlock: ParenthesizedBlock): Int? {
        addStackFrame()
        val result = parenthesizedBlock.underlyingBlock.accept(this)
        removeStackFrame()
        return result
    }

    override fun visitVariableDefinition(variableDefinition: VariableDefinition): Int? {
        if (variableDefinition.name in stack.last().variables) {
            throw VariableRedefinitionException(
                    variableDefinition.position,
                    variableDefinition.name
            )
        }
        stack.last().variables[variableDefinition.name] =
                visitExpression(variableDefinition.value)
        return null
    }

    override fun visitWhileCycle(whileCycle: WhileCycle): Int? {
        while (visitCondition(whileCycle.condition)) {
            val blockResult = whileCycle.body.accept(this)
            if (blockResult != null) {
                return blockResult
            }
        }
        return null
    }

    override fun visitIfClause(ifClause: IfClause): Int? =
            if (visitCondition(ifClause.condition))
                visit(ifClause.thenBody)
            else
                ifClause.elseBody?.let(this::visit)

    override fun visitVariableAssignment(variableAssignment: VariableAssignment): Int? {
        val stackFrame = findStackFrameForVariable(variableAssignment.name)
                ?: throw VariableUndefinedException(
                        variableAssignment.position,
                        variableAssignment.name
                )
        stackFrame.variables[variableAssignment.name] =
                visitExpression(variableAssignment.newValue)
        return null
    }

    override fun visitPrintlnCall(printlnCall: PrintlnCall): Int? {
        println(printlnCall.parameters
                .map(this::visitExpression)
                .joinToString(" ")
        )
        return null
    }

    override fun visitReturnStatement(returnStatement: ReturnStatement): Int {
        if (stackDepth() == 0) {
            throw InvalidReturnStatementException(returnStatement.position)
        }
        return visitExpression(returnStatement.expression)
    }

    override fun visitFunctionCall(functionCall: FunctionCall): Int {
        val function = findFunction(functionCall.name)
                ?: throw FunctionUndefinedException(
                functionCall.position,
                functionCall.name
        )
        addStackFrame(StackFrame(
                function.parameterNames
                        .zip(functionCall.parameters.map(this::visitExpression))
                        .toMap()
                        .toMutableMap()
        )
        )
        val callResult = visit(function.body)
        removeStackFrame()
        return callResult ?: 0
    }

    override fun visitIdentifier(identifier: Identifier): Int =
            findStackFrameForVariable(identifier.text)
                    ?.variables
                    ?.get(identifier.text)
                    ?: throw VariableUndefinedException(
                            identifier.position,
                            identifier.text
                    )

    override fun visitNumber(number: Number): Int = number.value

    override fun visitParenthesizedExpression(
            parenthesizedExpression: ParenthesizedExpression
    ): Int = visitExpression(parenthesizedExpression.underlyingExpression)

    override fun visitBinaryExpression(binaryExpression: BinaryExpression): Int =
            binaryExpression.operator.operation(
                    visitExpression(binaryExpression.left),
                    visitExpression(binaryExpression.right)
            )
}