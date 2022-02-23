package dev.bitspittle.morple.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.text.Text
import dev.bitspittle.morple.components.layout.PageLayout
import dev.bitspittle.morple.components.widgets.game.*
import dev.bitspittle.morple.data.*
import kotlinx.browser.window
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

@Page
@Composable
fun HomePage() {
    // "words" is a set but there's no mutableStateSetOf method
    val words = remember { mutableStateMapOf<String, Unit>()}
    val validator = remember { Validator() }
    var ready by remember { mutableStateOf(false) }

    var validationText by remember { mutableStateOf("") }

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
                (0 until board.numRows).forEach { y ->
                    Row {
                        (0 until Board.NUM_COLS).forEach { x ->
                            val tileState = board.tiles[x, y]
                            Tile(
                                board.letters[x, y],
                                Modifier.onKeyDown { evt ->
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

                                        validationText = ""
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
                                        actionsUndo.add(Action(x, y, null))
                                        actionsRedo.clear()
                                        if (it == ClearKey.BACKSPACE) navLeft(x, y)
                                    }

                                    if (evt.code == "Enter") {
                                        validationText = buildString {
                                            val errors = validator.validate(board, words.keys)
                                            if (errors.isEmpty()) {
                                                append("CONGRATULATIONS! You Won!")
                                            } else {
                                                errors.forEach { error -> appendLine(error.message) }
                                            }
                                        }
                                    }
                                },
                                variant = when (tileState) {
                                    TileState.ABSENT -> AbsentTileVariant
                                    TileState.PRESENT -> PresentTileVariant
                                    TileState.MATCH -> MatchTileVariant
                                },
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

                if (validationText.isNotEmpty()) {
                    Column {
                        validationText.split("\n").forEach { line -> Div { Text(line) } }
                    }
                }
            }
        }
    }
}
