package dev.bitspittle.morple.data

sealed class GameError(val message: String) {
    sealed class Tile(val x: Int, val y: Int, message: String) : GameError(message)
    sealed class LetterTile(val letter: Char, x: Int, y: Int, message: String) : Tile(x, y, message)
    sealed class Row(val y: Int, message: String) : GameError(message)

    class EmptyTile(x: Int, y: Int) : Tile(x, y, "A letter is missing.")
    class InvalidWord(invalidWord: String, y: Int) : Row(y, "\"$invalidWord\" is not an accepted word.")
    class RepeatedWord(repeatedWord: String, y: Int) : Row(y, "\"$repeatedWord\" was already used earlier.")
    class NotAbsent(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' is present in the final word")
    class RepeatedAbsent(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' is a repeat, and you have more absent repeats than allowed for this puzzle")
    class NotPresent(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' is not present in the final word")
    class InconsistentMatch(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' does not agree with other matches in the same column.")
    class NotMatch(letter: Char, x: Int, y: Int) : LetterTile(letter, x, y, "The letter '$letter' does not match the final solution")
}

private const val INVALID_CHAR = '?'

class Validator {
    fun validate(gameSettings: GameSettings, board: Board, words: Set<String>): List<GameError> {
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

            val totalAbsentCharacters = usedAbsentChars.values.sum()

            // If a puzzle only has three (or less) absent squares in it, it's probably fine if two are the same letter
            if (totalAbsentCharacters > 3) {
                val repeatedAbsentCharacters = usedAbsentChars.values.sumOf { it - 1 }

                if ((repeatedAbsentCharacters / totalAbsentCharacters.toFloat()) > gameSettings.maxAbsentRepetitionPercent) {
                    usedAbsentChars.filter { it.value > 1 }.keys.forEach { overusedLetter ->
                        for (y in 0 until board.numRows - 1) {
                            for (x in 0 until Board.NUM_COLS) {
                                val letter = board.letters[x, y] ?: continue
                                if (board.tiles[x, y] == TileState.ABSENT && letter == overusedLetter) {
                                    errors.add(GameError.RepeatedAbsent(letter, x, y))
                                }
                            }
                        }
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
                        if (letter != mutableFinalWord[x]) {
                            errors.add(GameError.NotMatch(letter, x, y))
                        } else {
                            // Remove the letter so it won't be reused by following checks
                            mutableFinalWord[x] = INVALID_CHAR
                        }
                    }
                }

                // Now that we've removed matched characters from consideration, let's check all remaining present tiles
                for (x in 0 until Board.NUM_COLS) {
                    val letter = board.letters[x, y] ?: continue
                    if (board.tiles[x, y] == TileState.PRESENT) {
                        val foundIndex = mutableFinalWord.indexOf(letter)
                        // Direct matches should not be consumed! They're errors
                        if (foundIndex >= 0 && foundIndex != x) {
                            mutableFinalWord[foundIndex] = INVALID_CHAR
                        } else {
                            errors.add(GameError.NotPresent(letter, x, y))
                        }
                    }
                }

                // Now that we've removed matched AND present characters from consideration, let's check all remaining
                // absent tiles
                for (x in 0 until Board.NUM_COLS) {
                    val letter = board.letters[x, y] ?: continue
                    if (board.tiles[x, y] == TileState.ABSENT) {
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