package ru.spbau.mit.dsl

import java.io.OutputStream

@DslMarker
annotation class LatexMarker

@LatexMarker
abstract class Element {
    abstract internal fun render(appendable: Appendable, indent: Int)

    fun toOutputStream(outputStream: OutputStream, indent: Int = 0) {
        outputStream.writer().run {
            render(this, indent)
            close()
        }
    }

    override fun toString(): String {
        return buildString { render(this, 0) }
    }
}

class TextElement(private val text: String) : Element() {
    override fun render(appendable: Appendable, indent: Int) {
        appendable.appendIndent(indent)
        appendable.appendln(text)
    }
}

abstract class ElementWithContents : Element() {
    private val contents = mutableListOf<Element>()

    protected fun <T : Element> addElement(
            element: T,
            init: T.() -> Unit
    ): T {
        element.init()
        contents.add(element)
        return element
    }

    private fun renderElement(
            appendable: Appendable,
            indent: Int,
            element: Element
    ) {
        element.render(appendable, indent)
    }

    override fun render(appendable: Appendable, indent: Int) {
        for (element in contents) {
            renderElement(appendable, indent, element)
        }
    }

    interface WithTextContents {
        operator fun String.unaryPlus() {
            if (this@WithTextContents is ElementWithContents) {
                for (line in this.trimMargin().split('\n')) {
                    addElement(TextElement(line), {})
                }
            }
        }
    }
}

interface WithTextContents : ElementWithContents.WithTextContents

interface WithOptionalArguments {
    // for usage as named optional argument in vararg
    infix fun String.to(value: String): String = "$this=$value"
}