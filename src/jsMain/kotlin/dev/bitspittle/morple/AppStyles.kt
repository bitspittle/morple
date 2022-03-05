package dev.bitspittle.morple

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.InitSilk
import com.varabyte.kobweb.silk.InitSilkContext
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.registerBaseStyle
import kotlinx.browser.localStorage

const val COLOR_MODE_KEY = "morple:app:colorMode"

@InitSilk
fun updateTheme(ctx: InitSilkContext) {
    ctx.config.initialColorMode = localStorage.getItem(COLOR_MODE_KEY)?.let { ColorMode.valueOf(it) } ?: ColorMode.DARK

    ctx.config.registerBaseStyle("body") {
        Modifier.fontFamily("Ubuntu", "Roboto", "Arial", "Helvetica", "sans-serif")
    }

    ctx.config.registerBaseStyle(".morple-title") {
        Modifier.fontFamily("Alegreya", "sans")
    }
}

class SitePalette(
    val error: Color,
    val warning: Color,
    val tile: Tile,
    val key: Key,
    val bs: Color,
) {
    class Tile(
        val border: Color,
        val absent: Color,
        val present: Color,
        val match: Color,
        val focused: Color,
        val text: Color,
        val lock: Color,
    )

    class Key(
        val bg: Color,
        val fg: Color
    )
}

object SitePalettes {
    private val sitePalettes = mapOf(
        ColorMode.LIGHT to SitePalette(
            error = Colors.Red,
            warning = Colors.Yellow,
            SitePalette.Tile(
                border = Color.rgb(0xd3d6da),
                absent = Color.rgb(0x787c73),
                present = Color.rgb(0xc9b458),
                match = Color.rgb(0x6aaa64),
                focused = Colors.Black,
                text = Colors.White,
                lock = Colors.White,
            ),
            SitePalette.Key(
                bg = Color.rgb(0xd3d6da),
                fg = Colors.Black,
            ),
            bs = Color.rgb(0x009900),
        ),
        ColorMode.DARK to SitePalette(
            error = Colors.Red,
            warning = Colors.Yellow,
            SitePalette.Tile(
                border = Color.rgb(0x818384),
                absent = Color.rgb(0x3a3a3c),
                present = Color.rgb(0xb59f3b),
                match = Color.rgb(0x538d4e),
                focused = Colors.White,
                text = Colors.White,
                lock = Colors.White,
            ),
            SitePalette.Key(
                bg = Color.rgb(0x818384),
                fg = Colors.White,
            ),
            bs = Color.rgb(0x04f904),
        ),
    )

    operator fun get(colorMode: ColorMode) = sitePalettes.getValue(colorMode)
}

fun ColorMode.toSitePalette() = SitePalettes[this]