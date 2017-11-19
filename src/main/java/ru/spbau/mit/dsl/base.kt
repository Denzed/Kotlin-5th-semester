package ru.spbau.mit.dsl

import java.io.OutputStream

class Command(
        private val name: String,
        private val mandatory: Array<String> = emptyArray(),
        private val optional: Array<String> = emptyArray()
) : Element(), WithOptionalArguments {
    override fun render(builder: StringBuilder, indent: Int) {
        builder.appendIndent(indent)
        builder.append("\\$name")
        for (argument in mandatory) {
            builder.append("{$argument}")
        }
        if (optional.isNotEmpty()) {
            builder.append("[${optional.joinToString()}]")
        }
        builder.appendln()
    }

    override fun toOutputStream(outputStream: OutputStream, indent: Int) {
        outputStream.writer().run {
            appendIndent(indent)
            append("\\$name")
            for (argument in mandatory) {
                append("{$argument}")
            }
            if (optional.isNotEmpty()) {
                append("[${optional.joinToString()}]")
            }
            appendln()
            close()
        }
    }
}

abstract class BaseEnvironment(
        environmentName: String,
        optional: Array<String> = emptyArray()
) : ElementWithContents(), WithOptionalArguments {
    private val beginCommand = Command("begin", arrayOf(environmentName), optional)
    private val endCommand = Command("end", arrayOf(environmentName))

    override fun render(builder: StringBuilder, indent: Int) {
        beginCommand.render(builder, indent)
        super.render(builder, indent + 1)
        endCommand.render(builder, indent)
    }

    override fun toOutputStream(outputStream: OutputStream, indent: Int) {
        beginCommand.toOutputStream(outputStream, indent)
        super.toOutputStream(outputStream, indent + 1)
        endCommand.toOutputStream(outputStream, indent)
    }
}

class Item : ElementWithContents(), WithTextContents {
    override fun render(builder: StringBuilder, indent: Int) {
        builder.appendIndent(indent)
        builder.appendln("\\item")
        super.render(builder, indent + 1)
    }

    override fun toOutputStream(outputStream: OutputStream, indent: Int) {
        outputStream.writer().run {
            appendIndent(indent)
            append("\\item")
            close()
        }
        super.toOutputStream(outputStream, indent + 1)
    }
}