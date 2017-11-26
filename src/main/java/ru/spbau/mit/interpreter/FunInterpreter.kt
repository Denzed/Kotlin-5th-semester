package ru.spbau.mit.interpreter

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.interpreter.ast.ASTBuilder
import ru.spbau.mit.interpreter.ast.ASTVisitor
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser

fun interpretFile(fileName: String) {
    val lexer = FunLexer(CharStreams.fromFileName(fileName))
    val parser = FunParser(BufferedTokenStream(lexer))
    val ast = ASTBuilder.visitFile(parser.file())

    ASTVisitor().visit(ast)
}

fun interpretCode(code: String): Int? {
    val lexer = FunLexer(CharStreams.fromString(code))
    val parser = FunParser(BufferedTokenStream(lexer))
    val ast = ASTBuilder.visit(parser.block())

    return ASTVisitor().visit(ast)
}
