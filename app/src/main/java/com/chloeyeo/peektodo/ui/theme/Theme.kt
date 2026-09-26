package com.chloeyeo.peektodo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Theme built from the "Shiba wink" icon: khaki ground, clipboard-white
 * cards with thin ink outlines, orange-tan primary, pencil-blue secondary.
 * Dynamic colour is deliberately off so the app always matches its icon.
 */

private val LightColors = lightColorScheme(
    primary = Shiba.Orange,
    onPrimary = Shiba.Ink,
    primaryContainer = Shiba.Wood,
    onPrimaryContainer = Shiba.Ink,
    secondary = Shiba.TickDeep,
    onSecondary = Shiba.Clipboard,
    secondaryContainer = Shiba.PencilPale,
    onSecondaryContainer = Shiba.Ink,
    tertiary = Shiba.Eraser,
    onTertiary = Shiba.Ink,
    background = Shiba.Khaki,
    onBackground = Shiba.Ink,
    surface = Shiba.Khaki,
    onSurface = Shiba.Ink,
    surfaceVariant = Shiba.Clipboard,
    onSurfaceVariant = Shiba.InkMuted,
    surfaceContainer = Shiba.Clipboard,
    surfaceContainerLow = Shiba.Clipboard,
    surfaceContainerHigh = Shiba.Cream,
    surfaceContainerHighest = Shiba.Cream,
    outline = Shiba.Ink,
    outlineVariant = Shiba.Ferrule,
    error = Shiba.ErrorDeep,
    onError = Shiba.Clipboard,
    errorContainer = Shiba.Eraser,
    onErrorContainer = Shiba.Ink,
)

private val DarkColors = darkColorScheme(
    primary = Shiba.Orange,
    onPrimary = Shiba.Ink,
    primaryContainer = Shiba.OrangeDeep,
    onPrimaryContainer = Shiba.DarkInk,
    secondary = Shiba.Pencil,
    onSecondary = Shiba.Ink,
    secondaryContainer = Shiba.PencilDeep,
    onSecondaryContainer = Shiba.DarkInk,
    tertiary = Shiba.Eraser,
    onTertiary = Shiba.Ink,
    background = Shiba.DarkGround,
    onBackground = Shiba.DarkInk,
    surface = Shiba.DarkGround,
    onSurface = Shiba.DarkInk,
    surfaceVariant = Shiba.DarkCard,
    onSurfaceVariant = Shiba.DarkInkMuted,
    surfaceContainer = Shiba.DarkCard,
    surfaceContainerLow = Shiba.DarkCard,
    surfaceContainerHigh = Shiba.DarkCardRaised,
    surfaceContainerHighest = Shiba.DarkCardRaised,
    outline = Shiba.DarkOutline,
    outlineVariant = Shiba.DarkOutline,
    error = Shiba.Eraser,
    onError = Shiba.Ink,
    errorContainer = Shiba.ErrorDeep,
    onErrorContainer = Shiba.DarkInk,
)

/** Squircle-ish corners like the icon. */
private val ShibaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** System font, nudged to friendlier medium weights for headings and labels. */
private val ShibaTypography = Typography().let { base ->
    base.copy(
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontWeight = FontWeight.Medium),
    )
}

@Composable
fun PeekTodoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val extended = if (darkTheme) DarkExtendedColors else LightExtendedColors
    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            shapes = ShibaShapes,
            typography = ShibaTypography,
            content = content,
        )
    }
}

/** Access point for the extra icon colours: `MaterialTheme.shiba.card`. */
val MaterialTheme.shiba: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
