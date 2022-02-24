package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.UserSelect
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.toCssColor
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.Text
import dev.bitspittle.morple.toSitePalette
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement

val FILLED_TILE_STYLE = Modifier
    .color(Colors.White)
    .border("0px")
    .cursor(Cursor.Pointer)

private fun ComponentModifiers.addHoverFocusStates() {
    val hovered = Modifier.outlineWidth(0.px)
    val focused = Modifier
        .border(width = 4.px, LineStyle.Solid, colorMode.toSitePalette().tile.focused.toCssColor())
        .outlineWidth(0.px) // Default focus has a thin black line, so disable it

    focus { focused }
    hover { hovered }
}

val TileStyle = ComponentStyle("morple-tile") {
    base {
        Modifier
            .margin(2.px)
            .fontSize(2.cssRem)
            .fontWeight(FontWeight.Bolder)
            .textAlign(TextAlign.Center)
            .size(4.cssRem)
            .color(if (colorMode.isLight()) Colors.Black else Colors.Gray)
            .border(2.px, LineStyle.Solid, colorMode.toSitePalette().tile.border.toCssColor())
            // Don't allow drag-highlighting tile letters!
            .userSelect(UserSelect.None)
    }
}

val AbsentTileVariant = TileStyle.addVariant("absent") {
    base { FILLED_TILE_STYLE.backgroundColor(colorMode.toSitePalette().tile.absent.toCssColor()) }
    addHoverFocusStates()
}

val PresentTileVariant = TileStyle.addVariant("present") {
    base { FILLED_TILE_STYLE.backgroundColor(colorMode.toSitePalette().tile.present.toCssColor()) }
    addHoverFocusStates()
}

val MatchTileVariant = TileStyle.addVariant("match") {
    base { FILLED_TILE_STYLE.backgroundColor(colorMode.toSitePalette().tile.match.toCssColor()) }
    addHoverFocusStates()
}

val ErrorTileVariant = TileStyle.addVariantBase("error-tile") {
    Modifier.color(colorMode.toSitePalette().error.toCssColor())
}

@Composable
fun Tile(letter: Char? = null, modifier: Modifier = Modifier, variant: ComponentVariant? = null, elementScope: (@Composable ElementScope<HTMLElement>.() -> Unit)? = null) {
    val focusable = Modifier.tabIndex(0).takeIf { variant != null } ?: Modifier
    Box(TileStyle.toModifier(variant).then(modifier).then(focusable), elementScope = elementScope) {
        if (letter != null) {
            Text(letter.uppercaseChar().toString(), Modifier.align(Alignment.Center))
        }
    }
}