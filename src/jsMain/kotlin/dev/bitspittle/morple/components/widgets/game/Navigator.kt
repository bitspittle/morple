package dev.bitspittle.morple.components.widgets.game

class Navigator(
    private val numRows: Int,
    private val numCols: Int,
    private val getPos: () -> Pair<Int, Int>,
    private val setFocus: (Int, Int) -> Unit,
) {
    val maxX get() = numCols - 1
    val maxY get() = numRows - 1

    val x get() = getPos().first
    val y get() = getPos().second

    fun navTo(x: Int, y: Int) = setFocus(x, y)

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
