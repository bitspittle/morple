package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.varabyte.kobweb.compose.dom.clearFocus
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.toCssColor
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.morple.data.*
import dev.bitspittle.morple.pages.*
import dev.bitspittle.morple.toSitePalette
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.w3c.dom.HTMLElement

private val LETTER_CODES = (('a' .. 'z') + ('A'..'Z')).associate { it.toString() to it.uppercaseChar() }
private val ROW_CODES = (1..6).associateBy { it.toString() }

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

val FinishedBoardStyle = ComponentStyle.base("morple-finished-board") {
    Modifier.outline(width = 4.px, LineStyle.Double, colorMode.toSitePalette().finished.toCssColor())
}

val ErrorRowStyle = ComponentStyle.base("morple-error-row") {
    Modifier.outline(width = 1.px, LineStyle.Solid, colorMode.toSitePalette().error.toCssColor())
}

@Composable
fun MorpleBoard(
    validator: Validator,
    board: Board,
    words: Set<String>,
    mutableGameState: MutableState<GameState>,
    actionsUndo: SnapshotStateList<Action>,
    actionsRedo: SnapshotStateList<Action>,
) {
    board.resetLetters(actionsUndo)

    val tileRefs = mutableListOf<HTMLElement>()
    val tileRefs2d = MutableList2d(tileRefs, Board.NUM_COLS)

    val lastX = Board.NUM_COLS - 1
    val lastY = board.numRows - 1

    var gameState by mutableGameState

    fun navUp(x: Int, y: Int) { if (y > 0) tileRefs2d[x, y - 1].focus() }
    fun navDown(x: Int, y: Int) { if (y < lastY) tileRefs2d[x, y + 1].focus() }
    fun navLeft(x: Int, y: Int) {
        if (x > 0) tileRefs2d[x - 1, y].focus()
        else if (y > 0) tileRefs2d[lastX, y - 1].focus()
    }
    fun navRight(x: Int, y: Int) {
        if (x < lastX) tileRefs2d[x + 1, y].focus()
        else if (y < lastY) tileRefs2d[0, y + 1].focus()
    }
    fun navHome(x: Int, y: Int) { if (x > 0) tileRefs2d[0, y].focus() }
    fun navEnd(x: Int, y: Int) { if (x < lastX) tileRefs2d[lastX, y].focus() }

    Column(FinishedBoardStyle.toModifier().takeIf { gameState is GameState.Finished } ?: Modifier) {
        (0 until board.numRows).forEach { y ->
            Row(
                ErrorRowStyle.toModifier().takeIf {
                    gameState.let { gameState -> // Convert var to local
                        gameState is GameState.Errors && gameState.errors.any { error ->
                            error is Error.Row && error.y == y
                        }
                    }
                } ?: Modifier
            ) {
                (0 until Board.NUM_COLS).forEach { x ->
                    val tileState = board.tiles[x, y]
                    Tile(
                        board.letters[x, y],
                        Modifier.onKeyDown { evt ->
                            if (gameState == GameState.Finished) {
                                document.activeElement?.clearFocus()
                                return@onKeyDown
                            }

                            LETTER_CODES[evt.key]?.let { letter ->
                                if (letter == 'Z' && !evt.altKey && evt.shiftKey && evt.ctrlKey) {
                                    // Redo
                                    if (actionsRedo.isNotEmpty()) {
                                        actionsUndo.add(actionsRedo.removeFirst())
                                        actionsUndo.last().let { action ->
                                            tileRefs2d[action.x, action.y].focus()
                                        }
                                    }
                                } else if (letter == 'Z' && !evt.altKey && !evt.shiftKey && evt.ctrlKey) {
                                    // Undo
                                    if (actionsUndo.isNotEmpty()) {
                                        actionsUndo.last().let { action ->
                                            tileRefs2d[action.x, action.y].focus()
                                        }
                                        actionsRedo.add(0, actionsUndo.removeLast())
                                    }
                                } else if (!evt.altKey && !evt.ctrlKey) {
                                    actionsUndo.add(Action(x, y, letter))
                                    actionsRedo.clear()
                                    navRight(x, y)
                                }

                                gameState = GameState.InProgress
                            }

                            NAV_CODES[evt.code]?.let { navKey ->
                                when (navKey) {
                                    NavKey.UP -> navUp(x, y)
                                    NavKey.DOWN -> navDown(x, y)
                                    NavKey.LEFT -> navLeft(x, y)
                                    NavKey.RIGHT -> navRight(x, y)
                                    NavKey.HOME -> navHome(x, y)
                                    NavKey.END -> navEnd(x, y)
                                }
                            }

                            ROW_CODES[evt.key]?.let { rowKey ->
                                val rowIndex = rowKey - 1
                                if (rowIndex < board.numRows) {
                                    tileRefs2d[x, rowIndex].focus()
                                }
                            }

                            CLEAR_CODES[evt.code]?.let {
                                if (board.letters[x, y] != null) {
                                    actionsUndo.add(Action(x, y, null))
                                    actionsRedo.clear()
                                }
                                if (it == ClearKey.BACKSPACE) navLeft(x, y)
                                gameState = GameState.InProgress
                            }

                            if (evt.code == "Enter") {
                                val errors = validator.validate(board, words)
                                gameState =
                                    if (errors.isEmpty()) {
                                        document.activeElement?.clearFocus()
                                        GameState.Finished
                                    } else {
                                        gameState = GameState.Finished
                                        GameState.Errors(errors)
                                    }
                            }
                        },
                        variant = when (tileState) {
                            TileState.ABSENT -> AbsentTileVariant
                            TileState.PRESENT -> PresentTileVariant
                            TileState.MATCH -> MatchTileVariant
                        }.then(
                            gameState.let { gameState -> // Convert var to local
                                if (gameState is GameState.Errors) {
                                    when {
                                        gameState.errors.any { error ->
                                            error is Error.Tile && error.x == x && error.y == y
                                        } -> ErrorTileVariant
                                        else -> null
                                    }
                                } else {
                                    null
                                }
                            } ?: ComponentVariant.Empty
                        ),
                        elementScope = {
                            DomSideEffect { div ->
                                tileRefs.add(div)
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
