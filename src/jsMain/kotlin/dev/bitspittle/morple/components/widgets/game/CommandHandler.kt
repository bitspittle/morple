package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import dev.bitspittle.morple.data.Action
import dev.bitspittle.morple.data.Board
import dev.bitspittle.morple.data.Validator
import dev.bitspittle.morple.pages.GameState

class CommandHandler(
    private val board: Board,
    private val navigator: Navigator,
    private val validator: Validator,
    private val words: Set<String>,
    mutableGameState: MutableState<GameState>,
    mutableKeyCount: MutableState<Int>,
    private val actionsUndo: MutableList<Action>,
    private val actionsRedo: MutableList<Action>,
) {
    private var gameState by mutableGameState
    private var keyCount by mutableKeyCount

    fun undo() {
        if (actionsUndo.isNotEmpty()) {
            actionsUndo.last().let { action ->
                navigator.navTo(action.x, action.y)
            }
            actionsRedo.add(0, actionsUndo.removeLast())
        }

        gameState = GameState.InProgress
    }

    fun redo() {
        if (actionsRedo.isNotEmpty()) {
            actionsUndo.add(actionsRedo.removeFirst())
            actionsUndo.last().let { action ->
                navigator.navTo(action.x, action.y)
            }
        }

        gameState = GameState.InProgress
    }

    fun type(letter: Char) {
        if (board.letters[navigator.x, navigator.y] != letter) {
            keyCount++
        }

        actionsUndo.add(Action(navigator.x, navigator.y, letter))
        actionsRedo.clear()
        navigator.navRight()

        gameState = GameState.InProgress
    }

    fun delete(moveLeft: Boolean = false) {
        // Special case handling for the very last char, since we can't move past it.
        val isOnLastChar =
            navigator.x == navigator.maxX && navigator.y == navigator.maxY &&
                board.letters[navigator.x, navigator.y] != null

        if (moveLeft && !isOnLastChar) navigator.navLeft()
        if (board.letters[navigator.x, navigator.y] != null) {
            actionsUndo.add(Action(navigator.x, navigator.y, null))
            actionsRedo.clear()
        }

        gameState = GameState.InProgress
    }

    fun row(rowIndex: Int) {
        if (rowIndex < board.numRows) {
            navigator.navTo(navigator.x, rowIndex)
        }
    }

    fun submit() {
        val errors = validator.validate(board, words)
        gameState = if (errors.isEmpty()) GameState.Finished else GameState.Errors(errors)
    }
}
