package dev.bitspittle.morple

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.InitSilk
import com.varabyte.kobweb.silk.InitSilkContext
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.registerBaseStyle
import dev.bitspittle.morple.data.Tile
import kotlinx.browser.localStorage

const val COLOR_MODE_KEY = "morple:app:colorMode"

private val TEXT_FONT = Modifier
    .fontFamily("Ubuntu", "Roboto", "Arial", "Helvetica", "sans-serif")

@InitSilk
fun updateTheme(ctx: InitSilkContext) {
    ctx.config.initialColorMode = localStorage.getItem(COLOR_MODE_KEY)?.let { ColorMode.valueOf(it) } ?: ColorMode.DARK

    ctx.config.registerBaseStyle("body") { TEXT_FONT.lineHeight(1.5) }
}

class SitePalette(
    val tile: Tile
) {
    class Tile(
        val absent: Color,
        val present: Color,
        val match: Color
    )
}

object SitePalettes {
    private val sitePalettes = mapOf(
        ColorMode.LIGHT to SitePalette(
            SitePalette.Tile(
                absent = Color.rgb(0x787c73),
                present = Color.rgb(0xc9b458),
                match = Color.rgb(0x6aaa64)
            )
        ),
        ColorMode.DARK to SitePalette(
            SitePalette.Tile(
                absent = Color.rgb(0x3a3a3c),
                present = Color.rgb(0xb59f3b),
                match = Color.rgb(0x538d4e)
            )
        ),
    )

    operator fun get(colorMode: ColorMode) = sitePalettes.getValue(colorMode)
}

fun ColorMode.toSitePalette() = SitePalettes[this]