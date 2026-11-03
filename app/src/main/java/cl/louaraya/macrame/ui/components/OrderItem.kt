package cl.louaraya.macrame.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.louaraya.macrame.data.local.OrderEntity
import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.util.Formatters

/** Fila de una solicitud. La administradora puede cambiar el estado; el cliente solo lo consulta. */
@Composable
fun OrderItem(
    order: OrderEntity,
    productName: String,
    category: String,
    canEditStatus: Boolean,
    onStatusChange: (String) -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    categoryIcon(category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "#${order.id} · ${productName.ifBlank { "Pieza eliminada" }}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${order.clientAlias} · ${order.quantity} u. · ${Formatters.price(order.total)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Solicitado ${Formatters.formatDate(order.createdMillis)}" +
                        (order.eventMillis?.let { " · Evento ${Formatters.formatUtcDate(it)}" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (order.notes.isNotBlank()) {
                    Text(
                        order.notes,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(order.status)
                    if (!order.synced) {
                        Icon(
                            Icons.Rounded.CloudOff,
                            contentDescription = "Pendiente de sincronizar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (canEditStatus) {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Cambiar estado")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        Catalog.statuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    menuOpen = false
                                    onStatusChange(status)
                                }
                            )
                        }
                    }
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Eliminar solicitud",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
