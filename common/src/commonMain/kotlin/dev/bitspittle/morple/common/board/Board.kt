package dev.bitspittle.morple.common.board

import dev.bitspittle.morple.common.board.Board.Companion.MAX_NUM_ROWS
import dev.bitspittle.morple.common.board.Board.Companion.NUM_COLS
import dev.bitspittle.morple.common.collections.List2d
import dev.bitspittle.morple.common.collections.MutableList2d

interface Board {
    companion object {
        const val NUM_COLS = 5
        const val MAX_NUM_ROWS = 6
    }

    val tiles: List2d<TileState>
    val letters: List2d<Char?>
    val numRows: Int
}

// A board with the tiles finalized but with the letters free to edit
interface FinalizedBoard : Board {
    override val letters: MutableList2d<Char?>
}

class MutableBoard(override val numRows: Int) : FinalizedBoard {
    override val tiles = MutableList2d(MutableList(NUM_COLS * numRows) { TileState.ABSENT }, NUM_COLS)
    override val letters = MutableList2d(MutableList<Char?>(NUM_COLS * numRows) { null }, NUM_COLS)
}
