package dev.bitspittle.morple.data

import dev.bitspittle.morple.common.board.Board
import dev.bitspittle.morple.common.board.TileState

sealed class GameError(val message: String) {
    sealed class Tile(val x: Int, val y: Int, message: String) : GameError(message)
    sealed class LetterTile(val letter: Char, x: Int, y: Int, message: String) : Tile(x, y, message)
    sealed class Row(val y: Int, message: String) : GameError(message)

    class EmptyTile(x: Int, y: Int) : Tile(x, y, "A letter is missing.")
    class InvalidWord(invalidWord: String, y: Int) : Row(y, "\"$invalidWord\" is not an accepted word.")
    class RepeatedWord(repeatedWord: String, y: Int) : Row(y, "\"$repeatedWord\" was already used earlier.")
    class NotAbsent(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' is present in the final word.")
    class NotPresent(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' is not present in the final word (or it is but has already been matched by a different tile).")
    class InvalidPresent(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' matches the final solution when it should not.")
    class InconsistentMatch(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' does not agree with other matches in the same column.")
    class NotMatch(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' does not match the final solution")
}

private const val INVALID_CHAR = '?'

class Validator {
    fun validate(board: Board, words: Set<String>): List<GameError> {
        val errors = mutableListOf<GameError>()

        val finalWord = (0 until Board.NUM_COLS)
            .map { x -> board.letters[x, board.numRows - 1] ?: INVALID_CHAR }
            .joinToString("")

        // Empty tiles
        for (y in 0 until board.numRows) {
            for (x in 0 until Board.NUM_COLS) {
                if (board.letters[x, y] == null) {
                    errors.add(GameError.EmptyTile(x, y))
                }
            }
        }

        // Invalid words
        for (y in 0 until board.numRows) {
            val word = (0 until Board.NUM_COLS).mapNotNull { x -> board.letters[x, y] }.joinToString("")
            if (word.length == Board.NUM_COLS && !words.contains(word)) {
                errors.add(GameError.InvalidWord(word, y))
            }
        }

        // Repeated words
        mutableSetOf<String>().let { usedWords ->
            for (y in 0 until board.numRows) {
                val word = (0 until Board.NUM_COLS).mapNotNull { x -> board.letters[x, y] }.joinToString("")
                if (word.length == Board.NUM_COLS && !usedWords.add(word)) {
                    errors.add(GameError.RepeatedWord(word, y))
                }
            }
        }

        // Repeated absent characters
        mutableMapOf<Char, Int>().let { usedAbsentChars ->
            // No need to check last row -- it is, by definition, all matches
            for (y in 0 until board.numRows - 1) {
                for (x in 0 until Board.NUM_COLS) {
                    val letter = board.letters[x, y] ?: continue
                    if (board.tiles[x, y] == TileState.ABSENT) {
                        usedAbsentChars[letter] = usedAbsentChars.getOrPut(letter) { 0 } + 1
                    }
                }
            }
        }

        // Inconsistent match
        // Skip checking last row; that's a different check that will be done later
        for (y in 0 until board.numRows - 1) {
            for (x in 0 until Board.NUM_COLS) {
                val letter = board.letters[x, y] ?: continue
                if (board.tiles[x, y] == TileState.MATCH) {
                    for (y2 in 0 until board.numRows - 1) {
                        if (y2 != y) {
                            val letter2 = board.letters[x, y2] ?: break
                            if (board.tiles[x, y2] == TileState.MATCH && letter != letter2) {
                                errors.add(GameError.InconsistentMatch(letter, x, y))
                            }
                        }
                    }
                }
            }
        }

        // Not match / Not present / Not absent (only check if the last row is started)
        if (finalWord.any { it.isLetter() }) {
            // Note: Skip last row - no need to compare against itself
            for (y in 0 until board.numRows - 1) {
                val mutableFinalWord = finalWord.toMutableList()

                // First, remove characters already matched on this line, they shouldn't be counted against when checking
                // remaining present characters
                for (x in 0 until Board.NUM_COLS) {
                    val letter = board.letters[x, y] ?: continue
                    if (board.tiles[x, y] == TileState.MATCH) {
                        if (letter != finalWord[x]) {
                            errors.add(GameError.NotMatch(letter, x, y))
                        } else {
                            // Remove the letter, so it won't be reused by following checks
                            mutableFinalWord[x] = INVALID_CHAR
                        }
                    }
                }

                // Call out present tiles that match the final row.
                for (x in 0 until Board.NUM_COLS) {
                    val letter = board.letters[x, y] ?: continue
                    if (board.tiles[x, y] == TileState.PRESENT) {
                        if (letter == finalWord[x]) {
                            errors.add(GameError.InvalidPresent(letter, x, y))
                        }
                    }
                }

                // Now that we've removed matched characters from consideration, let's check all remaining present tiles
                // in order from left to right. This is done so we cannot have an absent tile show up before a
                // present tile with the same letter value - in that case, the first letter should be present and the
                // last should be absent.
                for (x in 0 until Board.NUM_COLS) {
                    val letter = board.letters[x, y] ?: continue
                    if (board.tiles[x, y] == TileState.PRESENT) {
                        // Skip over errors reported by the previous "invalid present" section
                        if (letter != finalWord[x]) {
                            val foundIndex = mutableFinalWord.indexOf(letter)
                            if (foundIndex >= 0) {
                                mutableFinalWord[foundIndex] = INVALID_CHAR
                            } else {
                                errors.add(GameError.NotPresent(letter, x, y))
                            }
                        }
                    } else if (board.tiles[x, y] == TileState.ABSENT) {
                        val foundIndex = mutableFinalWord.indexOf(letter)
                        if (foundIndex >= 0) {
                            errors.add(GameError.NotAbsent(letter, x, y))
                        }
                    }
                }
            }
        }

        return errors
    }
}