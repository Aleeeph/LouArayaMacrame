@file:OptIn(ExperimentalMaterial3Api::class)

package cl.louaraya.macrame.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.louaraya.macrame.data.local.ProductEntity
import cl.louaraya.macrame.domain.OrderRules
import cl.louaraya.macrame.util.Formatters

@Composable
fun ProductCard(
    product: ProductEntity,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(
                photoPath = product.photoPath,
                imageUrl = product.imageUrl,
                category = product.category,
                size = 92.dp
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${product.category} · ${product.material}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${OrderRules.productCode(product.id)} · ${Formatters.dimensions(product.widthCm, product.lengthCm)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    Formatters.price(product.price),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            FavoriteButton(favorite = favorite, onClick = onToggleFavorite)
        }
    }
}

/** Corazón con animación de resorte y cambio de color al guardar la pieza en la colección. */
@Composable
fun FavoriteButton(favorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (favorite) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "favScale"
    )
    val tint by animateColorAsState(
        targetValue = if (favorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "favTint"
    )
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (favorite) "Quitar de mi colección" else "Guardar en mi colección",
            tint = tint,
            modifier = Modifier.scale(scale)
        )
    }
}
