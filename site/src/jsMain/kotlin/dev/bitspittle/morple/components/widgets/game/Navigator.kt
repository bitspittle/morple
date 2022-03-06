package dev.bitspittle.morple.components.widgets.game

import dev.bitspittle.morple.common.board.Pt

class Navigator(
    private val numRows: Int,
    private val numCols: Int,
    private val getPos: () -> Pt,
    private val setFocus: (Pt) -> Unit,
) {
    val maxX get() = numCols - 1
    val maxY get() = numRows - 1

    val x get() = getPos().x
    val y get() = getPos().y

    fun navTo(x: Int, y: Int) = setFocus(Pt(x, y))

    fun navUp() {
        if (y > 0) navTo(x, y - 1)
    }

    fun navDown() {
        if (y < maxY) navTo(x, y + 1)
    }

    fun navLeft() {
        if (x > 0) navTo(x - 1, y)
        else if (y > 0) navTo(maxX, y - 1)
    }

    fun navRight() {
        if (x < maxX) navTo(x + 1, y)
        else if (y < maxY) navTo(0, y + 1)
    }

    fun navHome() {
        if (x > 0) navTo(0, y)
    }

    fun navEnd() {
        if (x < maxX) navTo(maxX, y)
    }
}
