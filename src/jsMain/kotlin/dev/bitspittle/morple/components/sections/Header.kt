package dev.bitspittle.morple.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.Text
import com.varabyte.kobweb.silk.theme.toSilkPalette
import dev.bitspittle.morple.components.widgets.button.ColorModeButton
import org.jetbrains.compose.web.css.*

val HeaderStyle = ComponentStyle.base("morple-header") {
    Modifier
        .fillMaxWidth()
        .padding(0.5.cssRem)
        .fontSize(1.25.cssRem)
        .textAlign(TextAlign.Center)
}

val MorpleTextStyle = ComponentStyle.base("morple-text") {
    Modifier
        .fontSize(2.cssRem)
        .fontWeight(FontWeight.Bold)
}

val VersionTextStyle = ComponentStyle.base("morple-version") {
    Modifier
        .fontSize(0.6.cssRem)
        .textAlign(TextAlign.Center)
        .margin(top = (-1).em)
}

val HeaderButtonStyle = ComponentStyle.base("morple-header-button") {
    Modifier
        .margin(0.px, 10.px).backgroundColor(colorMode.toSilkPalette().background)
}

@Composable
fun Header() {
    Box(HeaderStyle.toModifier()) {
        Column(Modifier.align(Alignment.Center)) {
            Text("Morple", MorpleTextStyle.toModifier())
            Text("v" + AppGlobals.getValue("version"), VersionTextStyle.toModifier().fillMaxWidth())
        }
        ColorModeButton(HeaderButtonStyle.toModifier().align(Alignment.CenterEnd))
    }
}