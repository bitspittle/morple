package dev.bitspittle.morple.data

class GameSettings(
    val maxAbsentRepetitionPercent: Float,
    val autoFillMatchColumns: Boolean,
    val showErrorsInstantly: Boolean,
) {
    companion object {
        fun from(mode: GameMode): GameSettings {
            return when(mode) {
                GameMode.EASY -> GameSettings(
                    maxAbsentRepetitionPercent = 0.4f,
                    autoFillMatchColumns = true,
                    showErrorsInstantly = true,
                )
                GameMode.NORMAL -> GameSettings(
                    maxAbsentRepetitionPercent = 0.2f,
                    autoFillMatchColumns = true,
                    showErrorsInstantly = false,
                )
                GameMode.HARD -> GameSettings(
                    maxAbsentRepetitionPercent = 0.2f,
                    autoFillMatchColumns = false,
                    showErrorsInstantly = false,
                )
            }
        }
    }
}