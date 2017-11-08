package ru.spbau.mit.interpreter
import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.spbau.mit.interpreter.ast.*
import ru.spbau.mit.interpreter.ast.Number
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals


class TestASTBuilder {
    private val errContent = ByteArrayOutputStream()

    @Before
    fun setUpStreams() {
        System.setErr(PrintStream(errContent))
    }

    @After
    fun cleanUpStreams() {
        assertEquals(0, errContent.size(), errContent.toString())
        errContent.reset()
        System.setErr(null)
    }

    private fun parseToAST(code: String): ASTNode {
        val lexer = FunLexer(CharStreams.fromString(code))
        val parser = FunParser(BufferedTokenStream(lexer))
        return ASTBuilder().visit(parser.block())
    }

    @Test
    fun testLiteral() {
        val code = "179"
        val expectedTree =
                Block(blankPosition,
                        mutableListOf(Number(blankPosition, 179)))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testIdentifier() {
        val code = "_s179"
        val expectedTree =
                Block(blankPosition,
                mutableListOf(Identifier(blankPosition, "_s179")))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testVariableDefinition() {
        val code = "var _s179 = 179"
        val expectedTree =
                Block(blankPosition,
                        mutableListOf(VariableDefinition(
                                blankPosition,
                                "_s179",
                                Number(blankPosition, 179))))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testVariableAssignment() {
        val code = "_s179 = 179"
        val expectedTree =
                Block(blankPosition,
                        mutableListOf(VariableAssignment(
                                blankPosition,
                                "_s179",
                                Number(blankPosition, 179))))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testBinaryOperation() {
        val code = "1 + 178"

        val expectedTree =
                Block(blankPosition,
                        mutableListOf(BinaryExpression(
                                blankPosition,
                                Number(blankPosition, 1),
                                Pair({ _, _ -> -1 }, "+"),
                                Number(blankPosition, 178))))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testFunctionDefinition() {
        val code = "fun sum(a, b) {\n" +
                "   return a + b\n" +
                "}"

        val expectedFunctionBody =
                Block(blankPosition,
                        mutableListOf(ReturnStatement(blankPosition,
                                BinaryExpression(
                                    blankPosition,
                                    Identifier(blankPosition, "a"),
                                    Pair({ _, _ -> -1 }, "+"),
                                    Identifier(blankPosition, "b")))))

        val expectedTree =
                Block(blankPosition,
                        mutableListOf(FunctionDefinition(
                                blankPosition,
                                "sum",
                                mutableListOf("a", "b"),
                                expectedFunctionBody)))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testFunctionCall() {
        val code = "sum(178, 1)"

        val expectedTree =
                Block(blankPosition,
                        mutableListOf(FunctionCall(
                                blankPosition,
                                "sum",
                                mutableListOf(
                                        Number(blankPosition, 178),
                                        Number(blankPosition, 1)
                                ))))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testWhileCycle() {
        val code = "while (1) {}"

        val expectedTree =
                Block(blankPosition,
                        mutableListOf(WhileCycle(
                                blankPosition,
                                Number(blankPosition,1),
                                BracedBlock(blankPosition,
                                        Block(blankPosition, mutableListOf())))))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testIfClause() {
        val code = "if (1) {}"

        val expectedTree =
                Block(blankPosition,
                        mutableListOf(IfClause(
                                blankPosition,
                                Number(blankPosition,1),
                                BracedBlock(blankPosition,
                                        Block(blankPosition, mutableListOf())),
                                null)))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testIfElseClause() {
        val code = "if (1) {} else {}"

        val expectedTree =
                Block(blankPosition,
                        mutableListOf(IfClause(
                                blankPosition,
                                Number(blankPosition,1),
                                BracedBlock(blankPosition,
                                        Block(blankPosition, mutableListOf())),
                                BracedBlock(blankPosition,
                                        Block(blankPosition, mutableListOf())))))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testReturnStatement() {
        val code = "return 179"

        val expectedTree =
                Block(blankPosition,
                        mutableListOf(ReturnStatement(
                                blankPosition,
                                Number(blankPosition,179))))

        assertEquals(expectedTree, parseToAST(code))
    }

    @Test
    fun testBracedExpression() {
        val code = "(1)"

        val expectedTree =
                Block(blankPosition,
                        mutableListOf(BracedExpression(
                                blankPosition,
                                Number(blankPosition,1))))

        assertEquals(expectedTree, parseToAST(code))
    }

    private companion object {
        val blankPosition = Pair(-1, -1)
    }
}