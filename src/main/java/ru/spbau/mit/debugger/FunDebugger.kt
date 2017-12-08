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
) : Suspendable() {
    private val breakpoints = mutableMapOf<Int,Breakpoint>()
    private var debugState: DebugState? = null

    internal val isRunning
        get() = debugState?.isRunning == true

    private var exited = false

    val hasExited
        get() = exited

    internal suspend fun evaluateLoadCommand(loadCommand: LoadCommand) {
        debugState?.cancel(
                CancellationException(
                        "Finished debugging as a result of " +
                                "LoadCommand(${loadCommand.fileName}) evaluation"
                )
        )
        breakpoints.clear()
        debugState = DebugState(loadCommand.fileName)
    }

    internal fun evaluateBreakpointCommand(breakpointCommand: BreakpointCommand) {
        breakpoints[breakpointCommand.line] = breakpointCommand.toBreakpoint()
    }

    internal fun evaluateConditionCommand(conditionCommand: ConditionCommand) {
        breakpoints[conditionCommand.line] = conditionCommand.toBreakpoint()
    }

    internal fun evaluateListCommand() {
        print(
                buildString {
                    appendln("Breakpoints:")
                    for ((line, breakpoint) in breakpoints) {
                        val description = when (breakpoint) {
                            is UnconditionalBreakpoint -> "unconditional"
                            is ConditionalBreakpoint -> "condition = (${breakpoint.condition})"
                            else -> throw InvalidBreakpointTypeException(breakpoint)
                        }
                        appendln("\tline $line: $description")
                    }
                }
        )
    }

    internal fun evaluateRemoveCommand(removeCommand: RemoveCommand) {
        breakpoints.remove(removeCommand.line)
    }

    internal suspend fun evaluateRunCommand() {
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

    internal suspend fun evaluateContinueCommand() {
        if (isRunning) {
            suspendWithAction { debugState?.resume() }
        } else {
            throw DebugNotRunningException()
        }
    }

    internal suspend fun evaluateStopCommand() {
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

    internal fun evaluateEvaluateCommand(evaluateCommand: EvaluateCommand) {
        val result = runBlocking {
            debugState?.interpreter?.let {
                evaluateCommand.expression.accept(it)
            }
        }
        println("Evaluation result: $result")
    }

    internal suspend fun evaluateExitCommand() {
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
            when (breakpoint) {
                is UnconditionalBreakpoint -> {
                    println("Stopping at line $line unconditionally")
                    this@DebugState.suspendWithAction { this@FunDebugger.resume() }
                }
                is ConditionalBreakpoint ->
                    if (breakpoint.condition.accept(interpreter) != 0) {
                        println("Stopping at line $line because " +
                                "(${breakpoint.condition}) evaluates to \"true\"")
                        this@DebugState.suspendWithAction { this@FunDebugger.resume() }
                    }
                else -> throw InvalidBreakpointTypeException(breakpoint)
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
