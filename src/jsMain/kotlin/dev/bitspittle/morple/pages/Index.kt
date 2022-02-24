package dev.bitspittle.morple.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.clearFocus
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.FaRedo
import com.varabyte.kobweb.silk.components.icons.fa.FaUndo
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.Text
import dev.bitspittle.morple.components.layout.PageLayout
import dev.bitspittle.morple.components.widgets.game.*
import dev.bitspittle.morple.data.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement

private val LETTER_CODES = (('a' .. 'z') + ('A'..'Z')).associate { it.toString() to it.uppercaseChar() }

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

fun <T> MutableMap<T, Unit>.add(key: T) { this[key] = Unit }

external fun decodeURIComponent(encodedURI: String): String

sealed interface GameState {
    object InProgress : GameState
    class Errors(val errors: List<Error>) : GameState
    object Finished : GameState
}

@Page
@Composable
fun HomePage() {
    // "words" is a set but there's no mutableStateSetOf method
    val words = remember { mutableStateMapOf<String, Unit>()}
    val validator = remember { Validator() }
    var ready by remember { mutableStateOf(false) }

    var gameState: GameState by remember { mutableStateOf(GameState.InProgress) }

    LaunchedEffect(Unit) {
        window.fetch("/words.txt")
            .then { response -> response.text() }
            .then { text ->
                text.split("\n").forEach { words.add(it.uppercase()) }
                ready = true
            }
    }

    val ctx = rememberPageContext()
    val board = remember {
        val puzzleValue = ctx.params["puzzle"]

        if (puzzleValue != null) {
            try {
                val puzzleValueBytes = puzzleValue.map { c -> c.code.toByte() }.toByteArray()
                return@remember Board.from(decodeURIComponent(puzzleValueBytes.decodeToString()).toEncoded())
            }
            catch (ex: Exception) {
                println("Skipped puzzle, could not parse it: $ex.\n\nWill choose a random one instead")
            }
        }

        Board.from(
            listOf(
            """
                ⬜🟨🟩⬜⬜
                ⬜⬜🟩🟩⬜
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛⬛⬛🟨
                🟩🟩⬛⬛⬛
                ⬛⬛⬛⬛🟨
                ⬛⬛⬛⬛⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬜🟨⬜🟩⬜
                ⬜⬜⬜⬜🟨
                ⬜🟨🟩🟩⬜
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                🟨🟨🟨⬛🟨
                🟩🟩🟨🟨⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛⬛🟩🟩
                ⬛⬛⬛🟨🟨
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬜🟩⬜⬜🟩
                🟨⬜⬜⬜⬜
                ⬜⬜⬜🟩🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛🟨⬛🟩
                ⬛🟨⬛⬛⬛
                ⬛⬛🟨⬛🟩
                ⬛⬛⬛⬛⬛
                🟨⬛⬛⬛🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛⬛🟨⬛
                ⬛⬛🟨🟨⬛
                🟨🟨🟨🟩⬛
                ⬛🟨⬛⬛⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬜⬜⬜🟨🟨
                ⬜🟨⬜🟨⬜
                🟨🟨🟨🟩🟨
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛⬛🟩⬛
                🟩⬛🟨⬛🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛🟩🟩⬛🟨
                ⬛🟩🟩🟩🟩
                ⬛🟩🟩🟩🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛🟩🟩⬛🟩
                ⬛⬛⬛⬛⬛
                ⬛⬛⬛⬛⬛
                ⬛⬛⬛🟨🟨
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛🟩⬛⬛
                🟩🟨🟩⬛⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                🟩⬜⬜⬜⬜
                ⬜⬜🟩🟩⬜
                ⬜🟨⬜🟨🟨
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛🟨⬛⬛⬛
                ⬛⬛⬛⬛⬛
                ⬛⬛⬛🟩🟨
                ⬛⬛⬛🟨⬛
                ⬛⬛⬛🟨⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛🟩🟩⬛⬛
                ⬛🟩🟩⬛⬛
                ⬛🟩🟩⬛🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                🟩⬛🟨⬛🟨
                ⬛🟨⬛🟨⬛
                🟩🟨🟨🟨⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬜🟨⬜🟨🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            ).random().toEncoded()
        )
    }

    val actionsUndo = remember { mutableStateListOf<Action>() }
    val actionsRedo = remember { mutableStateListOf<Action>() }

    board.resetLetters(actionsUndo)

    val tileRefs = mutableListOf<HTMLElement>()
    val tileRefs2d = MutableList2d(tileRefs, Board.NUM_COLS)

    val lastX = Board.NUM_COLS - 1
    val lastY = board.numRows - 1

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

    PageLayout("Morple", description = "Wordle... but upside-down!") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
//                Row(Modifier.fillMaxWidth().margin(bottom = 1.cssRem), verticalAlignment = Alignment.CenterVertically) {
//                    FaUndo()
//                    Spacer()
//                    Button(onClick = { println("Clicked") }) {
//                        Text("New Game")
//                    }
//                    Spacer()
//                    FaRedo()
//                }
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

                                        CLEAR_CODES[evt.code]?.let {
                                            if (board.letters[x, y] != null) {
                                                actionsUndo.add(Action(x, y, null))
                                                actionsRedo.clear()
                                            }
                                            if (it == ClearKey.BACKSPACE) navLeft(x, y)
                                            gameState = GameState.InProgress
                                        }

                                        if (evt.code == "Enter") {
                                            val errors = validator.validate(board, words.keys)
                                            if (errors.isEmpty()) {
                                                gameState = GameState.Finished
                                                document.activeElement?.clearFocus()
                                            } else {
                                                gameState = GameState.Errors(errors)
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
        }
    }
}
