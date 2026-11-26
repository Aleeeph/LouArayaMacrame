@file:OptIn(ExperimentalMaterial3Api::class)

package cl.louaraya.macrame.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.ui.components.AppTextField
import cl.louaraya.macrame.ui.components.AppearAnimated
import cl.louaraya.macrame.ui.components.EmptyState
import cl.louaraya.macrame.ui.components.NoInsets
import cl.louaraya.macrame.ui.components.ProductCard
import cl.louaraya.macrame.ui.components.categoryIcon
import cl.louaraya.macrame.viewmodel.CatalogViewModel
import cl.louaraya.macrame.viewmodel.SyncState
import cl.louaraya.macrame.viewmodel.appViewModel

@Composable
fun CatalogScreen(
    onAddProduct: () -> Unit,
    onOpenProduct: (Long) -> Unit,
    onOpenScanner: () -> Unit,
    vm: CatalogViewModel = appViewModel { CatalogViewModel(it.prefs, it.catalogRepo) }
) {
    val items by vm.items.collectAsStateWithLifecycle()
    val total by vm.total.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val category by vm.category.collectAsStateWithLifecycle()
    val onlyFav by vm.onlyFavorites.collectAsStateWithLifecycle()
    val sync by vm.sync.collectAsStateWithLifecycle()
    val isAdmin by vm.isAdmin.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = NoInsets,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Catálogo", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenScanner) {
                        Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Escanear código QR")
                    }
                    IconButton(onClick = vm::refresh, enabled = sync != SyncState.SYNCING) {
                        if (sync == SyncState.SYNCING) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.Sync, contentDescription = "Sincronizar catálogo")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = onAddProduct,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Nueva pieza") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            AppTextField(
                value = query,
                onValueChange = vm::onQueryChange,
                label = "Buscar por nombre o material",
                leadingIcon = Icons.Rounded.Search,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = category == null && !onlyFav,
                        onClick = {
                            vm.onCategoryChange(null)
                            vm.onOnlyFavoritesChange(false)
                        },
                        label = { Text("Todas") }
                    )
                }
                item {
                    FilterChip(
                        selected = onlyFav,
                        onClick = { vm.onOnlyFavoritesChange(!onlyFav) },
                        label = { Text("Mi colección") },
                        leadingIcon = { Icon(Icons.Rounded.Favorite, contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
                items(Catalog.categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { vm.onCategoryChange(if (category == cat) null else cat) },
                        label = { Text(cat) },
                        leadingIcon = { Icon(categoryIcon(cat), contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
            }
            AnimatedVisibility(visible = sync == SyncState.OFFLINE) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CloudOff, contentDescription = null)
                        Text(
                            "  Sin conexión con el servidor: se muestra el catálogo guardado en el teléfono.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            when {
                total == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                items.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.Storefront,
                    title = if (onlyFav) "Tu colección está vacía" else "Sin resultados",
                    subtitle = if (onlyFav) {
                        "Toca el corazón de una pieza para guardarla aquí."
                    } else {
                        "Prueba con otra búsqueda o categoría."
                    }
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "${items.size} ${if (items.size == 1) "pieza" else "piezas"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(items, key = { it.product.id }) { item ->
                        Box(Modifier.animateItem()) {
                            AppearAnimated {
                                ProductCard(
                                    product = item.product,
                                    favorite = item.favorite,
                                    onToggleFavorite = { vm.toggleFavorite(item.product.id) },
                                    onClick = { onOpenProduct(item.product.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
