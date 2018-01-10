package ru.spbau.mit.debugger

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.ast.PositionForgettingASTBuilder
import ru.spbau.mit.ast.nodes.Expression
import ru.spbau.mit.interpreter.InterpretingASTVisitor
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser

interface CommandEvaluator {
    suspend fun evaluateLoadCommand(loadCommand: LoadCommand)
    fun evaluateBreakpointCommand(breakpointCommand: BreakpointCommand)
    fun evaluateConditionCommand(conditionCommand: ConditionCommand)
    fun evaluateListCommand()
    fun evaluateRemoveCommand(removeCommand: RemoveCommand)
    suspend fun evaluateRunCommand()
    suspend fun evaluateContinueCommand()
    suspend fun evaluateStopCommand()
    fun evaluateEvaluateCommand(evaluateCommand: EvaluateCommand)
    suspend fun evaluateExitCommand()
}

abstract class Command {
    abstract suspend fun evaluate(evaluator: CommandEvaluator)
}

data class LoadCommand(val fileName: String) : Command() {
    override suspend fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateLoadCommand(this)
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
    abstract val line: Int

    abstract fun toBreakpoint(): Breakpoint
}

data class BreakpointCommand(override val line: Int) : AbstractBreakpointCommand() {
    override suspend fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateBreakpointCommand(this)

    override fun toBreakpoint() = UnconditionalBreakpoint
}

data class ConditionCommand(
        override val line: Int,
        private val condition: Expression
) : AbstractBreakpointCommand() {
    override suspend fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateConditionCommand(this)

    override fun toBreakpoint(): Breakpoint =
            ConditionalBreakpoint(condition)
}

object ListCommand : Command() {
    override suspend fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateListCommand()
}

data class RemoveCommand(val line: Int) : Command() {
    suspend override fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateRemoveCommand(this)
}

object RunCommand : Command() {
    override suspend fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateRunCommand()
}

data class EvaluateCommand(val expression: Expression) : Command() {
    suspend override fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateEvaluateCommand(this)
}

object StopCommand : Command() {
    override suspend fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateStopCommand()
}

object ContinueCommand : Command() {
    override suspend fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateContinueCommand()
}

object ExitCommand : Command() {
    suspend override fun evaluate(evaluator: CommandEvaluator) =
            evaluator.evaluateExitCommand()
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
        "evaluate" -> EvaluateCommand(
                buildExpression(options.joinToString(" "))
        )
        "stop" -> StopCommand
        "continue" -> ContinueCommand
        "exit" -> ExitCommand
        else -> throw UnknownCommandException(commandString)
    }
}
