package dev.bitspittle.morple.data

import dev.bitspittle.morple.common.board.Board.Companion.MAX_NUM_ROWS
import dev.bitspittle.morple.common.board.Board.Companion.NUM_COLS
import dev.bitspittle.morple.common.board.FinalizedBoard
import dev.bitspittle.morple.common.board.MutableBoard
import dev.bitspittle.morple.common.board.TileState
import dev.bitspittle.morple.common.collections.List2d
import dev.bitspittle.morple.common.collections.forEachIndexed
import dev.bitspittle.morple.common.collections.forEachRowIndexed
import dev.bitspittle.morple.common.collections.map

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

class GameBoard(private val gameSettings: GameSettings, private val initialState: List<Pair<TileState, Char?>>)
    : FinalizedBoard {
    companion object {
        /**
         * A board represented as an encoded string.
         *
         * In this text representation, "-" means no match, "+" means an exact match, and "*" means present but in a
         * different column.
         *
         * If a letter (A-Z) is specified, then the character following it represents its state, e.g. "A-" means this
         * tile's character is the letter A which is not present in the final solution.
         */
        fun from(gameSettings: GameSettings, encodedBoard: String): GameBoard {
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

            return GameBoard(gameSettings, tiles)
        }
    }

    private val board = MutableBoard(initialState.size / NUM_COLS).apply {
        for (i in initialState.indices) {
            val y = i / NUM_COLS
            val x = i % NUM_COLS
            tiles[x, y] = initialState[i].first
            letters[x, y] = initialState[2].second
        }
    }

    override val numRows = board.numRows
    override val tiles: List2d<TileState> = board.tiles
    override val letters = board.letters

    var isFilled: Boolean = false
        private set

    init {
        require(initialState.size % NUM_COLS == 0) { "Tried to create a board without $NUM_COLS columns in each row" }
        require(numRows in (1..MAX_NUM_ROWS)) { "Tried to create a board without 1 to $MAX_NUM_ROWS rows" }

        tiles.list1d.chunked(NUM_COLS).let { chunked ->
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

        resetLetters(listOf())
    }

    /**
     * Whether the letter at the current position can be changed or not
     */
    val isLocked = letters.map { it != null }

    fun resetLetters(actions: List<Action>) {
        initialState
            .map { it.second }
            .forEachIndexed { i, c -> letters[i] = c }

        actions.forEach { action ->
            if (isLocked[action.x, action.y]) return@forEach

            letters[action.x, action.y] = action.letter
        }

        isFilled = letters.list1d.all { it != null }
    }
}

