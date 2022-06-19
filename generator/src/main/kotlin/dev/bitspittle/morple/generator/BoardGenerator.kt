package dev.bitspittle.morple.generator

import dev.bitspittle.morple.common.collections.List2d

class BoardGenerator(private val allWords: Set<String>, private val commonWords: Set<String>) {
    fun generate(): Board {
        val tiles = List2d(commonWords.toMutableSet().let { wordsCopy ->
            Array(6) {
                wordsCopy.random().also {
                    wordsCopy.remove(it)
                }
            }
                .toList()
                .flatMap { s -> s.toCharArray().toList() }
        }, 5)

        return Board(tiles)
    }
}