package dev.bitspittle.morple.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import dev.bitspittle.morple.components.layout.PageLayout
import dev.bitspittle.morple.components.widgets.game.*
import dev.bitspittle.morple.data.*
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

fun <T> MutableMap<T, Unit>.add(key: T) {
    this[key] = Unit
}

external fun decodeURIComponent(encodedURI: String): String

@Page
@Composable
fun HomePage() {
    // "words" is a set but there's no mutableStateSetOf method
    val words = remember { mutableStateMapOf<String, Unit>() }
    val validator = remember { Validator() }
    var ready by remember { mutableStateOf(false) }

    val mutableGameState = remember { mutableStateOf<GameState>(GameState.Normal) }
    val mutableActiveTile = remember { mutableStateOf(0 to 0) }
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

    val gameSettings = remember {
        val gameMode = try {
            ctx.params["mode"]?.let { GameMode.valueOf(it.uppercase()) } ?: GameMode.EASY
        } catch (ex: Exception) {
            GameMode.EASY
        }

        GameSettings.from(gameMode)
    }

    val board = remember {
        val puzzleValue = ctx.params["puzzle"]

        if (puzzleValue != null) {
            try {
                val puzzleValueBytes = puzzleValue.map { c -> c.code.toByte() }.toByteArray()
                return@remember Board.from(gameSettings, decodeURIComponent(puzzleValueBytes.decodeToString()).toEncoded())
            } catch (ex: Exception) {
                println("Skipped puzzle, could not parse it: $ex.\n\nWill choose a random one instead")
            }
        }

        val sampleBoards = listOf(
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
            """
                ⬛⬛⬛⬛⬛
                ⬛🟨⬛⬛⬛
                🟨🟨⬛⬛⬛
                ⬛🟨🟨🟨⬛
                ⬛🟩🟩⬛🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬜⬜⬜⬜⬜
                ⬜⬜⬜🟨🟨
                ⬜🟩🟩⬜🟩
                ⬜🟩🟩⬜🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛🟨⬛⬛⬛
                ⬛🟩🟨⬛⬛
                ⬛🟩⬛🟨⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬜🟨⬜⬜🟩
                ⬜⬜⬜🟨⬜
                ⬜🟩🟩⬜🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛⬛⬛⬛
                ⬛⬛⬛🟩⬛
                ⬛⬛⬛⬛⬛
                ⬛🟩🟩🟩⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛⬛⬛⬛
                ⬛⬛🟨⬛⬛
                ⬛🟩⬛⬛⬛
                ⬛🟩⬛⬛⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛⬛⬛⬛
                ⬛⬛🟨⬛⬛
                🟨⬛⬛⬛⬛
                ⬛⬛🟨🟨⬛
                ⬛⬛⬛🟩🟩
                🟩🟩🟩🟩🟩
            """.trimIndent(),
            """
                ⬛⬛⬛⬛⬛
                ⬛⬛⬛⬛⬛
                ⬛⬛⬛⬛⬛
                🟨🟩⬛🟩⬛
                🟩🟩🟩🟩🟩
            """.trimIndent(),
        )

        Board.from(
            gameSettings,
            sampleBoards.random().toEncoded()
        )
    }

    val actionsUndo = remember { mutableStateListOf<Action>() }
    val actionsRedo = remember { mutableStateListOf<Action>() }

    PageLayout("Morple", description = "Wordle... but upside-down!") {
        val tileRefs = mutableListOf<HTMLElement>()
        val navigator = Navigator(MutableList2d(tileRefs, Board.NUM_COLS), mutableActiveTile)
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
                    mutableErrors,
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
    }
}
