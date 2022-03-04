package dev.bitspittle.morple.components.widgets.overlay

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.BoxScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import org.jetbrains.compose.web.css.*

val ModalStyle = ComponentStyle.base("morple-modal") {
    Modifier
        .position(Position.Absolute)
        .top(0.px)
        .right(0.px)
        .bottom(0.px)
        .left(0.px)
        .backgroundColor(Colors.Black.copyf(alpha = 0.7f))
        .zIndex(1000) // TODO: Add z-index constants for various categories
}

val ModalContentStyle = ComponentStyle("morple-modal-content") {
    base {
        Modifier
            .fillMaxWidth()
            .maxWidth(80.percent)
            .height(60.percent)
            .margin(top = 20.percent)
            .overflowY(Overflow.Auto)
            .padding(1.cssRem)
    }

    Breakpoint.MD {
        Modifier
            .maxWidth(600.px)
    }
}

@Composable
fun Modal(
    modalVariant: ComponentVariant? = null,
    modalContentVariant: ComponentVariant? = null,
    onCloseRequested: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(ModalStyle.toModifier(modalVariant).onClick { evt ->
        evt.preventDefault()
        onCloseRequested()
    }, contentAlignment = Alignment.TopCenter) {
        Box(ModalContentStyle.toModifier(modalContentVariant)) {
            content()
        }
    }
}
