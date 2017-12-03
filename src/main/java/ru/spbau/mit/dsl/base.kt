package ru.spbau.mit.dsl

class Command(
        private val name: String,
        private val mandatory: Array<String> = emptyArray(),
        private val optional: Array<String> = emptyArray()
) : Element(), WithOptionalArguments {
    override fun render(appendable: Appendable, indent: Int) {
        appendable.appendIndent(indent)
        appendable.append("\\$name")
        for (argument in mandatory) {
            appendable.append("{$argument}")
        }
        if (optional.isNotEmpty()) {
            appendable.append("[${optional.joinToString()}]")
        }
        appendable.appendln()
    }
}

abstract class BaseEnvironment(
        environmentName: String,
        optional: Array<String> = emptyArray()
) : ElementWithContents(), WithOptionalArguments {
    private val beginCommand = Command("begin", arrayOf(environmentName), optional)
    private val endCommand = Command("end", arrayOf(environmentName))

    override fun render(appendable: Appendable, indent: Int) {
        beginCommand.render(appendable, indent)
        super.render(appendable, indent + 1)
        endCommand.render(appendable, indent)
    }
}

class Item : ElementWithContents(), WithTextContents {
    override fun render(appendable: Appendable, indent: Int) {
        appendable.appendIndent(indent)
        appendable.appendln("\\item")
        super.render(appendable, indent + 1)
    }
}