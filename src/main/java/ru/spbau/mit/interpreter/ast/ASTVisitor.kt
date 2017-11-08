package ru.spbau.mit.interpreter.ast

class ASTVisitor {
    private val stack: MutableList<StackFrame> = mutableListOf(StackFrame())

    private inner class StackFrame(
            val variables: MutableMap<String,Int> = mutableMapOf(),
            val functions: MutableMap<String,FunctionDefinition> = mutableMapOf()
    )

    private fun stackDepth(): Int = stack.size - 1

    private fun addStackFrame(stackFrame: StackFrame = StackFrame()) =
            stack.add(stackFrame)

    private fun removeStackFrame() = stack.removeAt(stack.lastIndex)

    private fun findStackFrameForVariable(variable: String): StackFrame? {
        return stack.lastOrNull { frame -> variable in frame.variables }
    }

    private fun findFunction(function: String): FunctionDefinition? {
        return stack
                .lastOrNull { frame -> function in frame.functions }
                ?.functions
                ?.get(function)
    }

    fun visit(astNode: ASTNode): Int? {
        return astNode.accept(this)
    }

    private fun visitExpression(expression: Expression): Int {
        return expression.accept(this)
    }

    private fun visitCondition(expression: Expression): Boolean {
        return visitExpression(expression) != 0
    }

    fun visitFile(file: File) {
        file.block.accept(this)
    }

    fun visitBlock(block: Block): Int? {
        block.statements.asSequence().forEach { statement ->
            when (statement) {
                is Identifier -> return@forEach
                else -> return visit(statement) ?: return@forEach
            }
        }
        return null
    }

    fun visitFunctionDefinition(functionDefinition: FunctionDefinition) {
        when (functionDefinition.name) {
            in stack.last().functions -> throw FunctionRedefinitionException(
                    functionDefinition.position,
                    functionDefinition.name)
            else -> stack.last().functions[functionDefinition.name] = functionDefinition
        }
    }

    fun visitBracedBlock(bracedBlock: BracedBlock): Int? {
        addStackFrame()
        val result = bracedBlock.underlyingBlock.accept(this)
        removeStackFrame()
        return result
    }

    fun visitVariableDefinition(variableDefinition: VariableDefinition) {
        when (variableDefinition.name) {
            in stack.last().variables -> throw VariableRedefinitionException(
                    variableDefinition.position,
                    variableDefinition.name)
            else -> stack.last().variables[variableDefinition.name] =
                    variableDefinition.value.accept(this)
        }
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

    fun visitIfClause(ifClause: IfClause): Int? {
        return when {
            visitCondition(ifClause.condition) -> visit(ifClause.thenBody)
            ifClause.elseBody != null -> visit(ifClause.elseBody)
            else -> null
        }
    }

    fun visitVariableAssignment(variableAssignment: VariableAssignment) {
        val stackFrame = findStackFrameForVariable(variableAssignment.name)
        when (stackFrame) {
            null -> throw VariableUndefinedException(
                    variableAssignment.position,
                    variableAssignment.name)
            else -> stackFrame.variables[variableAssignment.name] =
                    visitExpression(variableAssignment.newValue)
        }
    }

    fun visitPrintlnCall(printlnCall: PrintlnCall) {
        println(printlnCall.parameters
                .map(this::visitExpression)
                .joinToString(" "))
    }

    fun visitReturnStatement(returnStatement: ReturnStatement): Int {
        when (stackDepth()) {
            0 -> throw InvalidReturnStatementException(returnStatement.position)
            else -> return visitExpression(returnStatement.expression)
        }
    }

    fun visitFunctionCall(functionCall: FunctionCall): Int {
        val function = findFunction(functionCall.name)
        when (function) {
            null -> throw FunctionUndefinedException(
                    functionCall.position,
                    functionCall.name)
            else -> {
                addStackFrame(StackFrame(
                        function.parameterNames
                                .zip(functionCall.parameters.map(this::visitExpression))
                                .toMap()
                                .toMutableMap()))
                val callResult = visit(function.body)
                removeStackFrame()
                return callResult ?: 0
            }
        }
    }

    fun visitIdentifier(identifier: Identifier): Int {
        val stackFrame = findStackFrameForVariable(identifier.text)
        when (stackFrame) {
            null -> throw VariableUndefinedException(
                    identifier.position,
                    identifier.text)
            else -> return stackFrame.variables[identifier.text]!!
        }
    }

    fun visitNumber(number: Number): Int {
        return number.value
    }

    fun visitBracedExpression(bracedExpression: BracedExpression): Int {
        return visitExpression(bracedExpression.underlyingExpression)
    }

    fun visitBinaryExpression(binaryExpression: BinaryExpression): Int {
        return binaryExpression.operator.first(
                visitExpression(binaryExpression.left),
                visitExpression(binaryExpression.right)
        )
    }
}