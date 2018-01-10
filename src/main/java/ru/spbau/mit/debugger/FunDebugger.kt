package ru.spbau.mit.debugger

import kotlinx.coroutines.experimental.*
import ru.spbau.mit.ast.nodes.Statement
import ru.spbau.mit.interpreter.InterpretingASTVisitor
import ru.spbau.mit.interpreter.buildASTFromFile
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.suspendCoroutine

abstract class Suspendable {
    private var continuation: Continuation<Unit>? = null

    protected suspend fun suspendWithAction(
            action: () -> Unit
    ) = suspendCoroutine<Unit> {
        continuation = it
        action()
    }

    internal fun resume() = continuation?.resume(Unit)

    protected fun resumeWithException(throwable: Throwable) =
            continuation?.resumeWithException(throwable)
}

fun <T> runDebugging(block: suspend CoroutineScope.(FunDebugger) -> T) =
        runBlocking(newSingleThreadContext("Debugger thread")) {
            val debugger = FunDebugger(coroutineContext)
            block(debugger)
        }

class FunDebugger internal constructor(
        val context: CoroutineContext
) : Suspendable(), CommandEvaluator {
    private var debugState: DebugState? = null

    internal val isRunning
        get() = debugState?.isRunning == true

    var exited = false
        private set

    override suspend fun evaluateLoadCommand(loadCommand: LoadCommand) {
        debugState?.cancel(
                CancellationException(
                        "Finished debugging as a result of " +
                                "LoadCommand(${loadCommand.fileName}) evaluation"
                )
        )
        debugState = DebugState(loadCommand.fileName)
    }

    override fun evaluateBreakpointCommand(breakpointCommand: BreakpointCommand) {
        debugState?.run {
            breakpoints[breakpointCommand.line] = breakpointCommand.toBreakpoint()
        }
    }

    override fun evaluateConditionCommand(conditionCommand: ConditionCommand) {
        debugState?.run {
            breakpoints[conditionCommand.line] = conditionCommand.toBreakpoint()
        }
    }

    override fun evaluateListCommand() {
        print(
                buildString {
                    appendln("Breakpoints:")
                    debugState?.run {
                        for ((line, breakpoint) in breakpoints) {
                            val description = when (breakpoint) {
                                is UnconditionalBreakpoint -> "unconditional"
                                is ConditionalBreakpoint -> "condition = (${breakpoint.condition})"
                            }
                            appendln("\tline $line: $description")
                        }
                    }
                }
        )
    }

    override fun evaluateRemoveCommand(removeCommand: RemoveCommand) {
        debugState?.run {
            breakpoints.remove(removeCommand.line)
        }
    }

    override suspend fun evaluateRunCommand() {
        when {
            isRunning -> {
                debugState?.cancel(
                        CancellationException("An exception occurred in debugger")
                )
                throw DebugAlreadyRunningException()
            }
            debugState == null -> throw NoFileLoadedException()
            else -> suspendWithAction { debugState?.runDebug() }
        }
    }

    override suspend fun evaluateContinueCommand() {
        if (isRunning) {
            suspendWithAction { debugState?.resume() }
        } else {
            throw DebugNotRunningException()
        }
    }

    override suspend fun evaluateStopCommand() {
        if (isRunning) {
            debugState?.cancel(
                    CancellationException(
                            "Finished debugging as a result of StopCommand evaluation"
                    )
            )
        } else {
            throw DebugNotRunningException()
        }
        println("Debug stopped")
    }

    override fun evaluateEvaluateCommand(evaluateCommand: EvaluateCommand) {
        val result = runBlocking {
            debugState?.interpreter?.let {
                evaluateCommand.expression.accept(it)
            }
        }
        println("Evaluation result: $result")
    }

    override suspend fun evaluateExitCommand() {
        if (isRunning) {
            debugState?.cancel(
                    CancellationException(
                            "Finished debugging as a result of ExitCommand evaluation"
                    )
            )
        }
        exited = true
    }

    private inner class DebugState(fileName: String) : Suspendable() {
        val breakpoints = mutableMapOf<Int,Breakpoint>()
        val interpreter = DebuggingASTVisitor()
        val ast = buildASTFromFile(fileName)

        private var job: Job? = null

        val isRunning
            get() = job?.isActive == true

        fun runDebug() {
            job = launch(context) {
                ast.accept(interpreter)
                this@FunDebugger.resume()
            }
        }

        suspend fun cancel(throwable: Throwable) {
            job?.cancel(throwable)
            this@DebugState.resumeWithException(throwable)
        }

        private suspend fun checkBreakpoint(line: Int) {
            val breakpoint = breakpoints[line] ?: return
            if (breakpoint.shouldStopInCurrentContext(interpreter)) {
                when (breakpoint) {
                    is UnconditionalBreakpoint ->
                        println("Stopping at line $line unconditionally")
                    is ConditionalBreakpoint ->
                        println("Stopping at line $line because " +
                                "(${breakpoint.condition}) evaluates to \"true\"")
                }
                this@DebugState.suspendWithAction { this@FunDebugger.resume() }
            }
        }

        private inner class DebuggingASTVisitor : InterpretingASTVisitor() {
            override suspend fun interpretStatement(statement: Statement): Int? {
                checkBreakpoint(statement.position.first)
                return super.interpretStatement(statement)
            }
        }
    }
}
