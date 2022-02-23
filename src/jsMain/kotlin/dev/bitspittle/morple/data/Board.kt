package dev.bitspittle.morple.data

private val ENCODED_PART_REGEX = Regex("""([A-Z]?[+\-*])""")

private val ENCODED_WORDLE_MAP = mapOf(
    // Wordle squares generated by light mode
    "⬜" to '-',
    "⬛" to '*',
    "🟨" to '+',
    // Wordle squares generated by dark mode
    "⬛" to '-',
    "\uD83D\uDFE8" to '*',
    "\uD83D\uDFE9" to '+',
)

fun String.toEncoded(): String {
    val encoded = mutableListOf<Char>()
    var remaining = this
    while (remaining.isNotEmpty()) {
        val wordleSquare = ENCODED_WORDLE_MAP.keys.firstOrNull { wordleSquare -> remaining.startsWith(wordleSquare) }
        remaining = if (wordleSquare != null) {
            encoded.add(ENCODED_WORDLE_MAP.getValue(wordleSquare))
            remaining.removePrefix(wordleSquare)
        } else {
            remaining.drop(1)
        }
    }

    return encoded.joinToString("")
}

class Board(private val initialState: List<Pair<TileState, Char?>>) {
    companion object {
        const val NUM_COLS = 5
        const val MAX_NUM_ROWS = 6

        /**
         * A board represented as an encoded string.
         *
         * In this text representation, "-" means no match, "+" means an exact match, and "*" means present but in a
         * different column.
         *
         * If a letter (A-Z) is specified, then the character following it represents its state, e.g. "A-" means this
         * tile's character is the letter A which is not present in the final solution.
         */
        fun from(encodedBoard: String): Board {
            val tiles = ENCODED_PART_REGEX.findAll(encodedBoard)
                .map { result -> result.groupValues.first() }
                .map { part ->
                    check(part.length == 1 || part.length == 2) { "Unexpected encoded string part: $part" }
                    val tileState = when (val c = part.last()) {
                        '-' -> TileState.ABSENT
                        '*' -> TileState.PRESENT
                        '+' -> TileState.MATCH
                        else -> error { "Unexpected encoded char: $c" }
                    }
                    val letter = if (part.length == 2) part.first() else null
                    tileState to letter
                }
                .toList()

            return Board(tiles)
        }
    }

    private val _tiles = initialState.map { it.first }
    private val _letters = initialState.map { it.second }.toMutableList()
    val numRows = initialState.size / NUM_COLS

    init {
        require(initialState.size % NUM_COLS == 0) { "Tried to create a board without $NUM_COLS columns in each row" }
        require(numRows in (1..MAX_NUM_ROWS)) { "Tried to create a board without 1 to $MAX_NUM_ROWS rows" }

        _tiles.chunked(5).let { chunked ->
            chunked.forEachIndexed { y, row ->
                if (y == chunked.lastIndex) {
                    require(row.all { tileState -> tileState == TileState.MATCH }) {
                        "Tried to create a board where the final row isn't all matches"
                    }
                } else {
                    require(!row.all { tileState -> tileState == TileState.MATCH }) {
                        "Tried to create a board where an intermediate row is all matches"
                    }
                }
            }
        }
    }

    val tiles = List2d(_tiles, NUM_COLS)
    val letters = MutableList2d(_letters, NUM_COLS)

    fun resetLetters(actions: List<Action>) {
        initialState
            .map { it.second }
            .forEachIndexed { i, c -> _letters[i] = c }

        actions.forEach { action ->
            letters[action.x, action.y] = action.letter
            // If editing a match tile, set all vertical match tiles as well
            if (tiles[action.x, action.y] == TileState.MATCH) {
                (0 until numRows)
                    .filter { y -> y != action.y }
                    .forEach { y ->
                        if (tiles[action.x, y] == TileState.MATCH) {
                            letters[action.x, y] = action.letter
                        }
                    }
            }
        }
    }
}

