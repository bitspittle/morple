package dev.bitspittle.morple.components.widgets.game

sealed interface GameState {
    object Normal : GameState
    object Finished : GameState
}
