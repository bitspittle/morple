package dev.bitspittle.morple.common.board

data class Pt(val x: Int, val y: Int) {
    constructor() : this(0, 0)
}

fun Pair<Int, Int>.toPt() = Pt(first, second)