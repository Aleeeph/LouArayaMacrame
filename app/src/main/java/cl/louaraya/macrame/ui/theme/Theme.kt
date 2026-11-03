package cl.louaraya.macrame.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Paleta macramé: terracota cálido, arena natural y verde salvia como acento.
private val LightColors = lightColorScheme(
    primary = Color(0xFF8C4A2F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCB),
    onPrimaryContainer = Color(0xFF370E00),
    secondary = Color(0xFF77574B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF2C150D),
    tertiary = Color(0xFF56633F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD9E8BA),
    onTertiaryContainer = Color(0xFF141F03),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBF5EE),
    onBackground = Color(0xFF211A17),
    surface = Color(0xFFFBF5EE),
    onSurface = Color(0xFF211A17),
    surfaceVariant = Color(0xFFF5DED4),
    onSurfaceVariant = Color(0xFF52443D),
    outline = Color(0xFF85736C),
    outlineVariant = Color(0xFFD8C2BA),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8EFE7),
    surfaceContainer = Color(0xFFF3EAE1),
    surfaceContainerHigh = Color(0xFFEDE4DB),
    surfaceContainerHighest = Color(0xFFE7DED5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB596),
    onPrimary = Color(0xFF571F06),
    primaryContainer = Color(0xFF703419),
    onPrimaryContainer = Color(0xFFFFDBCB),
    secondary = Color(0xFFE6BEAE),
    onSecondary = Color(0xFF442A1F),
    secondaryContainer = Color(0xFF5D4034),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFBDCC9F),
    onTertiary = Color(0xFF293515),
    tertiaryContainer = Color(0xFF3F4C2A),
    onTertiaryContainer = Color(0xFFD9E8BA),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1210),
    onBackground = Color(0xFFF0DFD9),
    surface = Color(0xFF1A1210),
    onSurface = Color(0xFFF0DFD9),
    surfaceVariant = Color(0xFF52443D),
    onSurfaceVariant = Color(0xFFD8C2BA),
    outline = Color(0xFFA08D85),
    outlineVariant = Color(0xFF52443D),
    surfaceContainerLowest = Color(0xFF140C0A),
    surfaceContainerLow = Color(0xFF221A17),
    surfaceContainer = Color(0xFF271E1B),
    surfaceContainerHigh = Color(0xFF322825),
    surfaceContainerHighest = Color(0xFF3D3330)
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MacrameTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
