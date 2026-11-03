package cl.louaraya.macrame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.louaraya.macrame.domain.Catalog
import coil.compose.AsyncImage
import java.io.File

/** Sin insets: se usa en las pantallas de pestañas porque el Scaffold raíz ya reserva la barra inferior. */
val NoInsets = WindowInsets(0, 0, 0, 0)

/** Ícono de cada categoría de campaña. */
fun categoryIcon(category: String): ImageVector = when (category) {
    "Matrimonios" -> Icons.Rounded.Favorite
    "Festivales" -> Icons.Rounded.Celebration
    "Decoración de Interiores" -> Icons.Rounded.Weekend
    "Viajes" -> Icons.Rounded.Flight
    else -> Icons.Rounded.Palette
}

/**
 * Imagen de una pieza: foto tomada con la cámara o imagen remota (Coil la guarda en caché).
 * Si no hay imagen, o mientras carga, se ve un fondo con el ícono de su categoría.
 */
@Composable
fun ProductImage(
    photoPath: String?,
    imageUrl: String?,
    category: String,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    iconSize: Dp = 36.dp
) {
    val sized = if (size != null) modifier.size(size) else modifier
    Box(
        modifier = sized
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(iconSize)
        )
        val model: Any? = photoPath?.let { File(it) } ?: imageUrl
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = "Imagen de la pieza",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Anima la entrada (fade + desplazamiento) la primera vez que el contenido aparece. */
@Composable
fun AppearAnimated(delayMillis: Int = 0, content: @Composable () -> Unit) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(450, delayMillis)) + slideInVertically(tween(450, delayMillis)) { it / 6 }
    ) {
        content()
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(12.dp))
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Chip de estado de una solicitud, con un color distinto por estado. */
@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        Catalog.STATUS_PENDING -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        Catalog.STATUS_CONFIRMED -> scheme.primaryContainer to scheme.onPrimaryContainer
        Catalog.STATUS_DELIVERED -> scheme.surfaceContainerHighest to scheme.onSurface
        else -> scheme.secondaryContainer to scheme.onSecondaryContainer
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = container) {
        Text(
            status,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/** Overlay de éxito con un check que aparece con efecto de resorte. */
@Composable
fun SuccessOverlay(visible: Boolean, message: String) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        var appeared by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { appeared = true }
        val scale by animateFloatAsState(
            targetValue = if (appeared) 1f else 0.4f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "successScale"
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.scale(scale)
            ) {
                Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(message, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
