package ru.spbau.mit.interpreter

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.interpreter.ast.ASTBuilder
import ru.spbau.mit.interpreter.ast.InterpretingASTVisitor
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser

fun interpretFile(fileName: String): Int? =
        interpretCharStream(CharStreams.fromFileName(fileName))

fun interpretCode(code: String): Int? =
        interpretCharStream(CharStreams.fromString(code))

private fun interpretCharStream(charStream: CharStream): Int? {
    val lexer = FunLexer(charStream)
    val parser = FunParser(BufferedTokenStream(lexer))
    val ast = ASTBuilder.visit(parser.file())
    return InterpretingASTVisitor().visit(ast)
}
