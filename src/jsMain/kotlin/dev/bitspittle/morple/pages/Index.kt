package dev.bitspittle.morple.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import dev.bitspittle.morple.components.layout.PageLayout
import dev.bitspittle.morple.components.widgets.game.AbsentTileStyle
import dev.bitspittle.morple.components.widgets.game.MatchTileStyle
import dev.bitspittle.morple.components.widgets.game.PresentTileStyle
import dev.bitspittle.morple.components.widgets.game.Tile

val stylechoices = listOf(null, AbsentTileStyle, PresentTileStyle, MatchTileStyle)

@Page
@Composable
fun HomePage() {
    PageLayout("Morple", description = "Wordle... but upside-down!") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
                (0 until 6).forEach { y ->
                    Row {
                        (0 until 5).forEach { x ->
                            Tile(('A'.code + x).toChar(), variant = stylechoices.random())
                        }
                    }
                }
            }
        }
    }
}
