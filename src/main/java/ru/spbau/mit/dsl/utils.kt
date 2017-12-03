package ru.spbau.mit.dsl

private const val INDENTATION = "    "

internal fun Appendable.appendIndent(indent: Int) =
        append(INDENTATION.repeat(indent))