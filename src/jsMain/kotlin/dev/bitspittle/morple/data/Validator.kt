package dev.bitspittle.morple.data

sealed class Error(val message: String) {
    class MissingLetter(val x: Int, val y: Int) : Error("A letter is missing.")
    class InvalidWord(val invalidWord: String, val y: Int) : Error("\"$invalidWord\" is not an accepted word.")
    class RepeatedWord(val repeatedWord: String, val y: Int) : Error("\"$repeatedWord\" was already used earlier.")
    class NotAbsent(letter: Char, val x: Int, val y: Int) : Error("The letter '$letter' is present in the final word")
    class NotPresent(letter: Char, val x: Int, val y: Int) : Error("The letter '$letter' is not present in the final word")
    class NotMatch(letter: Char, val x: Int, val y: Int) : Error("The letter '$letter' does not match the final solution")
}

class Validator {
    fun validate(board: Board, words: Set<String>): List<Error> {
        val errors = mutableListOf<Error>()

        val finalWord = (0 until Board.NUM_COLS)
            .map { x -> board.letters[x, board.numRows - 1] ?: '?' }
            .joinToString("")

        // Missing letters
        for (y in 0 until board.numRows) {
            for (x in 0 until Board.NUM_COLS) {
                if (board.letters[x, y] == null) {
                    errors.add(Error.MissingLetter(x, y))
                }
            }
        }

        // Invalid words
        for (y in 0 until board.numRows) {
            val word = (0 until Board.NUM_COLS).mapNotNull { x -> board.letters[x, y] }.joinToString("")
            if (word.length == Board.NUM_COLS && !words.contains(word)) {
                errors.add(Error.InvalidWord(word, y))
            }
        }

        // Repeated words
        mutableSetOf<String>().let { usedWords ->
            for (y in 0 until board.numRows) {
                val word = (0 until Board.NUM_COLS).mapNotNull { x -> board.letters[x, y] }.joinToString("")
                if (word.length == Board.NUM_COLS && !usedWords.add(word)) {
                    errors.add(Error.RepeatedWord(word, y))
                }
            }
        }

        // Not absent
        // Skip last row - no need to compare against itself
        for (y in 0 until board.numRows - 1) {
            for (x in 0 until Board.NUM_COLS) {
                val letter = board.letters[x, y] ?: continue
                if (board.tiles[x, y] == TileState.ABSENT && finalWord.contains(letter)) {
                    errors.add(Error.NotAbsent(letter, x, y))
                }
            }
        }


        // Not present
        // Skip last row - no need to compare against itself
        for (y in 0 until board.numRows - 1) {
            val finalLetterCounts = finalWord.groupingBy { it }.eachCount().toMutableMap()
            // First, remove characters already matched on this line, they shouldn't be counted against when checking
            // remaining present characters
            for (x in 0 until Board.NUM_COLS) {
                val letter = board.letters[x, y] ?: continue
                if (board.tiles[x, y] == TileState.MATCH && letter == finalWord[x]) {
                    val count = finalLetterCounts.getValue(letter)
                    check(count > 0) // letter == finalWord[x] implies the letter should be counted
                    finalLetterCounts[letter] = count - 1
                }
            }

            for (x in 0 until Board.NUM_COLS) {
                val letter = board.letters[x, y] ?: continue
                if (board.tiles[x, y] == TileState.PRESENT) {
                    val count = finalLetterCounts[letter]
                    if (count == null || count == 0) {
                        errors.add(Error.NotPresent(letter, x, y))
                    } else {
                        finalLetterCounts[letter] = count - 1
                    }
                }
            }
        }

        // Not match
        // Skip last row - no need to compare against itself
        for (y in 0 until board.numRows - 1) {
            for (x in 0 until Board.NUM_COLS) {
                val letter = board.letters[x, y] ?: continue
                if (board.tiles[x, y] == TileState.MATCH && finalWord[x] != letter) {
                    errors.add(Error.NotMatch(letter, x, y))
                }
            }
        }

        return errors
    }
}