package ru.spbau.mit.debugger

import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.spbau.mit.ast.blankPosition
import ru.spbau.mit.ast.nodes.BinaryExpression
import ru.spbau.mit.ast.nodes.FunctionCall
import ru.spbau.mit.ast.nodes.Identifier
import ru.spbau.mit.ast.nodes.Number
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class TestDebugger {
    private val testFile = javaClass
            .classLoader
            .getResource("test.fun")!!
            .file

    private val condition =
            BinaryExpression(
                    blankPosition,
                    BinaryExpression(
                            blankPosition,
                            Identifier(blankPosition, "i"),
                            BinaryExpression.Companion.Operator.MOD,
                            Number(blankPosition,2)
                    ),
                    BinaryExpression.Companion.Operator.EQ,
                    Number(blankPosition, 1)
            )

    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()

    @Before
    fun setUpStreams() {
        System.setOut(PrintStream(outContent))
        System.setErr(PrintStream(errContent))
    }

    @After
    fun cleanUpStreams() {
        assertEquals(0, errContent.size(), errContent.toString())
        outContent.reset()
        errContent.reset()
        System.setErr(null)
        System.setOut(null)
    }

    private fun assertPrints(text: String) {
        outContent.close()
        assertEquals(text, outContent.toString())
    }

    @Test
    fun testRunCommand() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            RunCommand.evaluate(debugger)
        }

        assertPrints("""
            1 1
            2 2
            3 3
            4 5
            5 8

            """.trimIndent()
        )
    }

    @Test
    fun testRerunning() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            RunCommand.evaluate(debugger)
            RunCommand.evaluate(debugger)
        }

        assertPrints("""
            1 1
            2 2
            3 3
            4 5
            5 8
            1 1
            2 2
            3 3
            4 5
            5 8

            """.trimIndent()
        )
    }

    @Test(expected = DebugAlreadyRunningException::class)
    fun testDoubleRun() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            BreakpointCommand(9).evaluate(debugger)
            RunCommand.evaluate(debugger)
            RunCommand.evaluate(debugger)
        }
    }

    @Test(expected = NoFileLoadedException::class)
    fun testRunWithoutLoad() {
        runDebugging { debugger ->
            RunCommand.evaluate(debugger)
        }
    }

    @Test
    fun testBreakpointCommand() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            BreakpointCommand(9).evaluate(debugger)
            RunCommand.evaluate(debugger)
            while (debugger.isRunning) {
                ContinueCommand.evaluate(debugger)
            }
        }

        assertPrints("""
            Stopping at line 9 unconditionally
            1 1
            Stopping at line 9 unconditionally
            2 2
            Stopping at line 9 unconditionally
            3 3
            Stopping at line 9 unconditionally
            4 5
            Stopping at line 9 unconditionally
            5 8
            Stopping at line 9 unconditionally

            """.trimIndent()
        )
    }

    @Test
    fun testConditionalBreakpointCommand() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            ConditionCommand(9, condition).evaluate(debugger)
            RunCommand.evaluate(debugger)
            while (debugger.isRunning) {
                ContinueCommand.evaluate(debugger)
            }
        }

        assertPrints("""
            Stopping at line 9 because (i % 2 == 1) evaluates to "true"
            1 1
            2 2
            Stopping at line 9 because (i % 2 == 1) evaluates to "true"
            3 3
            4 5
            Stopping at line 9 because (i % 2 == 1) evaluates to "true"
            5 8

            """.trimIndent()
        )
    }

    @Test
    fun testStopCommand() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            ConditionCommand(9, condition).evaluate(debugger)
            RunCommand.evaluate(debugger)
            ContinueCommand.evaluate(debugger)
            StopCommand.evaluate(debugger)
        }

        assertPrints("""
            Stopping at line 9 because (i % 2 == 1) evaluates to "true"
            1 1
            2 2
            Stopping at line 9 because (i % 2 == 1) evaluates to "true"
            Debug stopped

            """.trimIndent()
        )
    }

    @Test(expected = DebugNotRunningException::class)
    fun testDoubleStop() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            BreakpointCommand(9).evaluate(debugger)
            RunCommand.evaluate(debugger)
            StopCommand.evaluate(debugger)
            StopCommand.evaluate(debugger)
        }
    }

    @Test
    fun testListCommand() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            BreakpointCommand(8).evaluate(debugger)
            ConditionCommand(9, condition).evaluate(debugger)
            ListCommand.evaluate(debugger)
        }

        assertPrints("""
            Breakpoints:
            ${"\t"}line 8: unconditional
            ${"\t"}line 9: condition = (i % 2 == 1)

            """.trimIndent()
        )
    }

    @Test
    fun testRemoveCommand() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            BreakpointCommand(8).evaluate(debugger)
            ConditionCommand(9, condition).evaluate(debugger)
            ListCommand.evaluate(debugger)
            RemoveCommand(8).evaluate(debugger)
            ListCommand.evaluate(debugger)
        }

        assertPrints("""
            Breakpoints:
            ${"\t"}line 8: unconditional
            ${"\t"}line 9: condition = (i % 2 == 1)
            Breakpoints:
            ${"\t"}line 9: condition = (i % 2 == 1)

            """.trimIndent()
        )
    }

    @Test
    fun testBreakpointOverwrite() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            BreakpointCommand(8).evaluate(debugger)
            ListCommand.evaluate(debugger)
            ConditionCommand(8, condition).evaluate(debugger)
            ListCommand.evaluate(debugger)
        }

        assertPrints("""
            Breakpoints:
            ${"\t"}line 8: unconditional
            Breakpoints:
            ${"\t"}line 8: condition = (i % 2 == 1)

            """.trimIndent()
        )
    }

    @Test
    fun testEvaluateCommand() {
        runDebugging { debugger ->
            LoadCommand(testFile).evaluate(debugger)
            ConditionCommand(
                    9,
                    BinaryExpression(
                            blankPosition,
                            Identifier(blankPosition, "i"),
                            BinaryExpression.Companion.Operator.EQ,
                            Number(blankPosition, 4)
                    )
            ).evaluate(debugger)
            RunCommand.evaluate(debugger)
            EvaluateCommand(
                    FunctionCall(
                            blankPosition,
                            "fib",
                            listOf(Identifier(blankPosition, "i"))
                    )
            ).evaluate(debugger)
            ContinueCommand.evaluate(debugger)
        }

        assertPrints("""
            1 1
            2 2
            3 3
            Stopping at line 9 because (i == 4) evaluates to "true"
            Evaluation result: 5
            4 5
            5 8

            """.trimIndent()
        )
    }
}