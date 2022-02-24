package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.toCssColor
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.morple.toSitePalette
import org.jetbrains.compose.web.css.*

val KeyboardStyle = ComponentStyle.base("morple-keyboard") {
    Modifier.outline(width = 4.px, LineStyle.Double, colorMode.toSitePalette().finished.toCssColor())
}

@Composable
fun Keyboard(onKeyPressed: (Char) -> Unit, modifier: Modifier = Modifier) {

}
