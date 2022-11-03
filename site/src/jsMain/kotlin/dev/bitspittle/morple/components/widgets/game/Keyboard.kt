package dev.bitspittle.morple.components.widgets.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.UserSelect
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.fa.FaDeleteLeft
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.style.breakpoint.Breakpoint
import dev.bitspittle.morple.common.board.TileState
import dev.bitspittle.morple.common.collections.forEachIndexed
import dev.bitspittle.morple.data.GameBoard
import dev.bitspittle.morple.data.GameSettings
import dev.bitspittle.morple.toSitePalette
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

sealed interface KeyAction {
    class Type(val letter: Char) : KeyAction
    object Submit : KeyAction
    object Backspace : KeyAction
    object Undo : KeyAction
    object Redo : KeyAction
}

private val KEYBOARD_LAYOUT = listOf(
    "QWERTYUIOP".map { c -> KeyAction.Type(c) }.toList(),
    "ASDFGHJKL".map { c -> KeyAction.Type(c) }.toList(),
    listOf(KeyAction.Submit) + "ZXCVBNM".map { c -> KeyAction.Type(c) }.toList() + listOf(KeyAction.Backspace)
)

val KeyboardStyle = ComponentStyle("morple-keyboard") {
    base {
        Modifier
            .margin(top = 1.cssRem)
            .fillMaxWidth()
            .fontWeight(FontWeight.Bold)
            .fontSize(0.8.cssRem)
    }

    Breakpoint.SM { Modifier.fontSize(1.cssRem) }
}

val KeyStyle = ComponentStyle("morple-key") {
    base {
        Modifier
            .backgroundColor(colorMode.toSitePalette().key.bg)
            .color(colorMode.toSitePalette().key.fg)
            .margin(2.px)
            .maxWidth(2.5.cssRem)
            .width(8.percent)
            .lineHeight(3.5.cssRem)
            .height(3.5.cssRem)
            .textAlign(TextAlign.Center)
            .borderRadius(5.px)
            // Don't allow drag-highlighting key letters!
            .userSelect(UserSelect.None)
            .cursor(Cursor.Pointer)
    }

    Breakpoint.SM { Modifier.margin(3.px).width(10.percent) }
}

val ControlKeyVariant = KeyStyle.addVariant("control") {
    base { Modifier.maxWidth(5.cssRem).width(12.percent) }
    Breakpoint.SM { Modifier.width(20.percent) }
}

private val ComponentModifier.FILLED_KEY_STYLE get() = Modifier
    .color(colorMode.toSitePalette().tile.text)

val AbsentKeyVariant = KeyStyle.addVariantBase("absent") {
    FILLED_KEY_STYLE
        .backgroundColor(colorMode.toSitePalette().tile.absent)
}

val RepetetiveKeyVariant = KeyStyle.addVariantBase("repetitive") {
    Modifier.color(colorMode.toSitePalette().warning)
}

val PresentKeyVariant = KeyStyle.addVariantBase("present") {
    FILLED_KEY_STYLE
        .backgroundColor(colorMode.toSitePalette().tile.present)
}

val MatchKeyVariant = KeyStyle.addVariantBase("match") {
    FILLED_KEY_STYLE
        .backgroundColor(colorMode.toSitePalette().tile.match)
}

val DisabledButtonStyle = ComponentStyle.base("morple-button-disabled") {
    Modifier.opacity(50.percent)
}

val HiddenStyle = ComponentStyle.base("morple-hidden") {
    Modifier.opacity(0.percent)
}

@Composable
private fun Key(tileStates: Map<Char, TileState>, action: KeyAction, autoSubmit: Boolean, onKeyPressed: (KeyAction) -> Unit) {
    if (action is KeyAction.Type) {
        val keyVariant = when (tileStates[action.letter]) {
            TileState.ABSENT -> AbsentKeyVariant
            TileState.PRESENT -> PresentKeyVariant
            TileState.MATCH -> MatchKeyVariant
            null -> null
        }

        Div(KeyStyle.toModifier(keyVariant).asAttributesBuilder {
            onClick { onKeyPressed(action) }
        }) {
            Text(action.letter.toString())
        }
    } else if (action is KeyAction.Submit) {
        Div(
            KeyStyle.toModifier(ControlKeyVariant)
                .then(DisabledButtonStyle.takeIf { autoSubmit }?.toModifier() ?: Modifier)
                .then(Modifier.onClick { if (!autoSubmit) onKeyPressed(action) })
                .asAttributesBuilder()) {
            Text("CHECK")
        }
    } else if (action is KeyAction.Backspace) {
        Div(KeyStyle.toModifier(ControlKeyVariant).asAttributesBuilder {
            onClick { onKeyPressed(action) }
        }) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                FaDeleteLeft()
            }
        }
    }
}

@Composable
fun Keyboard(
    gameSettings: GameSettings,
    board: GameBoard,
    onKeyPressed: (KeyAction) -> Unit,
    forceInvalidationWhenBoardChanges: () -> Unit,
) {
    forceInvalidationWhenBoardChanges()

    val tileStates = mutableMapOf<Char, TileState>()
    board.tiles.forEachIndexed { x, y, tileState ->
        when (tileState) {
            // Absent characters don't override other states
            TileState.ABSENT -> board.letters[x, y]?.let { c ->
                if (!tileStates.containsKey(c)) {
                    tileStates[c] = TileState.ABSENT
                }
            }
            // Present characters don't overwrite matches
            TileState.PRESENT -> board.letters[x, y]?.let { c ->
                if (!tileStates.containsKey(c) || tileStates[c] == TileState.ABSENT) {
                    tileStates[c] = TileState.PRESENT
                }
            }
            // Matches always get added
            TileState.MATCH -> board.letters[x, y]?.let { c -> tileStates[c] = TileState.MATCH }
        }
    }

    Column(KeyboardStyle.toModifier(), horizontalAlignment = Alignment.CenterHorizontally) {
        KEYBOARD_LAYOUT.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                row.forEach { keyAction ->
                    Key(tileStates, keyAction, gameSettings.showErrorsInstantly, onKeyPressed)
                }
            }
        }
    }
}
