package ru.spbau.mit.interpreter.ast

import ru.spbau.mit.interpreter.ast.nodes.*
import ru.spbau.mit.interpreter.ast.nodes.Number

class ASTVisitor {
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

    fun visit(astNode: ASTNode): Int? = astNode.accept(this)

    private fun visitExpression(expression: Expression): Int = expression.accept(this)

    private fun visitCondition(expression: Expression): Boolean =
            visitExpression(expression) != 0

    fun visitFile(file: File) {
        file.block.accept(this)
    }

    fun visitBlock(block: Block): Int? {
        return block.statements
                .asSequence()
                .filter { it !is Identifier }
                .map { visit(it) }
                .firstOrNull { it != null }
    }

    fun visitFunctionDefinition(functionDefinition: FunctionDefinition) {
        if (functionDefinition.name in stack.last().functions) {
            throw FunctionRedefinitionException(
                    functionDefinition.position,
                    functionDefinition.name
            )
        }
        stack.last().functions[functionDefinition.name] = functionDefinition
    }

    fun visitParenthesizedBlock(parenthesizedBlock: ParenthesizedBlock): Int? {
        addStackFrame()
        val result = parenthesizedBlock.underlyingBlock.accept(this)
        removeStackFrame()
        return result
    }

    fun visitVariableDefinition(variableDefinition: VariableDefinition) {
        if (variableDefinition.name in stack.last().variables) {
            throw VariableRedefinitionException(
                    variableDefinition.position,
                    variableDefinition.name)
        }
        stack.last().variables[variableDefinition.name] =
                variableDefinition.value.accept(this)
    }

    fun visitWhileCycle(whileCycle: WhileCycle): Int? {
        while (visitCondition(whileCycle.condition)) {
            val blockResult = whileCycle.body.accept(this)
            if (blockResult != null) {
                return blockResult
            }
        }
        return null
    }

    fun visitIfClause(ifClause: IfClause): Int? =
            if (visitCondition(ifClause.condition))
                visit(ifClause.thenBody)
            else
                ifClause.elseBody?.let(this::visit)

    fun visitVariableAssignment(variableAssignment: VariableAssignment) {
        val stackFrame = findStackFrameForVariable(variableAssignment.name)
                ?: throw VariableUndefinedException(
                    variableAssignment.position,
                    variableAssignment.name
                )
        stackFrame.variables[variableAssignment.name] =
                visitExpression(variableAssignment.newValue)
    }

    fun visitPrintlnCall(printlnCall: PrintlnCall) {
        println(printlnCall.parameters
                .map(this::visitExpression)
                .joinToString(" "))
    }

    fun visitReturnStatement(returnStatement: ReturnStatement): Int {
        if (stackDepth() == 0) {
            throw InvalidReturnStatementException(returnStatement.position)
        }
        return visitExpression(returnStatement.expression)
    }

    fun visitFunctionCall(functionCall: FunctionCall): Int {
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

    fun visitIdentifier(identifier: Identifier): Int =
            findStackFrameForVariable(identifier.text)?.variables?.get(identifier.text)
                ?: throw VariableUndefinedException(
                        identifier.position,
                        identifier.text
                )

    fun visitNumber(number: Number): Int = number.value

    fun visitParenthesizedExpression(parenthesizedExpression: ParenthesizedExpression): Int =
            visitExpression(parenthesizedExpression.underlyingExpression)

    fun visitBinaryExpression(binaryExpression: BinaryExpression): Int =
        binaryExpression.operator.first(
                visitExpression(binaryExpression.left),
                visitExpression(binaryExpression.right)
        )
}