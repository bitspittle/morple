package dev.bitspittle.morple.data

open class List2d<T>(val list1d: List<T>, val numCols: Int) {
    val numRows get() = list1d.size / numCols

    operator fun get(x: Int, y: Int) = list1d[toIndex1d(x, y)]

    protected fun toIndex1d(x: Int, y: Int) = y * numCols + x
}

fun <T> List2d<T>.forEachIndexed(block: (Int, Int, T) -> Unit) {
    for (y in 0 until numRows) {
        for (x in 0 until numCols) {
            block(x, y, this[x, y])
        }
    }
}

class MutableList2d<T>(private val mutableList1d: MutableList<T>, numCols: Int) : List2d<T>(mutableList1d, numCols) {
    operator fun set(x: Int, y: Int, value: T) {
        mutableList1d[toIndex1d(x, y)] = value
    }
}
