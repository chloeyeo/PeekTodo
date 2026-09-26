package com.chloeyeo.peektodo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Every colour in the app, in one place. The first block is sampled pixel
 * for pixel from design/shiba_wink.png; the derived block adds the few
 * shades needed for WCAG AA text contrast and a warm dark theme.
 */
object Shiba {
    // Sampled from the icon
    val Khaki = Color(0xFFF3E2B3)       // icon background, app ground
    val Clipboard = Color(0xFFFFFBF3)   // clipboard, cards
    val Cream = Color(0xFFFFF1DF)       // shiba muzzle, checkbox fill
    val Orange = Color(0xFFE4A263)      // shiba fur, primary accent
    val Ink = Color(0xFF3A332E)         // outlines, text
    val Pencil = Color(0xFF8FAECF)      // pencil body, secondary accent
    val Tick = Color(0xFF4E78A6)        // checkbox ticks
    val Eraser = Color(0xFFF1A7A0)      // pencil eraser, error container
    val Ferrule = Color(0xFFD9D2C7)     // pencil ferrule, dividers
    val LineGrey = Color(0xFFC8C3BC)    // clipboard task lines
    val Wood = Color(0xFFF3D9B1)        // sharpened pencil wood

    // Derived, light theme
    val InkMuted = Color(0xFF6B5F55)    // secondary text on Khaki, 5.5:1
    val TickDeep = Color(0xFF3E6690)    // link/emphasis text on Clipboard, 5.9:1
    val PencilPale = Color(0xFFDCE6F2)  // secondary container
    val ErrorDeep = Color(0xFFA33D36)   // error text on Clipboard, 6.4:1

    // Derived, dark theme (same warmth, lower brightness)
    val DarkGround = Color(0xFF2A241F)  // ground
    val DarkCard = Color(0xFF3A332E)    // cards (the ink colour, inverted role)
    val DarkCardRaised = Color(0xFF4A423B)
    val DarkOutline = Color(0xFF7A6D62) // thin card outlines on dark
    val DarkInk = Color(0xFFF6EBD4)     // primary text on dark, 13:1
    val DarkInkMuted = Color(0xFFC9BDA9) // secondary text on dark, 8.5:1
    val OrangeDeep = Color(0xFF6B4A2A)  // primary container on dark
    val PencilDeep = Color(0xFF3C5470)  // secondary container on dark
}

/** Colours the Material scheme has no slot for: the clipboard-style card and its outline, and the tick. */
@Immutable
data class ExtendedColors(
    val card: Color,
    val cardOutline: Color,
    val checkboxFill: Color,
    val checkboxOutline: Color,
    val tick: Color,
    val taskLine: Color,
)

val LightExtendedColors = ExtendedColors(
    card = Shiba.Clipboard,
    cardOutline = Shiba.Ink,
    checkboxFill = Shiba.Cream,
    checkboxOutline = Shiba.Ink,
    tick = Shiba.Tick,
    taskLine = Shiba.LineGrey,
)

val DarkExtendedColors = ExtendedColors(
    card = Shiba.DarkCard,
    cardOutline = Shiba.DarkOutline,
    checkboxFill = Shiba.DarkCardRaised,
    checkboxOutline = Shiba.DarkInkMuted,
    tick = Shiba.Pencil,
    taskLine = Shiba.DarkOutline,
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
