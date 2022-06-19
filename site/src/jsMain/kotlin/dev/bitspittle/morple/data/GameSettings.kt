package dev.bitspittle.morple.data

class GameSettings(
    val showErrorsInstantly: Boolean,
) {
    companion object {
        fun from(mode: GameMode): GameSettings {
            return when(mode) {
                GameMode.EASY -> GameSettings(
                    showErrorsInstantly = true,
                )
                GameMode.NORMAL -> GameSettings(
                    showErrorsInstantly = false,
                )
                GameMode.HARD -> GameSettings(
                    showErrorsInstantly = false,
                )
            }
        }
    }
}