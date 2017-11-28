package ru.spbau.mit

import ru.spbau.mit.interpreter.interpretFile

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Please specify a file to interpret")
    } else {
        interpretFile(args[0])
    }
}