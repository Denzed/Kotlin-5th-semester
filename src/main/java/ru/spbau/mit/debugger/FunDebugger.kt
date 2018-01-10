package ru.spbau.mit.debugger

import kotlinx.coroutines.experimental.*
import ru.spbau.mit.ast.nodes.Expression
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
    private var debugState: DebugState? = null

    internal val isRunning
        get() = debugState?.isRunning == true

    var exited = false
        private set

    suspend fun load(fileName: String) {
        debugState?.cancel(
                CancellationException(
                        "Finished debug before loading a new file $fileName"
                )
        )
        debugState = DebugState(fileName)
    }

    fun addBreakpoint(line: Int, breakpoint: Breakpoint) {
        debugState?.run {
            breakpoints[line] = breakpoint
        }
    }

    fun list() {
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

    fun removeBreakpoint(line: Int) {
        debugState?.run {
            breakpoints.remove(line)
        }
    }

    suspend fun runDebug() {
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

    suspend fun continueDebug() {
        if (isRunning) {
            suspendWithAction { debugState?.resume() }
        } else {
            throw DebugNotRunningException()
        }
    }

    suspend fun stopDebug() {
        if (isRunning) {
            debugState?.cancel(
                    CancellationException(
                            "Stopped debug"
                    )
            )
        } else {
            throw DebugNotRunningException()
        }
        println("Debug stopped")
    }

    fun evaluateExpression(expression: Expression) {
        val result = runBlocking {
            debugState?.interpreter?.let {
                expression.accept(it)
            }
        }
        println("Evaluation result: $result")
    }

    suspend fun exitDebugger() {
        if (isRunning) {
            debugState?.cancel(
                    CancellationException(
                            "Finished debug before debugger exitDebugger"
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
