package dev.bitspittle.morple.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.morple.components.layout.PageLayout
import dev.bitspittle.morple.components.widgets.game.AbsentTileVariant
import dev.bitspittle.morple.components.widgets.game.MatchTileVariant
import dev.bitspittle.morple.components.widgets.game.PresentTileVariant
import dev.bitspittle.morple.components.widgets.game.Tile
import dev.bitspittle.morple.data.Board
import dev.bitspittle.morple.data.TileState
import dev.bitspittle.morple.data.get
import dev.bitspittle.morple.data.toEncoded

@Page
@Composable
fun HomePage() {
    val board = remember {
        Board.from(
            """
                ⬜🟨🟩⬜⬜
                ⬜⬜🟩🟩⬜
                🟩🟩🟩🟩🟩
            """.trimIndent().toEncoded()
        )
    }

    PageLayout("Morple", description = "Wordle... but upside-down!") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
                (0 until board.numRows).forEach { y ->
                    Row {
                        (0 until Board.NUM_COLS).forEach { x ->
                            val dataTile = board[x, y]
                            Tile(dataTile.letter, variant = when (dataTile.state) {
                                TileState.ABSENT -> AbsentTileVariant
                                TileState.PRESENT -> PresentTileVariant
                                TileState.MATCH -> MatchTileVariant
                            })
                        }
                    }
                }
                (board.numRows until Board.MAX_NUM_ROWS).forEach { y ->
                    Row {
                        (0 until Board.NUM_COLS).forEach { _ ->
                            Tile()
                        }
                    }
                }
            }
        }
    }
}
