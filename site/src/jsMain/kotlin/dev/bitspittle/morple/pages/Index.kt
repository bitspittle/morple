package dev.bitspittle.morple.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.icons.fa.FaCircleExclamation
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.morple.common.board.Board
import dev.bitspittle.morple.common.board.Pt
import dev.bitspittle.morple.common.collections.MutableList2d
import dev.bitspittle.morple.components.layout.PageLayout
import dev.bitspittle.morple.components.widgets.game.*
import dev.bitspittle.morple.components.widgets.overlay.Modal
import dev.bitspittle.morple.data.*
import dev.bitspittle.morple.toSitePalette
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement

fun <T> MutableMap<T, Unit>.add(key: T) {
    this[key] = Unit
}

external fun decodeURIComponent(encodedURI: String): String

val ErrorIconStyle = ComponentStyle.base("morple-error") {
    Modifier.color(colorMode.toSitePalette().error).cursor(Cursor.Pointer)
}

@Page
@Composable
fun HomePage() {
    // "words" is a set but there's no mutableStateSetOf method
    val words = remember { mutableStateMapOf<String, Unit>() }
    val validator = remember { Validator() }
    var ready by remember { mutableStateOf(false) }

    val mutableGameState = remember { mutableStateOf<GameState>(GameState.Normal) }
    val mutableActiveTile = remember { mutableStateOf(Pt()) }
    val mutableKeyCount = remember { mutableStateOf(0) }
    val mutableErrors = remember { mutableStateListOf<GameError>() }
    val mutableShowErrors = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        window.fetch("/words.txt")
            .then { response -> response.text() }
            .then { text ->
                text.split("\n").forEach { words.add(it.uppercase()) }
                ready = true
            }
    }

    val ctx = rememberPageContext()

    val gameSettings = GameSettings.from(GameMode.HARD)
//    val gameSettings = remember {
//        val gameMode = try {
//            ctx.params["mode"]?.let { GameMode.valueOf(it.uppercase()) } ?: GameMode.EASY
//        } catch (ex: Exception) {
//            GameMode.EASY
//        }
//
//        GameSettings.from(gameMode)
//    }

    val board = remember {
        val puzzleValue = ctx.params["puzzle"]

        if (puzzleValue != null) {
            try {
                val puzzleValueBytes = puzzleValue.map { c -> c.code.toByte() }.toByteArray()
                return@remember GameBoard.from(gameSettings, decodeURIComponent(puzzleValueBytes.decodeToString()).toEncoded())
            } catch (ex: Exception) {
                println("Skipped puzzle, could not parse it: $ex.\n\nWill choose a random one instead")
            }
        }

        GameBoard.from(
            gameSettings,
            "+T*A+R-E-T--A+C-T+S+I--C--K--O-T**-L-I-S*S-+++F++"
        )
    }

    val actionsUndo = remember { mutableStateListOf<Action>() }
    val actionsRedo = remember { mutableStateListOf<Action>() }

    // Empty tiles are obvious -- no need to show a distracting error icon for them
    val indexedTileErrors = mutableErrors.filterIsInstance<GameError.Tile>().filter { it !is GameError.EmptyTile }.groupBy { Pt(it.x, it.y) }
    val indexedRowErrors = mutableErrors.filterIsInstance<GameError.Row>().groupBy { it.y }

    var showErrorModal by remember { mutableStateOf(false) }
    val activeTile by mutableActiveTile

    PageLayout(
        "Morple", description = "Wordle... but upside-down!",
        extraAction = {
            val showErrors by mutableShowErrors
            if (showErrors && indexedTileErrors.contains(activeTile) || indexedRowErrors.contains(activeTile.y)) {
                FaCircleExclamation(ErrorIconStyle.toModifier().onClick {
                    showErrorModal = true
                })
            }
        }
    ) {
        val tileRefs = mutableListOf<HTMLElement>()
        val tileRefs2d = MutableList2d(tileRefs, Board.NUM_COLS)
        val navigator = Navigator(
            board.numRows,
            Board.NUM_COLS,
            getPos = { mutableActiveTile.value },
            setFocus = { pt ->
                mutableActiveTile.value = pt
                tileRefs2d[pt.x, pt.y].focus()
            }
        )
        val commandHandler =
            CommandHandler(
                gameSettings,
                board,
                navigator,
                validator,
                words.keys,
                mutableGameState,
                mutableKeyCount,
                actionsUndo,
                actionsRedo,
                mutableErrors,
                mutableShowErrors
            )

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer()
                MorpleBoard(
                    gameSettings,
                    board,
                    navigator,
                    commandHandler,
                    tileRefs,
                    mutableGameState,
                    mutableActiveTile,
                    mutableErrors,
                    mutableShowErrors.value,
                    forceInvalidationWhenBoardChanges = { actionsUndo.firstOrNull() },
                )
                Spacer()
                Keyboard(
                    gameSettings,
                    board,
                    onKeyPressed = keyHandler@ { keyAction ->
                        if (mutableGameState.value == GameState.Finished) return@keyHandler

                        when (keyAction) {
                            is KeyAction.Type -> commandHandler.type(keyAction.letter)
                            KeyAction.Submit -> commandHandler.submit()
                            KeyAction.Backspace -> commandHandler.delete(moveLeftIfEmpty = true)
                            KeyAction.Undo -> commandHandler.undo()
                            KeyAction.Redo -> commandHandler.redo()
                        }
                    },
                    forceInvalidationWhenBoardChanges = { actionsUndo.firstOrNull() }
                )
            }
        }

        if (showErrorModal) {
            Modal(onCloseRequested = { showErrorModal = false }) {
                Column(Modifier.rowGap(1.cssRem)) {
                    indexedRowErrors[activeTile.y]?.forEach {
                        Div { SpanText(it.message) }
                    }
                    indexedTileErrors[activeTile]?.forEach {
                        Div { SpanText(it.message) }
                    }
                }
            }
        }
    }
}
