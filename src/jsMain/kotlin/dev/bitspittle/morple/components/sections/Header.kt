package dev.bitspittle.morple.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.Text
import com.varabyte.kobweb.silk.theme.toSilkPalette
import dev.bitspittle.morple.components.widgets.button.ColorModeButton
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

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

val HeaderButtonStyle = ComponentStyle.base("morple-nav-button") {
    Modifier.margin(0.px, 10.px).backgroundColor(colorMode.toSilkPalette().background)
}

@Composable
fun Header() {
    Column(HeaderStyle.toModifier()) {
        Row(Modifier.fillMaxWidth()) {
            Spacer()
            ColorModeButton(HeaderButtonStyle.toModifier())
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Morple", MorpleTextStyle.toModifier())
        }
    }
}