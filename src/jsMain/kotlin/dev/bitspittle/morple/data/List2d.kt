package dev.bitspittle.morple.data

open class List2d<T>(val list1d: List<T>, private val numCols: Int) {
    operator fun get(x: Int, y: Int) = list1d[toIndex1d(x, y)]

    protected fun toIndex1d(x: Int, y: Int) = y * numCols + x
}

class MutableList2d<T>(private val mutableList1d: MutableList<T>, numCols: Int) : List2d<T>(mutableList1d, numCols) {
    operator fun set(x: Int, y: Int, value: T) {
        mutableList1d[toIndex1d(x, y)] = value
    }
}