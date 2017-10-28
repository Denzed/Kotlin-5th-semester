package ru.spbau.mit

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestSolutionOnSamples {
    private fun makeProblemAndSolve(
            n: Int,
            m: Int,
            k: Int,
            field: Array<String>): Pair<Int, Array<String>> {
        return Problem(n, m, k, field).solve()
    }

    @Test
    fun testFirstSample() {
        val (resultDrained, resultField) =
                makeProblemAndSolve(
                        5,
                        4,
                        1,
                        arrayOf("****",
                                "*..*",
                                "****",
                                "**.*",
                                "..**")
                )
        val (answerDrained, answerField) = Pair(
                1,
                arrayOf("****",
                        "*..*",
                        "****",
                        "****",
                        "..**")
        )
        assertEquals(resultDrained, answerDrained)
        assertTrue(Arrays.equals(resultField, answerField))
    }

    @Test
    fun testSecondSample() {
        val (resultDrained, resultField) =
                makeProblemAndSolve(
                        3,
                        3,
                        0,
                        arrayOf("***",
                                "*.*",
                                "***")
                )
        val (answerDrained, answerField) = Pair(
                1,
                arrayOf("***",
                        "***",
                        "***")
        )
        assertEquals(resultDrained, answerDrained)
        assertTrue(Arrays.equals(resultField, answerField))
    }
}
