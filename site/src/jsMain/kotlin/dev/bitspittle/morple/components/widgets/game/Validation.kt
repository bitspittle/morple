package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.fa.FaCircleExclamation
import com.varabyte.kobweb.silk.components.style.*
import dev.bitspittle.morple.toSitePalette
import org.jetbrains.compose.web.css.*

val ValidationStyle = ComponentStyle.base("morple-validation") {
    Modifier
        .color(colorMode.toSitePalette().error)
        .fontSize(1.5.cssRem)
        .padding(left = 1.cssRem)
        .display(DisplayStyle.None)
}

val ShowValidationVariant = ValidationStyle.addVariantBase("show") {
    Modifier.display(DisplayStyle.Unset)
}

@Composable
fun Validation(modifier: Modifier = Modifier, variant: ComponentVariant? = null) {
    FaCircleExclamation(ValidationStyle.toModifier(variant).then(modifier))
}