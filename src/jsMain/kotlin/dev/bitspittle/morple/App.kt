package dev.bitspittle.morple

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.theme.colors.getColorMode
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.*

@App
@Composable
fun MorpleApp(content: @Composable () -> Unit) {
    SilkApp {
        val colorMode = getColorMode()
        LaunchedEffect(colorMode) {
            localStorage.setItem(COLOR_MODE_KEY, colorMode.name)
        }

        Surface(Modifier.minWidth(100.vw).minHeight(100.vh)) {
            content()
        }
    }
}
