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
import dev.bitspittle.morple.data.*
import org.w3c.dom.HTMLElement

// Create "KeyA" -> 'A', etc. mapping
private val LETTER_CODES = ('A'..'Z').associateBy { "Key$it" }

enum class NavKey(val value: String) {
    UP("ArrowUp"),
    DOWN("ArrowDown"),
    LEFT("ArrowLeft"),
    RIGHT("ArrowRight"),
    HOME("Home"),
    END("End"),
}

// Create "ArrowUp" -> UP, etc. mapping
private val NAV_CODES = NavKey.values().associateBy { it.value }

enum class ClearKey(val value: String) {
    BACKSPACE("Backspace"),
    DELETE("Delete"),
}

// Create "Backspace" -> BACKSPACE, etc. mapping
private val CLEAR_CODES = ClearKey.values().associateBy { it.value }


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

    val actionsUndo = remember { mutableStateListOf<Action>() }
    val actionsRedo = remember { mutableStateListOf<Action>() }

    board.resetLetters(actionsUndo)

    val _tileRefs = mutableListOf<HTMLElement>()
    val tileRefs = MutableList2d(_tileRefs, Board.NUM_COLS)

    val lastX = Board.NUM_COLS - 1
    val lastY = board.numRows - 1

    PageLayout("Morple", description = "Wordle... but upside-down!") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
                (0 until board.numRows).forEach { y ->
                    Row {
                        (0 until Board.NUM_COLS).forEach { x ->
                            val dataTile = board.tiles[x, y]
                            Tile(
                                board.letters[x, y],
                                Modifier.onKeyDown { evt ->
                                    fun navUp() { if (y > 0) tileRefs[x, y - 1].focus() }
                                    fun navDown() { if (y < lastY) tileRefs[x, y + 1].focus() }
                                    fun navLeft() {
                                        if (x > 0) tileRefs[x - 1, y].focus()
                                        else if (y > 0) tileRefs[lastX, y - 1].focus()
                                    }
                                    fun navRight() {
                                        if (x < lastX) tileRefs[x + 1, y].focus()
                                        else if (y < lastY) tileRefs[0, y + 1].focus()
                                    }
                                    fun navHome() { if (x > 0) tileRefs[0, y].focus() }
                                    fun navEnd() { if (x < lastX) tileRefs[lastX, y].focus() }

                                    LETTER_CODES[evt.code]?.let { letter ->
                                        if (letter == 'Z' && !evt.altKey && evt.shiftKey && evt.ctrlKey) {
                                            // Redo
                                            if (actionsRedo.isNotEmpty()) {
                                                actionsUndo.add(actionsRedo.removeFirst())
                                                actionsUndo.last().let { action ->
                                                    tileRefs[action.x, action.y].focus()
                                                }
                                            }
                                        } else if (letter == 'Z' && !evt.altKey && !evt.shiftKey && evt.ctrlKey) {
                                            // Undo
                                            if (actionsUndo.isNotEmpty()) {
                                                actionsUndo.last().let { action ->
                                                    tileRefs[action.x, action.y].focus()
                                                }
                                                actionsRedo.add(0, actionsUndo.removeLast())
                                            }
                                        } else if (!evt.altKey && !evt.ctrlKey) {
                                            actionsUndo.add(Action(x, y, letter))
                                            actionsRedo.clear()
                                            navRight()
                                        }
                                        Unit
                                    }

                                    NAV_CODES[evt.code]?.let { navKey ->
                                        when (navKey) {
                                            NavKey.UP -> navUp()
                                            NavKey.DOWN -> navDown()
                                            NavKey.LEFT -> navLeft()
                                            NavKey.RIGHT -> navRight()
                                            NavKey.HOME -> navHome()
                                            NavKey.END -> navEnd()
                                        }
                                    }

                                    CLEAR_CODES[evt.code]?.let {
                                        actionsUndo.add(Action(x, y, null))
                                        actionsRedo.clear()
                                        if (it == ClearKey.BACKSPACE) navLeft()
                                    }
                                },
                                variant = when (dataTile.state) {
                                    TileState.ABSENT -> AbsentTileVariant
                                    TileState.PRESENT -> PresentTileVariant
                                    TileState.MATCH -> MatchTileVariant
                                },
                                elementScope = {
                                    DomSideEffect { div ->
                                        _tileRefs.add(div)
                                    }
                                }
                            )
                        }
                    }
                }
                (board.numRows until Board.MAX_NUM_ROWS).forEach { _ ->
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
