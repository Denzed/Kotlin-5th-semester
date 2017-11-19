package ru.spbau.mit.dsl

import java.io.OutputStreamWriter

private val INDENTATION = "    "

internal fun StringBuilder.appendIndent(indent: Int) =
        append(INDENTATION.repeat(indent))

internal fun OutputStreamWriter.appendIndent(indent: Int) =
        append(INDENTATION.repeat(indent))