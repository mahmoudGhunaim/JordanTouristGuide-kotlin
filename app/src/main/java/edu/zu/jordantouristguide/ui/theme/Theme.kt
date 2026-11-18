package edu.zu.jordantouristguide.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.zu.jordantouristguide.data.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = PetraRose,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBD1),
    onPrimaryContainer = Color(0xFF3B0A02),
    secondary = DeadSeaBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC4E8F5),
    onSecondaryContainer = Color(0xFF001F2A),
    tertiary = DesertGold,
    onTertiary = Basalt,
    tertiaryContainer = Color(0xFFFFE3B0),
    onTertiaryContainer = Color(0xFF2A1800),
    background = Sand50,
    onBackground = Basalt,
    surface = Sand50,
    onSurface = Basalt,
    surfaceVariant = Sand200,
    onSurfaceVariant = Color(0xFF55443C),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White, // cards
    surfaceContainer = Sand100,
    surfaceContainerHigh = Color(0xFFF0E4D2),
    surfaceContainerHighest = Sand200,
    outline = Color(0xFF887269),
    outlineVariant = Color(0xFFDAC2B8),
)

private val DarkColors = darkColorScheme(
    primary = PetraRoseLight,
    onPrimary = Color(0xFF5F1504),
    primaryContainer = PetraRoseDark,
    onPrimaryContainer = Color(0xFFFFDBD1),
    secondary = DeadSeaBlueLight,
    onSecondary = Color(0xFF003546),
    secondaryContainer = Color(0xFF004D64),
    onSecondaryContainer = Color(0xFFC4E8F5),
    tertiary = DesertGoldLight,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF634000),
    onTertiaryContainer = Color(0xFFFFE3B0),
    background = NightSand,
    onBackground = Color(0xFFF1DFD7),
    surface = NightSand,
    onSurface = Color(0xFFF1DFD7),
    surfaceVariant = Color(0xFF53433D),
    onSurfaceVariant = Color(0xFFD8C2BA),
    surfaceContainerLowest = Color(0xFF140F0C),
    surfaceContainerLow = Color(0xFF2A211C), // cards, a bit lighter than the background
    surfaceContainer = NightSurface,
    surfaceContainerHigh = NightSurfaceHigh,
    surfaceContainerHighest = Color(0xFF3E322A),
    outline = Color(0xFFA08D85),
    outlineVariant = Color(0xFF53433D),
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 26.sp),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * App theme. Dynamic (wallpaper) colors are deliberately not used so the
 * app keeps its Jordanian identity on every device.
 */
@Composable
fun JordanGuideTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
