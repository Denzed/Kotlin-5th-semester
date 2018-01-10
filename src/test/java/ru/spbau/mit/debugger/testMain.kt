package ru.spbau.mit.debugger

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class TestMain {
    private val testFile = javaClass
            .classLoader
            .getResource("test.fun")!!
            .file

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
    fun testSimple() {
        val input = """
            load $testFile
            breakpoint 8
            condition 9 i % 2 == 0
            run
            list
            continue
            evaluateExpression fib(i)
            remove 9
            list
            condition 9 i == 5
            continue
            stop
            exitDebugger
        """.trimIndent()
        System.setIn(input.byteInputStream())
        main(arrayOf())
        assertPrints(
                """
                |Welcome to Fun debugger! List of available commands:
                |   load <filename> – loads file to memory. If there is a file loaded already, its debugging will be cancelled and all the breakpoints will be removed.
                |   breakpoint <line-number> – sets a breakpoint at the given line.
                |   condition <line-number> <condition-expression> – sets a conditional breakpoint at the given line. If a line already contains a breakpoint, it will be overwritten.
                |   list – lists breakpoints with line numbers and conditions.
                |   remove <line-number> – removes breakpoint from the given line.
                |   run – runs the loaded file. Everything printed would be forwarded to standard output. If interpreted line contains a breakpoint and its condition is satisfied, the interpretation is paused. If the program was already running, an error is thrown.
                |   evaluateExpression <expression> – evaluates an expression in the context of current line.
                |   stop – stops the interpretation.
                |   continue – continues the interpretation until the next breakpoint or the file end.
                |   exitDebugger – stops the interpretation and exits the debugger.
                |> > > > Stopping at line 8 unconditionally
                |> Breakpoints:
                |	line 8: unconditional
                |	line 9: condition = (i % 2 == 0)
                |> 1 1
                |Stopping at line 9 because (i % 2 == 0) evaluates to "true"
                |> Evaluation result: 2
                |> > Breakpoints:
                |	line 8: unconditional
                |> > 2 2
                |3 3
                |4 5
                |Stopping at line 9 because (i == 5) evaluates to "true"
                |> Debug stopped
                |>${" "}
                """.trimMargin()
        )
    }

    @Test
    fun testWrongInput() {
        val input = """
            flit
            exitDebugger
        """.trimIndent()
        System.setIn(input.byteInputStream())
        main(arrayOf())
        assertPrints(
                """
                |Welcome to Fun debugger! List of available commands:
                |   load <filename> – loads file to memory. If there is a file loaded already, its debugging will be cancelled and all the breakpoints will be removed.
                |   breakpoint <line-number> – sets a breakpoint at the given line.
                |   condition <line-number> <condition-expression> – sets a conditional breakpoint at the given line. If a line already contains a breakpoint, it will be overwritten.
                |   list – lists breakpoints with line numbers and conditions.
                |   remove <line-number> – removes breakpoint from the given line.
                |   run – runs the loaded file. Everything printed would be forwarded to standard output. If interpreted line contains a breakpoint and its condition is satisfied, the interpretation is paused. If the program was already running, an error is thrown.
                |   evaluateExpression <expression> – evaluates an expression in the context of current line.
                |   stop – stops the interpretation.
                |   continue – continues the interpretation until the next breakpoint or the file end.
                |   exitDebugger – stops the interpretation and exits the debugger.
                |> Unknown command type met: "flit"
                |>${" "}
                """.trimMargin()
        )
    }
}
