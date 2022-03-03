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
import com.varabyte.kobweb.silk.components.icons.fa.FaLock
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.Text
import dev.bitspittle.morple.toSitePalette
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement

private val ComponentModifier.FILLED_TILE_STYLE get() = Modifier
    .color(colorMode.toSitePalette().tile.text)
    .border("0px")
    .cursor(Cursor.Pointer)

val TileStyle = ComponentStyle.base("morple-tile") {
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
        .outlineWidth(0.px) // Default focus has a thin black line, so disable it
}

val LockStyle = ComponentStyle.base("morple-lock") {
    Modifier
        .fontSize(0.7.cssRem)
        .padding(0.3.cssRem)
        .color(colorMode.toSitePalette().tile.lock)
}

val AbsentTileVariant = TileStyle.addVariantBase("absent") {
    FILLED_TILE_STYLE.backgroundColor(colorMode.toSitePalette().tile.absent.toCssColor())
}

val PresentTileVariant = TileStyle.addVariantBase("present") {
    FILLED_TILE_STYLE.backgroundColor(colorMode.toSitePalette().tile.present.toCssColor())
}

val MatchTileVariant = TileStyle.addVariantBase("match") {
    FILLED_TILE_STYLE.backgroundColor(colorMode.toSitePalette().tile.match.toCssColor())
}

val LetterErrorTileVariant = TileStyle.addVariantBase("error-letter") {
    Modifier.color(colorMode.toSitePalette().error.toCssColor())
}

val LetterWarningTileVariant = TileStyle.addVariantBase("warn-letter") {
    Modifier.color(colorMode.toSitePalette().warning.toCssColor())
}

// Changing the text color of empty tiles is useless... so outline them instead!
val EmptyErrorTileVariant = TileStyle.addVariantBase("error-empty") {
    Modifier.outline(width = 1.px, LineStyle.Solid, colorMode.toSitePalette().error.toCssColor())
}

val FocusedTileVariant = TileStyle.addVariantBase("focused") {
    Modifier.outline(width = 4.px, LineStyle.Solid, colorMode.toSitePalette().tile.focused.toCssColor())
}

@Composable
fun Tile() {
    Tile(null, false)
}

@Composable
fun Tile(letter: Char?, locked: Boolean, modifier: Modifier = Modifier, variant: ComponentVariant? = null, elementScope: (@Composable ElementScope<HTMLElement>.() -> Unit)? = null) {
    // Variants are used to indicate what kind of active tile this is -- otherwise, it's just an empty, non-focusable
    // tile
    val focusable = Modifier.tabIndex(0).takeIf { variant != null } ?: Modifier
    Box(TileStyle.toModifier(variant).then(modifier).then(focusable), elementScope = elementScope) {
        if (letter != null) {
            if (locked) {
                FaLock(LockStyle.toModifier().then(Modifier.align(Alignment.TopStart)))
            }
            Text(letter.uppercaseChar().toString(), Modifier.align(Alignment.Center))
        }
    }
}