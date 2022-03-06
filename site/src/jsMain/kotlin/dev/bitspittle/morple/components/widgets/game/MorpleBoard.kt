package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.toCssColor
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.morple.common.board.Pt
import dev.bitspittle.morple.common.board.TileState
import dev.bitspittle.morple.data.*
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

val BoardStyle = ComponentStyle.base("morple-board") {
    Modifier
}

val FinishedBoardStyle = ComponentStyle.base("morple-finished-board") {
    Modifier.boxShadow("0 0 10px 4px ${colorMode.toSitePalette().tile.match.toCssColor()}")
}

val ErrorRowStyle = ComponentStyle.base("morple-error-row") {
    Modifier.boxShadow("0 0 4px 1px ${colorMode.toSitePalette().error.toCssColor()}")
}

@Composable
fun MorpleBoard(
    gameSettings: GameSettings,
    board: Board,
    navigator: Navigator,
    commandHandler: CommandHandler,
    tileRefs: MutableList<HTMLElement>,
    mutableGameState: MutableState<GameState>,
    mutableActiveTile: MutableState<Pt>,
    errors: List<GameError>,
    showErrors: Boolean,
    forceInvalidationWhenBoardChanges: () -> Unit,
) {
    forceInvalidationWhenBoardChanges()

    val gameState by mutableGameState
    val activeTile by mutableActiveTile

    val indexedTileErrors = errors.filterIsInstance<GameError.Tile>().groupBy { Pt(it.x, it.y) }
    val indexedRowErrors = errors.filterIsInstance<GameError.Row>().groupBy { it.y }

    LaunchedEffect(board) {
        document.onkeydown = keyHandler@ { evt ->
            if (gameState == GameState.Finished) {
                return@keyHandler Unit
            }

            evt.preventDefault()
            LETTER_CODES[evt.key]?.let { letter ->
                if (letter == 'Z' && !evt.altKey && evt.shiftKey && evt.ctrlKey) {
                    commandHandler.redo()
                } else if (letter == 'Z' && !evt.altKey && !evt.shiftKey && evt.ctrlKey) {
                    commandHandler.undo()
                } else if (!evt.altKey && !evt.ctrlKey) {
                    commandHandler.type(letter)
                }
            }

            NAV_CODES[evt.code]?.let { navKey ->
                when (navKey) {
                    NavKey.UP -> navigator.navUp()
                    NavKey.DOWN -> navigator.navDown()
                    NavKey.LEFT -> navigator.navLeft()
                    NavKey.RIGHT -> navigator.navRight()
                    NavKey.HOME -> navigator.navHome()
                    NavKey.END -> navigator.navEnd()
                }
            }

            ROW_CODES[evt.key]?.let { rowKey ->
                commandHandler.row(rowKey - 1)
            }

            CLEAR_CODES[evt.code]?.let {
                commandHandler.delete(moveLeftIfEmpty = it === ClearKey.BACKSPACE)
            }

            if (evt.code == "Enter") {
                commandHandler.submit()
            }
        }
    }

    Column(BoardStyle.toModifier().then(FinishedBoardStyle.toModifier().takeIf { gameState is GameState.Finished } ?: Modifier)) {
        (0 until board.numRows).forEach { y ->
            Row(
                ErrorRowStyle.toModifier().takeIf { showErrors && indexedRowErrors.contains(y) } ?: Modifier
            ) {
                (0 until Board.NUM_COLS).forEach { x ->
                    val tileState = board.tiles[x, y]
                    Tile(
                        board.letters[x, y],
                        board.isLocked[x, y],
                        Modifier.onClick {
                            navigator.navTo(x, y)
                        },
                        variant = when (tileState) {
                            TileState.ABSENT -> AbsentTileVariant
                            TileState.PRESENT -> PresentTileVariant
                            TileState.MATCH -> MatchTileVariant
                        }.then(run {
                            val pt = Pt(x, y)
                            val tileErrors = indexedTileErrors[pt] ?: return@run ComponentVariant.Empty

                            when {
                                // Showing all empty tiles is way too noisy in "showErrorsInstantly" mode
                                showErrors && !gameSettings.showErrorsInstantly && tileErrors.filterIsInstance<GameError.EmptyTile>()
                                    .any() -> EmptyErrorTileVariant

                                showErrors && tileErrors.filterIsInstance<GameError.LetterTile>()
                                    .any { it !is GameError.RepeatedAbsent } ->
                                    LetterErrorTileVariant

                                // Repeated tiles are too hard to reason about without doing math, so always show them
                                tileErrors.filterIsInstance<GameError.RepeatedAbsent>().any() ->
                                    LetterWarningTileVariant

                                else -> ComponentVariant.Empty
                            }
                        }).thenIf(gameState != GameState.Finished && x == activeTile.x && y == activeTile.y, FocusedTileVariant),
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
