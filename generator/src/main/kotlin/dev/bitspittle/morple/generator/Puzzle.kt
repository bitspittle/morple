package dev.bitspittle.morple.generator

import dev.bitspittle.morple.common.board.TileState
import dev.bitspittle.morple.common.collections.*

fun Board.generatePuzzle(words: Set<String>) = Puzzle(this, words)

class Puzzle(val board: Board, val words: Set<String>) {
    private val letters = board.letters

    private val wordsByLetter: Array<Map<Char, Set<String>>> = Array<MutableMap<Char, MutableSet<String>>>(5) { mutableMapOf() }.apply {
        for (i in 0..4) {
            for (word in words) {
                val c = word[i]
                this[i]
                    .computeIfAbsent(c) { mutableSetOf() }
                    .add(word)
            }
        }
    } as Array<Map<Char, Set<String>>>

    private fun String.numOnes() = this.count { c -> c == '1' }

    private fun String.matches(finalWord: String, tileStates: List<TileState>): Boolean {
        val finalWordMutable = finalWord.toMutableList()
        for (i in this.indices) {
            val c = this[i]
            if (tileStates[i] == TileState.MATCH) {
                if (finalWordMutable[i] != c) return false
                finalWordMutable[i] = '_'
            }
        }

        for (i in this.indices) {
            val c = this[i]
            val state = tileStates[i]
            if (state == TileState.ABSENT) {
                if (finalWordMutable.contains(c)) return false
            }
            else if (state == TileState.PRESENT) {
                val foundIndex = finalWordMutable.indexOf(c)
                if (foundIndex < 0) return false
                finalWordMutable[foundIndex] = '_'
            }
        }

        return true
    }

    private fun pareDown(word: String, filter: (String) -> Boolean = { true }): String {
        val choices = mutableListOf<String>()
        for (num in 1 until 32) {
            val binary = num.toString(2).padStart(5, '0')
            val allMatches = mutableSetOf<String>()
            binary.forEachIndexed { i, c ->
                val currLetter = word[i]
                if (c == '1') {
                    val matches = wordsByLetter[i][currLetter]!!
                        .filter(filter)
                    if (allMatches.isEmpty()) {
                        allMatches.addAll(matches)
                    } else {
                        allMatches.removeIf { !matches.contains(it) }
                    }
                }
            }
            if (allMatches.size == 1) {
                choices.add(binary)
            }
        }

        val minSize = try {
            choices.minOf { it.numOnes() }
        } catch (ex: NoSuchElementException) {
            println("Unexpected exception caused by: $word")
            throw ex
        }

        return choices
            .filter { it.numOnes() == minSize }
            .random()
    }

    val visible: List2d<Boolean> = letters.map { false }.toMutableList2d().apply {
        val finalWord = letters.lastRow().joinToString("")
        for (y in 0 until lastRowIndex) {
            val rowWord = letters.getRow(y).joinToString("")
            val tileStates = board.tileStates.getRow(y)
            val binary = pareDown(rowWord) // { word -> word.matches(finalWord, tileStates) }
            for (x in binary.indices) {
                if (binary[x] == '1') {
                    this[x, y] = true
                }
            }
        }

        val allWords = words.toMutableSet()
        val possibleMatches = mutableSetOf<String>()
        for (possibleMatch in allWords) {
            var accepted = true
            for (y in 0 until lastRowIndex) {
                val rowWord = letters.getRow(y).joinToString("")
                val tileStates = board.tileStates.getRow(y)
                if (!rowWord.matches(possibleMatch, tileStates)) {
                    accepted = false
                    break
                }
            }
            if (accepted) {
                possibleMatches.add(possibleMatch)
            }
        }

        val binary = pareDown(finalWord) { word -> possibleMatches.contains(word) }
        for (x in binary.indices) {
            if (binary[x] == '1') {
                this[x, lastRowIndex] = true
            }
        }
    }

    override fun toString(): String {
        val letters = letters
        return buildString {
            for (y in 0 until letters.numRows) {
                for (x in 0 until letters.numCols) {
                    append(if (visible[x, y]) letters[x, y] else '_')
                }
                appendLine()
            }

            appendLine()
            for (y in 0 until letters.numRows) {
                for (x in 0 until letters.numCols) {
                    if (visible[x, y]) {
                        append(letters[x, y].uppercaseChar())
                    }
                    append(when (board.tileStates[x, y]) {
                        TileState.ABSENT -> '-'
                        TileState.PRESENT -> '*'
                        TileState.MATCH -> '+'
                    })
                }
            }
        }
    }
}
