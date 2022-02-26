package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import dev.bitspittle.morple.data.*

class CommandHandler(
    private val gameSettings: GameSettings,
    private val board: Board,
    private val navigator: Navigator,
    private val validator: Validator,
    private val words: Set<String>,
    mutableGameState: MutableState<GameState>,
    mutableKeyCount: MutableState<Int>,
    private val actionsUndo: MutableList<Action>,
    private val actionsRedo: MutableList<Action>,
    private val mutableErrors: MutableList<GameError>,
    private val mutableShowErrors: MutableState<Boolean>
) {
    private var gameState by mutableGameState
    private var keyCount by mutableKeyCount

    private fun checkValidState() {
        check(gameState != GameState.Finished)
    }

    private fun updateBoardAndErrors() {
        board.resetLetters(actionsUndo)
        mutableErrors.clear()
        mutableErrors.addAll(validator.validate(gameSettings, board, words))

        mutableShowErrors.value = gameSettings.showErrorsInstantly && mutableErrors.isNotEmpty()
    }

    fun undo() {
        checkValidState()

        if (actionsUndo.isNotEmpty()) {
            actionsUndo.last().let { action ->
                navigator.navTo(action.x, action.y)
            }
            actionsRedo.add(0, actionsUndo.removeLast())
        }

        updateBoardAndErrors()
    }

    fun redo() {
        checkValidState()

        if (actionsRedo.isNotEmpty()) {
            actionsUndo.add(actionsRedo.removeFirst())
            actionsUndo.last().let { action ->
                navigator.navTo(action.x, action.y)
            }
        }

        updateBoardAndErrors()
    }

    fun type(letter: Char) {
        checkValidState()

        if (board.letters[navigator.x, navigator.y] != letter) {
            keyCount++
        }

        actionsUndo.add(Action(navigator.x, navigator.y, letter))
        actionsRedo.clear()
        navigator.navRight()

        updateBoardAndErrors()

        if (gameSettings.showErrorsInstantly && board.isFilled) {
            // There's no need to manually submit if "showErrorsInstantly" is true
            // The user already knows if they already have errors or not
            submit(update = false)
        }
    }

    fun delete(moveLeftIfEmpty: Boolean = false) {
        checkValidState()

        // Special case handling for the very last char, since we can't move past it.
        val isOnChar = board.letters[navigator.x, navigator.y] != null

        if (moveLeftIfEmpty && !isOnChar) navigator.navLeft()
        if (board.letters[navigator.x, navigator.y] != null) {
            actionsUndo.add(Action(navigator.x, navigator.y, null))
            actionsRedo.clear()
        }

        updateBoardAndErrors()
    }

    fun row(rowIndex: Int) {
        checkValidState()

        if (rowIndex < board.numRows) {
            navigator.navTo(navigator.x, rowIndex)
        }
    }

    fun submit(update: Boolean = true) {
        checkValidState()

        if (update) {
            updateBoardAndErrors()
        }

        if (mutableErrors.isEmpty()) {
            gameState = GameState.Finished
        } else {
            mutableShowErrors.value = true
        }
    }
}
