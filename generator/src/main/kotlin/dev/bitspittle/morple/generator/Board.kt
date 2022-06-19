package dev.bitspittle.morple.generator

import dev.bitspittle.morple.common.board.TileState
import dev.bitspittle.morple.common.collections.*

class Board(val letters: List2d<Char>) {
    val tileStates: List2d<TileState> = MutableList2d(MutableList(letters.list1d.size) { TileState.ABSENT }, letters.numCols).apply {
        val finalWord = letters.lastRow().joinToString("")

        letters.forEachRowIndexed { y, chars ->
            if (y < letters.lastRowIndex) {
                val word = chars.joinToString("")
                val finalWordMutable = finalWord.toMutableList()

                for (x in word.indices) {
                    if (word[x] == finalWordMutable[x]) {
                        this[x, y] = TileState.MATCH
                        finalWordMutable[x] = '_'
                    }
                }

                for (x in word.indices) {
                    if (this[x, y] == TileState.ABSENT) {
                        val foundIndex = finalWordMutable.indexOf(word[x])
                        if (foundIndex >= 0) {
                            this[x, y] = TileState.PRESENT
                            finalWordMutable[foundIndex] = '_'
                        }
                    }
                }
            }
            else {
                for (x in 0 until letters.numCols) {
                    this[x, letters.lastRowIndex] = TileState.MATCH
                }

            }
        }
    }

    override fun toString(): String {
        return buildString {
            tileStates.forEachRow { rowStates ->
                appendLine(rowStates.joinToString("") { tile ->
                    when(tile) {
                        TileState.ABSENT -> "_"
                        TileState.PRESENT -> "?"
                        TileState.MATCH -> "!"
                    }
                })
            }

            letters.forEachRow { rowLetters ->
                appendLine(rowLetters.joinToString("") { letter -> letter.toString() })
            }
        }
    }
}
