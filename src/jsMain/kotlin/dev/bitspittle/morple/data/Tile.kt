package dev.bitspittle.morple.data

enum class TileState {
    ABSENT,
    PRESENT,
    MATCH
}

data class Tile(val state: TileState, val letter: Char?)