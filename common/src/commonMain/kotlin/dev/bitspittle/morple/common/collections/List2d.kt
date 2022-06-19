package dev.bitspittle.morple.common.collections

open class List2d<T>(val list1d: List<T>, val numCols: Int) {
    val numRows get() = list1d.size / numCols
    operator fun get(x: Int, y: Int) = list1d[toIndex1d(x, y)]
    operator fun get(index: Int) = list1d[index]
    protected fun toIndex1d(x: Int, y: Int) = y * numCols + x
}

val <T> List2d<T>.lastRowIndex get() = numRows - 1

fun <T> List2d<T>.getRow(rowIndex: Int): List<T> {
    val startX = numCols * rowIndex
    return list1d.subList(startX, startX + numCols)
}

fun <T> List2d<T>.lastRow(): List<T> {
    return getRow(lastRowIndex)
}


fun <T> List2d<T>.forEachIndexed(block: (Int, Int, T) -> Unit) {
    for (y in 0 until numRows) {
        for (x in 0 until numCols) {
            block(x, y, this[x, y])
        }
    }
}

fun <T> List2d<T>.forEachRowIndexed(block: (Int, List<T>) -> Unit) {
    for (y in 0 until numRows) {
        block(y, getRow(y))
    }
}

fun <T> List2d<T>.forEachRow(block: (List<T>) -> Unit) {
    forEachRowIndexed { _, row -> block(row) }
}

fun <T, R> List2d<T>.map(transform: (T) -> R): List2d<R> {
    return List2d(list1d.map { transform(it) }, numCols)
}

fun <T> List2d<T>.toMutableList2d(): MutableList2d<T> {
    if (this is MutableList2d<T>) return this

    return MutableList2d(this.list1d.toMutableList(), this.numCols)
}

class MutableList2d<T>(private val mutableList1d: MutableList<T>, numCols: Int) : List2d<T>(mutableList1d, numCols) {
    operator fun set(x: Int, y: Int, value: T) {
        mutableList1d[toIndex1d(x, y)] = value
    }

    operator fun set(index: Int, value: T) {
        mutableList1d[index] = value
    }
}
