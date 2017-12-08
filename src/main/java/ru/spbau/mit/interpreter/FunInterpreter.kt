package ru.spbau.mit.interpreter

import kotlinx.coroutines.experimental.runBlocking
import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.ast.ASTBuilder
import ru.spbau.mit.ast.nodes.ASTNode
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser

fun interpretFile(fileName: String): Int? = runBlocking {
    interpretAST(buildASTFromFile(fileName))
}

fun interpretCode(code: String): Int? = runBlocking {
    interpretAST(buildASTFromCode(code))
}

private suspend fun interpretAST(ast: ASTNode): Int? =
        ast.accept(InterpretingASTVisitor())

fun buildASTFromFile(fileName: String): ASTNode =
        buildASTFromCharStream(CharStreams.fromFileName(fileName))

fun buildASTFromCode(code: String): ASTNode =
        buildASTFromCharStream(CharStreams.fromString(code))

private fun buildASTFromCharStream(charStream: CharStream): ASTNode {
    val lexer = FunLexer(charStream)
    val parser = FunParser(BufferedTokenStream(lexer))
    return parser.file().accept(ASTBuilder())
}