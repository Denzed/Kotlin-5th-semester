package ru.spbau.mit.debugger

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.ast.ASTBuilder
import ru.spbau.mit.ast.WithBlankPosition
import ru.spbau.mit.ast.nodes.Expression
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser

abstract class Command {
    abstract suspend fun evaluate(debugger: FunDebugger)
}

data class LoadCommand(val fileName: String) : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.evaluateLoadCommand(this)
}

abstract class Breakpoint

object UnconditionalBreakpoint : Breakpoint()

data class ConditionalBreakpoint(val condition: Expression) : Breakpoint()

sealed class AbstractBreakpoint : Command() {
    abstract val line: Int

    abstract fun toBreakpoint(): Breakpoint
}

data class BreakpointCommand(override val line: Int) : AbstractBreakpoint() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.evaluateBreakpointCommand(this)

    override fun toBreakpoint() = UnconditionalBreakpoint
}

data class ConditionCommand(
        override val line: Int,
        private val condition: Expression
) : AbstractBreakpoint() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.evaluateConditionCommand(this)

    override fun toBreakpoint(): Breakpoint =
            ConditionalBreakpoint(condition)
}

object ListCommand : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.evaluateListCommand()
}

data class RemoveCommand(val line: Int) : Command() {
    suspend override fun evaluate(debugger: FunDebugger) =
            debugger.evaluateRemoveCommand(this)
}

object RunCommand : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.evaluateRunCommand()
}

data class EvaluateCommand(val expression: Expression) : Command() {
    suspend override fun evaluate(debugger: FunDebugger) =
            debugger.evaluateEvaluateCommand(this)
}

object StopCommand : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.evaluateStopCommand()
}

object ContinueCommand : Command() {
    override suspend fun evaluate(debugger: FunDebugger) =
            debugger.evaluateContinueCommand()
}

object ExitCommand : Command() {
    suspend override fun evaluate(debugger: FunDebugger) =
            debugger.evaluateExitCommand()
}

fun buildCommand(commandString: String): Command {
    fun buildExpression(expressionString: String): Expression {
        val lexer = FunLexer(CharStreams.fromString(expressionString))
        val parser = FunParser(BufferedTokenStream(lexer))

        return parser
                .expression()
                .accept(object : ASTBuilder(), WithBlankPosition {})
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
        "evaluate" -> EvaluateCommand(
                buildExpression(options.joinToString(" "))
        )
        "stop" -> StopCommand
        "continue" -> ContinueCommand
        "exit" -> ExitCommand
        else -> throw UnknownCommandException(commandString)
    }
}
