package ru.spbau.mit.dsl

fun document(init: Document.() -> Unit): Document {
    val document = Document()
    document.init()
    return document
}

class Document : BaseEnvironment("document") {
    private val preamble = mutableListOf<Element>()
    private var documentClassCount = 0

    private fun checkDocumentClass() {
        when {
            documentClassCount == 0 -> throw NoDocumentClassException()
            documentClassCount > 1  -> throw MultipleDocumentClassException()
        }
    }

    override fun render(appendable: Appendable, indent: Int) {
        checkDocumentClass()
        for (item in preamble) {
            item.render(appendable, indent)
        }
        appendable.appendln() // blank line between preamble and document
        super.render(appendable, indent)
    }

    private fun <T : Element> addPreambleElement(element: T) {
        preamble.add(element)
    }

    fun documentClass(
            className: String,
            vararg optional: String
    ) {
        documentClassCount++
        addPreambleElement(
                Command("documentclass", arrayOf(className), arrayOf(*optional))
        )
    }

    fun usePackage(
            packageName: String,
            vararg optional: String
    ) = addPreambleElement(
            Command("usepackage", arrayOf(packageName), arrayOf(*optional))
    )

    fun title(title: String): Command =
            addElement(Command("title", arrayOf(title)), {})

    fun author(author: String): Command =
            addElement(Command("author", arrayOf(author)), {})

    fun date(date: String): Command =
            addElement(Command("date", arrayOf(date)), {})

    fun makeTitle(): Command = addElement(Command("maketitle"), {})

    fun frame(
            vararg optional: String,
            init: Environment.() -> Unit
    ): Environment = addElement(
            Environment("frame", arrayOf(*optional)),
            init
    )
}