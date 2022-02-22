package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
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

val FILLED_TILE_STYLE = Modifier
    .color(Colors.White)
    .border("0px")

val TileStyle = ComponentStyle("morple-tile") {
    base {
        Modifier
            .margin(2.px)
            .fontSize(2.cssRem)
            .fontWeight(FontWeight.Bolder)
            .textAlign(TextAlign.Center)
            .size(4.cssRem)
            .color(if (colorMode.isLight()) Colors.Black else Colors.Gray)
            .border(2.px, LineStyle.Solid, colorMode.toSitePalette().tile.absent.toCssColor())
    }
}

val AbsentTileStyle = TileStyle.addBaseVariant("absent") {
    FILLED_TILE_STYLE
        .backgroundColor(colorMode.toSitePalette().tile.absent.toCssColor())
}

val PresentTileStyle = TileStyle.addBaseVariant("present") {
    FILLED_TILE_STYLE
        .backgroundColor(colorMode.toSitePalette().tile.present.toCssColor())
}

val MatchTileStyle = TileStyle.addBaseVariant("match") {
    FILLED_TILE_STYLE
        .backgroundColor(colorMode.toSitePalette().tile.match.toCssColor())
}

@Composable
fun Tile(letter: Char? = null, modifier: Modifier = Modifier, variant: ComponentVariant? = null) {
    Box(TileStyle.toModifier(variant).then(modifier)) {
        if (letter != null) {
            Text(letter.uppercaseChar().toString(), Modifier.align(Alignment.Center))
        }
    }
}