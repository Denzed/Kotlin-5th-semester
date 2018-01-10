package ru.spbau.mit.debugger

import org.junit.Test
import ru.spbau.mit.ast.blankPosition
import ru.spbau.mit.ast.nodes.BinaryExpression
import ru.spbau.mit.ast.nodes.FunctionCall
import ru.spbau.mit.ast.nodes.Number
import kotlin.test.assertEquals

class TestCommandBuilder {
    @Test
    fun testLoadCommand() {
        val fileName = "/home/test/test.fun"
        assertEquals(
                LoadCommand(fileName),
                buildCommand("load $fileName")
        )
    }

    @Test
    fun testLoadCommandOnFileWithSpaces() {
        val fileName = "/home/test at home/test.fun"
        assertEquals(
                LoadCommand(fileName),
                buildCommand("load $fileName")
        )
    }

    @Test
    fun testBreakpointCommand() {
        val line = 179
        assertEquals(
                BreakpointCommand(line),
                buildCommand("breakpoint $line")
        )
    }

    @Test
    fun testConditionCommand() {
        val line = 179
        val condition = FunctionCall(
                blankPosition,
                "sum",
                listOf(
                    BinaryExpression(
                            blankPosition,
                            Number(blankPosition,1),
                            BinaryExpression.Companion.Operator.ADD,
                            Number(blankPosition,7)
                    ),
                    Number(blankPosition, 9)
                )
        )
        assertEquals(
                ConditionCommand(line, condition),
                buildCommand("condition $line $condition")
        )
    }

    @Test
    fun testListCommand() {
        assertEquals(ListCommand, buildCommand("list"))
    }

    @Test
    fun testRemoveCommand() {
        val line = 179
        assertEquals(
                RemoveCommand(line),
                buildCommand("remove $line")
        )
    }

    @Test
    fun testEvaluateCommand() {
        val expression = FunctionCall(
                blankPosition,
                "sum",
                listOf(
                        BinaryExpression(
                                blankPosition,
                                Number(blankPosition,1),
                                BinaryExpression.Companion.Operator.ADD,
                                Number(blankPosition,7)
                        ),
                        Number(blankPosition, 9)
                )
        )
        assertEquals(
                EvaluateCommand(expression),
                buildCommand("evaluateExpression $expression")
        )
    }

    @Test
    fun testRunCommand() {
        assertEquals(RunCommand, buildCommand("run"))
    }

    @Test
    fun testStopCommand() {
        assertEquals(StopCommand, buildCommand("stop"))
    }

    @Test
    fun testContinueCommand() {
        assertEquals(ContinueCommand, buildCommand("continue"))
    }
}
