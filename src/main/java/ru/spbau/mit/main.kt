package ru.spbau.mit

import java.util.*

data class Move(val dy: Int, val dx: Int) {

    companion object {
        private val north = Move(-1, 0)
        private val east  = Move(0, 1)
        private val south = Move(1, 0)
        private val west  = Move(0, -1)

        val directions = listOf(north, east, south, west)
    }
}

data class Lake(val size: Int, val y: Int, val x: Int)

class Problem(
        private val n: Int,
        private val m: Int,
        private val k: Int,
        private val field: Array<String>) {

    private val used: MutableList<MutableList<Boolean>> =
            Array(n) { BooleanArray(m) { false }.toMutableList() }.toMutableList()

    private fun resetUsed() {
        used.clear()
        used.addAll(Array(n) { BooleanArray(m) { false }.toMutableList() })
    }

    private fun isNewWaterTile(y: Int, x: Int): Boolean =
            (y in 0..(n - 1) &&
             x in 0..(m - 1) &&
             !used[y][x] &&
             field[y][x] == '.')

    private fun traverseLake(y: Int, x: Int): Int? {
        used[y][x] = true
        var result = if (y in 1..(n - 2) && x in 1..(m - 2)) 1 else null
        for (move in Move.directions) {
            val newY = y + move.dy
            val newX = x + move.dx
            if (isNewWaterTile(newY, newX)) {
                val tempResult = traverseLake(newY, newX)
                if (result != null && tempResult != null) {
                    result += tempResult
                } else {
                    result = null
                }
            }
        }
        return result
    }

    private fun findLakes(): List<Lake> {
        val lakes: MutableList<Lake> = mutableListOf()
        for (y in 0 until n) {
            for (x in 0 until m) {
                if (isNewWaterTile(y, x)) {
                    val lakeTileCount = traverseLake(y, x)
                    if (lakeTileCount != null) {
                        lakes.add(Lake(lakeTileCount, y, x))
                    }
                }
            }
        }
        return lakes.sortedBy( Lake::size )
    }

    fun solve(): Pair<Int, Array<String>> {
        val lakes = findLakes()
        resetUsed()
        val lakesToDrain = lakes.subList(0, lakes.size - k)
        val drainedTiles = lakesToDrain.sumBy(Lake::size)
        lakesToDrain.forEach { lake -> traverseLake(lake.y, lake.x) }
        val resultField = Array(n, { y ->
            val resultLine = StringBuilder(m)
            for (x in 0 until m) {
                resultLine.append(if (used[y][x]) '*' else field[y][x])
            }
            resultLine.toString()
        })
        return Pair(drainedTiles, resultField)
    }
}

fun main(args: Array<String>) = Scanner(System.`in`).run {
    val n = nextInt()
    val m = nextInt()
    val k = nextInt()
    val field = Array(n, { _ -> next() })
    val (drainedTileCount, resultField) = Problem(n, m, k, field).solve()
    println(drainedTileCount)
    resultField.forEach { it -> println(it) }
}