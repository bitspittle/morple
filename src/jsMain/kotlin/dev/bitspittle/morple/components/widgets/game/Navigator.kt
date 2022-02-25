package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import dev.bitspittle.morple.data.List2d
import org.w3c.dom.HTMLElement

class Navigator(
    private val elements: List2d<HTMLElement>,
    mutableActiveTile: MutableState<Pair<Int, Int>>
) {
    private var activeTile by mutableActiveTile

    val maxX get() = elements.numCols - 1
    val maxY get() = elements.numRows - 1

    val x get() = activeTile.first
    val y get() = activeTile.second

    fun navTo(x: Int, y: Int) {
        activeTile = x to y
        elements[x, y].focus()
    }

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
