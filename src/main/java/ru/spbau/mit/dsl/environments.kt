package ru.spbau.mit.dsl

class Enumeration(
        enumerationName: String,
        optional: Array<String> = emptyArray()
) : BaseEnvironment(enumerationName, optional) {
    fun item(init: Item.() -> Unit): Item = addElement(Item(), init)
}

class TextOnlyEnvironment(
        environmentName: String,
        optional: Array<String> = emptyArray()
) : BaseEnvironment(environmentName, optional), WithTextContents

class Environment(
        environmentName: String,
        optional: Array<String> = emptyArray()
) : BaseEnvironment(environmentName, optional), WithTextContents {
    // basic list enumerations
    fun itemize(
            vararg optional: String,
            init: Enumeration.() -> Unit
    ): Enumeration = addElement(
            Enumeration("itemize", arrayOf(*optional)),
            init
    )

    fun enumerate(
            vararg optional: String,
            init: Enumeration.() -> Unit
    ): Enumeration = addElement(
            Enumeration("enumerate", arrayOf(*optional)),
            init
    )

    // math mode accepts only text and math commands -> accepting text should be enough
    fun math(init: TextOnlyEnvironment.() -> Unit): TextOnlyEnvironment = addElement(
            TextOnlyEnvironment("math"),
            init
    )

    // align environment from amsmath package works only in math mode
    fun align(init: TextOnlyEnvironment.() -> Unit): TextOnlyEnvironment = addElement(
            TextOnlyEnvironment("align"),
            init
    )

    // here we define text alignment from ragged2e package -- works only for text
    // (i.e. itemize would still be unaligned)
    fun flushleft(init: Environment.() -> Unit): Environment = addElement(
            Environment("flushleft"),
            init
    )

    fun flushright(init: Environment.() -> Unit): Environment = addElement(
            Environment("flushright"),
            init
    )

    fun center(init: Environment.() -> Unit): Environment = addElement(
            Environment("center"),
            init
    )

    fun customCommand(
            name: String,
            mandatory: Array<String> = emptyArray(),
            vararg optional: String
    ) = addElement(Command(name, mandatory, arrayOf(*optional)), {})

    fun customEnvironment(
            name: String,
            vararg optional: String,
            init: Environment.() -> Unit
    ) = addElement(
            Environment(name, arrayOf(*optional)),
            init
    )
}