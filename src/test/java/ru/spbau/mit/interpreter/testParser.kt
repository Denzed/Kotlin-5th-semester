package ru.spbau.mit.interpreter

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class TestCodeParser {
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
        assertEquals(0, outContent.size(), outContent.toString())
        outContent.reset()
        errContent.reset()
        System.setErr(null)
        System.setOut(null)
    }

    private fun parsesCode(code: String): Boolean {
        val lexer = FunLexer(CharStreams.fromString(code))
        val parser = FunParser(BufferedTokenStream(lexer))
        return parser.buildParseTree
    }

    @Test
    fun testLiteral() {
        val code = "179"
        assert(parsesCode(code))
    }

    @Test
    fun testIdentifier() {
        val code = "_s179"
        assert(parsesCode(code))
    }

    @Test
    fun testVariableDefinition() {
        val code = "var _s179 = 179"
        assert(parsesCode(code))
    }

    @Test
    fun testVariableAssignment() {
        val code = "_s179 = 179"
        assert(parsesCode(code))
    }

    @Test
    fun testBinaryOperation() {
        val code = "1 + 178"
        assert(parsesCode(code))
    }

    @Test
    fun testFunctionDefinition() {
        val code = "fun sum(a, b) {\n" +
                "   return a + b\n" +
                "}"
        assert(parsesCode(code))
    }

    @Test
    fun testFunctionCall() {
        val code = "sum(178, 1)"
        assert(parsesCode(code))
    }

    @Test
    fun testWhileCycle() {
        val code = "while (1) {}"
        assert(parsesCode(code))
    }

    @Test
    fun testIfClause() {
        val code = "if (1) {}"
        assert(parsesCode(code))
    }

    @Test
    fun testIfElseClause() {
        val code = "if (1) {} else {}"
        assert(parsesCode(code))
    }

    @Test
    fun testReturnStatement() {
        val code = "return 179"
        assert(parsesCode(code))
    }

    @Test
    fun testBracedExpression() {
        val code = "(1)"
        assert(parsesCode(code))
    }

    @Test
    fun testFirstSample() {
        val code = "var a = 10\n" +
                "var b = 20\n" +
                "if (a > b) {\n" +
                "    println(1)\n" +
                "} else {\n" +
                "    println(0)\n" +
                "}"
        assert(parsesCode(code))
    }

    @Test
    fun testSecondSample() {
        val code ="fun fib(n) {\n" +
                "    if (n <= 1) {\n" +
                "        return 1\n" +
                "    }\n" +
                "    return fib(n - 1) + fib(n - 2)\n" +
                "}\n" +
                "\n" +
                "var i = 1\n" +
                "while (i <= 5) {\n" +
                "    println(i, fib(i))\n" +
                "    i = i + 1\n" +
                "}\n"
        assert(parsesCode(code))
    }

    @Test
    fun testThirdSample() {
        val code = "fun foo(n) {\n" +
                "    fun bar(m) {\n" +
                "        return m + n\n" +
                "    }\n" +
                "\n" +
                "    return bar(1)\n" +
                "}\n" +
                "\n" +
                "println(foo(41)) // prints 42\n"
        assert(parsesCode(code))
    }
}