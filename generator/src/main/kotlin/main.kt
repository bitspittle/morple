import dev.bitspittle.morple.generator.BoardGenerator
import dev.bitspittle.morple.generator.generatePuzzle
import java.io.File

private class DummyForClassLoader

fun main() {
    val moduleClassloader = DummyForClassLoader::class.java.classLoader
    val commonWords = moduleClassloader.getResourceAsStream("common-words.txt")!!
        .readAllBytes().toString(Charsets.UTF_8)
        .split("\n")
        .toSet()

    val allWords = moduleClassloader.getResourceAsStream("words.txt")!!
        .readAllBytes().toString(Charsets.UTF_8)
        .split("\n")
        .toSet()

    val generator = BoardGenerator(allWords, commonWords)
    val board = generator.generate()
    print(board)
    print(board.generatePuzzle(allWords))
}
