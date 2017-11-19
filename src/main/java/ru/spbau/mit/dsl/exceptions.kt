package ru.spbau.mit.dsl

abstract class LatexException(message: String) : Exception(message)

class NoDocumentClassException : LatexException("no document class specified")

class MultipleDocumentClassException : LatexException("multiple document class specified")