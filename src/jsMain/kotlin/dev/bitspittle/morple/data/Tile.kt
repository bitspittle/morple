package dev.bitspittle.morple.data

enum class TileState {
    ABSENT,
    PRESENT,
    MATCH
}

class Tile(val state: TileState)