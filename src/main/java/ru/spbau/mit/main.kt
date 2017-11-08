package ru.spbau.mit

import ru.spbau.mit.interpreter.FunInterpreter

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Please specify a file to interpret")
    } else {
        FunInterpreter.interpretFile(args[0])
    }
}