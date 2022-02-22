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
        if (wordleSquare != null) {
            encoded.add(ENCODED_WORDLE_MAP.getValue(wordleSquare))
            remaining = remaining.removePrefix(wordleSquare)
        } else {
            remaining = remaining.drop(1)
        }
    }

    return encoded.joinToString("")
}

data class Board(val tiles: List<Tile>) {
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
            println(encodedBoard)
            println(
                ENCODED_PART_REGEX.findAll(encodedBoard).map { result ->
                    result.groupValues.first()
                }.joinToString(", ")
            )

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
                    Tile(tileState, letter)
                }
                .toList()

            return Board(tiles)
        }
    }

    val numRows = tiles.size / NUM_COLS

    init {
        require(tiles.size % NUM_COLS == 0) { "Tried to create a board without $NUM_COLS columns in each row" }
        require(numRows in (1..MAX_NUM_ROWS)) { "Tried to create a board without 1 to $MAX_NUM_ROWS rows" }

        tiles.chunked(5).let { chunked ->
            chunked.forEachIndexed { y, row ->
                if (y == chunked.lastIndex) {
                    require(row.all { tile -> tile.state == TileState.MATCH }) {
                        "Tried to create a board where the final row isn't all matches"
                    }
                } else {
                    require(!row.all { tile -> tile.state == TileState.MATCH }) {
                        "Tried to create a board where an intermediate row is all matches"
                    }
                }
            }
        }
    }
}

operator fun Board.get(x: Int, y: Int): Tile = this.tiles[y * Board.NUM_COLS + x]

