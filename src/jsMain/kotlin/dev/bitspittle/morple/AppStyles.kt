package dev.bitspittle.morple

import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.silk.theme.colors.ColorMode

class SitePalette(
    val brand: Color
)

object SitePalettes {
    private val sitePalettes = mapOf(
        ColorMode.LIGHT to SitePalette(brand = Color.rgb(0x009900)),
        ColorMode.DARK to SitePalette(brand = Color.rgb(0x04f904)),
    )

    operator fun get(colorMode: ColorMode) = sitePalettes.getValue(colorMode)
}