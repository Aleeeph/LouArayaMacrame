@file:OptIn(ExperimentalMaterial3Api::class)

package cl.louaraya.macrame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.ui.components.EmptyState
import cl.louaraya.macrame.ui.components.NoInsets
import cl.louaraya.macrame.ui.components.OrderItem
import cl.louaraya.macrame.viewmodel.OrdersViewModel
import cl.louaraya.macrame.viewmodel.appViewModel

@Composable
fun OrdersScreen(
    vm: OrdersViewModel = appViewModel { OrdersViewModel(it.prefs, it.orderRepo, it.catalogRepo) }
) {
    val all by vm.allItems.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()
    val filter by vm.statusFilter.collectAsStateWithLifecycle()
    val isAdmin by vm.isAdmin.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = NoInsets,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isAdmin) "Solicitudes" else "Mis solicitudes", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (all.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.ShoppingBag,
                title = "Sin solicitudes",
                subtitle = if (isAdmin) {
                    "Cuando los clientes soliciten piezas, aparecerán aquí."
                } else {
                    "Abre una pieza del catálogo y toca «Solicitar reserva»."
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(Modifier.padding(padding)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filter == null,
                            onClick = { vm.onFilterChange(null) },
                            label = { Text("Todas (${all.size})") }
                        )
                    }
                    items(Catalog.statuses) { status ->
                        val count = all.count { it.order.status == status }
                        FilterChip(
                            selected = filter == status,
                            onClick = { vm.onFilterChange(if (filter == status) null else status) },
                            label = { Text("$status ($count)") }
                        )
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (items.isEmpty()) {
                        item {
                            Text(
                                "No hay solicitudes con este estado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(items, key = { it.order.id }) { item ->
                        OrderItem(
                            order = item.order,
                            productName = item.productName,
                            category = item.category,
                            canEditStatus = isAdmin,
                            onStatusChange = { vm.setStatus(item.order, it) },
                            // el cliente solo puede retirar solicitudes que aún no se confirman
                            onDelete = if (isAdmin || item.order.status in listOf(
                                    Catalog.STATUS_EXPLORATION, Catalog.STATUS_PENDING
                                )
                            ) {
                                { vm.delete(item.order) }
                            } else {
                                null
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}
