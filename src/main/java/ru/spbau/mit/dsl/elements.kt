package ru.spbau.mit.dsl

import java.io.OutputStream

@DslMarker
annotation class LatexMarker

@LatexMarker
abstract class Element {
    abstract fun render(builder: StringBuilder, indent: Int)
    abstract fun toOutputStream(outputStream: OutputStream, indent: Int = 0)

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        render(stringBuilder, 0)
        return stringBuilder.toString()
    }
}

class TextElement(private val text: String) : Element() {
    override fun toOutputStream(outputStream: OutputStream, indent: Int) {
        outputStream.writer().run {
            appendIndent(indent)
            append(text)
            close()
        }
    }

    override fun render(builder: StringBuilder, indent: Int) {
        builder.appendIndent(indent)
        builder.appendln(text)
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, 0)
        return builder.toString()
    }
}

abstract class ElementWithContents : Element() {
    private val contents = mutableListOf<Element>()

    open fun <T : Element> addElement(
            element: T,
            init: T.() -> Unit
    ): T {
        element.init()
        contents.add(element)
        return element
    }

    protected open fun renderElement(
            builder: StringBuilder,
            indent: Int,
            element: Element
    ) {
        element.render(builder, indent)
    }

    protected open fun outputElement(
            outputStream: OutputStream,
            indent: Int,
            element: Element
    ) {
        element.toOutputStream(outputStream, indent)
    }

    override fun render(builder: StringBuilder, indent: Int) {
        for (element in contents) {
            renderElement(builder, indent, element)
        }
    }

    override fun toOutputStream(outputStream: OutputStream, indent: Int) {
        for (element in contents) {
            outputElement(outputStream, indent, element)
        }
    }
}

interface WithTextContents {
    // add text elements
    fun <T : Element> addElement(
            element: T,
            init: T.() -> Unit
    ): T

    operator fun String.unaryPlus() {
        for (line in this.trimMargin().split('\n')) {
            addElement(TextElement(line), {})
        }
    }
}

interface WithOptionalArguments {
    // for usage as named optional argument in vararg
    infix fun String.to(value: String): String = "$this=$value"
}