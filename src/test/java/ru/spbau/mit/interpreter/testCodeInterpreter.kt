package ru.spbau.mit.interpreter
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class TestCodeInterpreter {
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
    fun testLiteral() {
        val code = "println(179)"
        interpretCode(code)
        assertPrints("179\n")
    }

    @Test(expected = VariableUndefinedException::class)
    fun testUndefinedIdentifier() {
        interpretCode("println(_s179)")
    }

    @Test
    fun testIdentifier() {
        interpretCode(
                "var _s179 = 179\n" +
                "println(_s179)"
        )
        assertPrints("179\n")
    }

    @Test
    fun testVariableDefinition() {
        val code = "var _s179 = 179"
        interpretCode(code)
        assertPrints("")
    }

    @Test
    fun testVariableAssignment() {
        val code = "var _s179 = 0\n" +
                "_s179 = 179"
        interpretCode(code)
        assertPrints("")
    }

    @Test
    fun testBinaryOperation() {
        val code = "println(1 + 178)"
        interpretCode(code)
        assertPrints("179\n")
    }

    @Test
    fun testFunctionDefinition() {
        val code = "fun sum(a, b) {\n" +
                "   return a + b\n" +
                "}"
        interpretCode(code)
        assertPrints("")
    }

    @Test
    fun testFunctionCall() {
        val code = "fun sum(a, b) {\n" +
                "   return a + b\n" +
                "}\n" +
                "println(sum(178, 1))"
        interpretCode(code)
        assertPrints("179\n")
    }

    @Test
    fun testEmptyPrintlnCall() {
        val code = "println()"
        interpretCode(code)
        assertPrints("\n")
    }

    @Test
    fun testWhileCycle() {
        val code = "var i = 1\n" +
                "while (i + 1 > 0) {" +
                "println(i)" +
                "i = i - 1" +
                "}"
        interpretCode(code)
        assertPrints(
                "1\n" +
                "0\n"
        )
    }

    @Test
    fun testIfTrueClause() {
        val code = "if (1) {\n" +
                "println(1)\n" +
                "}"
        interpretCode(code)
        assertPrints("1\n")
    }

    @Test
    fun testIfFalseClause() {
        val code = "if (0) {\n" +
                "println(1)\n" +
                "}"
        interpretCode(code)
        assertPrints("")
    }

    @Test(expected = InvalidReturnStatementException::class)
    fun testReturnStatement() {
        val code = "return 179"
        interpretCode(code)
        assertPrints("")
    }

    @Test
    fun testParenthesizedExpression() {
        val code = "println((1))"
        interpretCode(code)
        assertPrints("1\n")
    }

    @Test
    fun testFirstSample() {
        interpretCode(
                "var a = 10\n" +
                "var b = 20\n" +
                "if (a > b) {\n" +
                "    println(1)\n" +
                "} else {\n" +
                "    println(0)\n" +
                "}"
        )
        assertPrints("0\n")
    }

    @Test
    fun testSecondSample() {
        interpretCode(
                "fun fib(n) {\n" +
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
        )
        assertPrints(
                "1 1\n" +
                "2 2\n" +
                "3 3\n" +
                "4 5\n" +
                "5 8\n"
        )
    }

    @Test
    fun testThirdSample() {
        interpretCode(
                "fun foo(n) {\n" +
                "    fun bar(m) {\n" +
                "        return m + n\n" +
                "    }\n" +
                "\n" +
                "    return bar(1)\n" +
                "}\n" +
                "\n" +
                "println(foo(41)) // prints 42\n"
        )
        assertPrints("42\n")
    }
}