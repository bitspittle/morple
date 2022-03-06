package dev.bitspittle.morple.components.layout

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.ColumnScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.Text
import dev.bitspittle.morple.components.sections.Footer
import dev.bitspittle.morple.components.sections.Header
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H1

val CenterColumnStyle = ComponentStyle("morple-center-column") {
    base { Modifier.fillMaxSize() }
}

@Composable
fun PageLayout(title: String, description: String = "", extraAction: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    LaunchedEffect(title) {
        document.title = title
        document.querySelector("""meta[name="description"]""")!!.setAttribute("content", description)
    }

    Box(Modifier
        .fillMaxWidth()
        .minHeight(100.percent)
        // Create a box with two rows: the main content (fills as much space as it can) and the footer (which reserves
        // space at the bottom). "auto" means the use the height of the row. "1fr" means give the rest of the space to
        // that row. Since this box is set to *at least* 100%, the footer will always appear at least on the bottom but
        // can be pushed further down if the first row grows beyond the page.
        .gridTemplateRows("1fr auto"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize().maxWidth(550.px).align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(extraAction)
            Column(CenterColumnStyle.toModifier()) {
                content()
            }
        }
        // Associate the footer with the row that will get pushed off the bottom of the page if it can't fit.
        Footer(Modifier.gridRowStart(2).gridRowEnd(3))
    }
}