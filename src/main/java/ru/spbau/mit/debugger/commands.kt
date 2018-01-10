package ru.spbau.mit.debugger

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.ast.PositionForgettingASTBuilder
import ru.spbau.mit.ast.nodes.Expression
import ru.spbau.mit.interpreter.InterpretingASTVisitor
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser

abstract class Command {
    abstract suspend fun evaluate(debugger: FunDebugger)
}

data class LoadCommand(private val fileName: String) : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.load(fileName)
}

sealed class Breakpoint {
    abstract suspend fun shouldStopInCurrentContext(
            interpreter: InterpretingASTVisitor
    ): Boolean
}

data class ConditionalBreakpoint(val condition: Expression) : Breakpoint() {
    override suspend fun shouldStopInCurrentContext(
            interpreter: InterpretingASTVisitor
    ): Boolean = condition.accept(interpreter) != 0
}

object UnconditionalBreakpoint : Breakpoint() {
    override suspend fun shouldStopInCurrentContext(
            interpreter: InterpretingASTVisitor
    ): Boolean = true
}

sealed class AbstractBreakpointCommand : Command() {
    protected abstract val line: Int
}

data class BreakpointCommand(override val line: Int) : AbstractBreakpointCommand() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.addBreakpoint(line, UnconditionalBreakpoint)
}

data class ConditionCommand(
        override val line: Int,
        private val condition: Expression
) : AbstractBreakpointCommand() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.addBreakpoint(line, ConditionalBreakpoint(condition))
}

object ListCommand : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.list()
}

data class RemoveCommand(private val line: Int) : Command() {
    suspend override fun evaluate(debugger: FunDebugger) =
            debugger.removeBreakpoint(line)
}

object RunCommand : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.runDebug()
}

data class EvaluateCommand(private val expression: Expression) : Command() {
    suspend override fun evaluate(debugger: FunDebugger) =
            debugger.evaluateExpression(expression)
}

object StopCommand : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.stopDebug()
}

object ContinueCommand : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.continueDebug()
}

object ExitCommand : Command() {
    suspend override fun evaluate(debugger: FunDebugger) =
            debugger.exitDebugger()
}

fun buildCommand(commandString: String): Command {
    fun buildExpression(expressionString: String): Expression {
        val lexer = FunLexer(CharStreams.fromString(expressionString))
        val parser = FunParser(BufferedTokenStream(lexer))

        return parser
                .expression()
                .accept(PositionForgettingASTBuilder)
                as Expression
    }

    val tokens = commandString.split(" ")
    val command = tokens[0]
    val options = tokens.drop(1)

    return when (command) {
        "load" -> LoadCommand(options.joinToString(" "))
        "breakpoint" -> BreakpointCommand(options[0].toInt())
        "condition" -> ConditionCommand(
                options[0].toInt(),
                buildExpression(options.drop(1).joinToString(" "))
        )
        "list" -> ListCommand
        "remove" -> RemoveCommand(options[0].toInt())
        "run" -> RunCommand
        "evaluateExpression" -> EvaluateCommand(
                buildExpression(options.joinToString(" "))
        )
        "stop" -> StopCommand
        "continue" -> ContinueCommand
        "exitDebugger" -> ExitCommand
        else -> throw UnknownCommandException(commandString)
    }
}
