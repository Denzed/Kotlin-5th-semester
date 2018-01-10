package ru.spbau.mit.debugger

fun printHelp() {
    println("""
        |Welcome to Fun debugger! List of available commands:
        |   load <filename> – loads file to memory. If there is a file loaded already, its debugging will be cancelled and all the breakpoints will be removed.
        |   breakpoint <line-number> – sets a breakpoint at the given line.
        |   condition <line-number> <condition-expression> – sets a conditional breakpoint at the given line. If a line already contains a breakpoint, it will be overwritten.
        |   list – lists breakpoints with line numbers and conditions.
        |   remove <line-number> – removes breakpoint from the given line.
        |   run – runs the loaded file. Everything printed would be forwarded to standard output. If interpreted line contains a breakpoint and its condition is satisfied, the interpretation is paused. If the program was already running, an error is thrown.
        |   evaluate <expression> – evaluates an expression in the context of current line.
        |   stop – stops the interpretation.
        |   continue – continues the interpretation until the next breakpoint or the file end.
        |   exit – stops the interpretation and exits the debugger.
    """.trimMargin())
}

fun main(args: Array<String>) = runDebugging { debugger ->
    printHelp()
    while (!debugger.exited) {
        try {
            print("> ")
            val inputLine = readLine()
            if (inputLine!!.isNotEmpty()) {
                buildCommand(inputLine).evaluate(debugger)
            }
        } catch (exception: Exception) {
            println(exception.message)
        }
    }
}