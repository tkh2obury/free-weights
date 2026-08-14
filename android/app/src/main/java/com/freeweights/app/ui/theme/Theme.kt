package com.freeweights.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

val MatrixGreen = Color(0xFF00FF66)
val MatrixGreenDim = Color(0xFF00A846)
val TerminalBlack = Color(0xFF020704)
val PanelBlack = Color(0xFF07110B)
val PanelHigh = Color(0xFF0B1D12)
val TerminalText = MatrixGreen
val TerminalMuted = Color(0xFF67A77D)
val Cyan = Color(0xFF00E5FF)
val Amber = Color(0xFFFFC857)

// Backward-compatible names used by a few compact UI helpers.
val Acid = MatrixGreen
val Blue = Cyan
val Orange = Amber

fun normalizeThemeHex(value: String): String? {
    val digits = value.trim().removePrefix("#")
    if (digits.length != 6 || digits.any { it.digitToIntOrNull(16) == null }) return null
    return "#${digits.uppercase()}"
}

fun themeColor(value: String, fallback: Color): Color = normalizeThemeHex(value)
    ?.removePrefix("#")
    ?.toLongOrNull(16)
    ?.let { Color((0xFF000000L or it).toInt()) }
    ?: fallback

private fun freeWeightsColors(textHex: String, backgroundHex: String) = run {
    val text = themeColor(textHex, TerminalText)
    val background = themeColor(backgroundHex, TerminalBlack)
    darkColorScheme(
        primary = text,
        onPrimary = background,
        secondary = lerp(text, Cyan, .45f),
        onSecondary = background,
        tertiary = Amber,
        background = background,
        onBackground = text,
        surface = lerp(background, text, .045f),
        onSurface = text,
        surfaceVariant = lerp(background, text, .09f),
        onSurfaceVariant = lerp(background, text, .64f),
        outline = lerp(background, text, .34f),
        outlineVariant = lerp(background, text, .19f),
        error = Color(0xFFFF5C72),
        onError = background,
    )
}

private val TerminalTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = FontFamily.Monospace),
        displayMedium = base.displayMedium.copy(fontFamily = FontFamily.Monospace),
        displaySmall = base.displaySmall.copy(fontFamily = FontFamily.Monospace),
        headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Monospace),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Monospace),
        headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Monospace),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Monospace),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Monospace),
        titleSmall = base.titleSmall.copy(fontFamily = FontFamily.Monospace),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.Monospace),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        bodySmall = base.bodySmall.copy(fontFamily = FontFamily.Monospace),
        labelLarge = base.labelLarge.copy(fontFamily = FontFamily.Monospace),
        labelMedium = base.labelMedium.copy(fontFamily = FontFamily.Monospace),
        labelSmall = base.labelSmall.copy(fontFamily = FontFamily.Monospace),
    )
}

@Composable
fun FreeWeightsTheme(
    textColor: String = "#00FF66",
    backgroundColor: String = "#020704",
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = freeWeightsColors(textColor, backgroundColor),
        typography = TerminalTypography,
        content = content,
    )
}
