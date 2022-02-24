package dev.bitspittle.morple.components.widgets.game

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.toCssColor
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.morple.toSitePalette
import org.jetbrains.compose.web.css.*

val FinishedBoardStyle = ComponentStyle.base("morple-finished-board") {
    Modifier.outline(width = 4.px, LineStyle.Double, colorMode.toSitePalette().finished.toCssColor())
}

val ErrorRowStyle = ComponentStyle.base("morple-error-row") {
    Modifier.outline(width = 1.px, LineStyle.Solid, colorMode.toSitePalette().error.toCssColor())
}
