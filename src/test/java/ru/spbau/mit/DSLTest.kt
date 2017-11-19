package ru.spbau.mit

import org.junit.Test
import ru.spbau.mit.dsl.MultipleDocumentClassException
import ru.spbau.mit.dsl.NoDocumentClassException
import ru.spbau.mit.dsl.document
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

class DSLTest {
    @Test(expected = NoDocumentClassException::class)
    fun testNoDocumentClass() {
        document {

        }.toString()
    }

    @Test(expected = MultipleDocumentClassException::class)
    fun testMultipleDocumentClass() {
        document {
            documentClass("1")
            documentClass("2")
        }.toString()
    }

    @Test
    fun testOptionalParameters() {
        val generated = document {
            documentClass("school", "179", "okay" to "true")
        }.toString().trim()

        val expected = """
            |\documentclass{school}[179, okay=true]
            |
            |\begin{document}
            |\end{document}
             """.trimMargin()

        assertEquals(expected, generated)
    }

    @Test
    fun testUsePackage() {
        val generated = document {
            documentClass("beamer")

            usePackage("babel", "russian")
        }.toString().trim()

        val expected = """
            |\documentclass{beamer}
            |\usepackage{babel}[russian]
            |
            |\begin{document}
            |\end{document}
            """.trimMargin()

        assertEquals(expected, generated)
    }

    @Test
    fun testFrame() {
        val generated = document {
            documentClass("beamer")
            usePackage("babel", "russian")

            title("sample")
            author("me")
            date("today")
            makeTitle()

            frame("plain") {
            }
        }.toString().trim()

        val expected = """
            |\documentclass{beamer}
            |\usepackage{babel}[russian]
            |
            |\begin{document}
            |    \title{sample}
            |    \author{me}
            |    \date{today}
            |    \maketitle
            |    \begin{frame}[plain]
            |    \end{frame}
            |\end{document}
            """.trimMargin()

        assertEquals(expected, generated)
    }

    @Test
    fun testCustomCommand() {
        val generated = document {
            documentClass("beamer")
            usePackage("babel", "russian")

            frame("plain") {
                customCommand("frametitle", arrayOf("sample"))
            }
        }.toString().trim()

        val expected = """
            |\documentclass{beamer}
            |\usepackage{babel}[russian]
            |
            |\begin{document}
            |    \begin{frame}[plain]
            |        \frametitle{sample}
            |    \end{frame}
            |\end{document}
            """.trimMargin()

        assertEquals(expected, generated)
    }

    @Test
    fun testCustomEnvironment() {
        val generated = document {
            documentClass("beamer")
            usePackage("babel", "russian")

            frame("plain") {
                customEnvironment("pyglist", "language" to "kotlin") {
                    +"""
                        |val a = 1
                        |
                    """
                }
            }
        }.toString().trim()

        val expected = ("""
            |\documentclass{beamer}
            |\usepackage{babel}[russian]
            |
            |\begin{document}
            |    \begin{frame}[plain]
            |        \begin{pyglist}[language=kotlin]
            |            val a = 1
            |            """ + """
            |        \end{pyglist}
            |    \end{frame}
            |\end{document}
            """).trimMargin()

        assertEquals(expected, generated)
    }

    @Test
    fun testLists() {
        val generated = document {
            documentClass("beamer")
            usePackage("babel", "russian")

            frame("plain") {
                itemize {
                    for (i in 1..3) {
                        item { +"item #$i" }
                    }
                }

                enumerate("I") {
                    for (i in 1..3) {
                        item { +"item #$i" }
                    }
                }
            }
        }.toString().trim()

        val expected = """
            |\documentclass{beamer}
            |\usepackage{babel}[russian]
            |
            |\begin{document}
            |    \begin{frame}[plain]
            |        \begin{itemize}
            |            \item
            |                item #1
            |            \item
            |                item #2
            |            \item
            |                item #3
            |        \end{itemize}
            |        \begin{enumerate}[I]
            |            \item
            |                item #1
            |            \item
            |                item #2
            |            \item
            |                item #3
            |        \end{enumerate}
            |    \end{frame}
            |\end{document}
            """.trimMargin()

        assertEquals(expected, generated)
    }

    @Test
    fun testMath() {
        val generated = document {
            documentClass("beamer")

            frame {
                math { +"1 + 1" }
            }
        }.toString().trim()

        val expected = """
            |\documentclass{beamer}
            |
            |\begin{document}
            |    \begin{frame}
            |        \begin{math}
            |            1 + 1
            |        \end{math}
            |    \end{frame}
            |\end{document}
            """.trimMargin()

        assertEquals(expected, generated)
    }

    @Test
    fun testAlignment() {
        val generated = document {
            documentClass("beamer")

            frame {
                align { +"1 + 1" }
                flushleft { +"left" }
                flushright { +"right" }
                center { +"center" }
            }
        }.toString().trim()

        val expected = """
            |\documentclass{beamer}
            |
            |\begin{document}
            |    \begin{frame}
            |        \begin{align}
            |            1 + 1
            |        \end{align}
            |        \begin{flushleft}
            |            left
            |        \end{flushleft}
            |        \begin{flushright}
            |            right
            |        \end{flushright}
            |        \begin{center}
            |            center
            |        \end{center}
            |    \end{frame}
            |\end{document}
            """.trimMargin()

        assertEquals(expected, generated)
    }

    @Test
    fun testStreamOutput() {
        val outputStream = ByteArrayOutputStream()
        document {
            documentClass("beamer")
            usePackage("babel", "russian")

            frame("plain") {
            }
        }.toOutputStream(outputStream)

        val generated = outputStream.toString().trim()

        val expected = """
            |\documentclass{beamer}
            |\usepackage{babel}[russian]
            |
            |\begin{document}
            |    \begin{frame}[plain]
            |    \end{frame}
            |\end{document}
            """.trimMargin()

        assertEquals(expected, generated)
    }
}