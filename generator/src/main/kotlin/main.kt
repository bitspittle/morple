import dev.bitspittle.morple.common.collections.List2d

fun main() {
//    val moduleClassloader = BoardGenerator::class.java.classLoader
//    val words = moduleClassloader.getResourceAsStream("words.txt")!!
//        .readAllBytes().toString(Charsets.UTF_8)
//        .split("\n")
//        .toSet()

    // Test List2d was imported
    val ints = List2d(listOf(1, 2, 3, 4), 2)
    for (y in 0 until ints.numRows) {
        for (x in 0 until ints.numCols) {
            print(ints[x, y])
            print(" ")
        }
        println()
    }
}