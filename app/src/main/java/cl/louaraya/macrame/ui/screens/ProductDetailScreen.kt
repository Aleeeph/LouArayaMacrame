@file:OptIn(ExperimentalMaterial3Api::class)

package cl.louaraya.macrame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Texture
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.louaraya.macrame.data.local.ProductEntity
import cl.louaraya.macrame.domain.OrderRules
import cl.louaraya.macrame.ui.components.AppDatePickerDialog
import cl.louaraya.macrame.ui.components.AppTextField
import cl.louaraya.macrame.ui.components.AppearAnimated
import cl.louaraya.macrame.ui.components.ClickableField
import cl.louaraya.macrame.ui.components.FavoriteButton
import cl.louaraya.macrame.ui.components.ProductImage
import cl.louaraya.macrame.ui.components.TodayOrFuture
import cl.louaraya.macrame.ui.components.rememberNotificationPermissionRequest
import cl.louaraya.macrame.util.Formatters
import cl.louaraya.macrame.util.Intents
import cl.louaraya.macrame.viewmodel.OrderFormState
import cl.louaraya.macrame.viewmodel.ProductDetailViewModel
import cl.louaraya.macrame.viewmodel.appViewModel
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    productId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    vm: ProductDetailViewModel = appViewModel(key = "product_detail_$productId") {
        ProductDetailViewModel(it.prefs, it.catalogRepo, it.orderRepo, productId)
    }
) {
    val productState by vm.product.collectAsStateWithLifecycle()
    val favorite by vm.favorite.collectAsStateWithLifecycle()
    val isAdmin by vm.isAdmin.collectAsStateWithLifecycle()
    val form by vm.form.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showSheet by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // Recurso nativo: permiso de notificaciones para avisar que la solicitud quedó registrada.
    val requestNotifications = rememberNotificationPermissionRequest()

    val product = productState

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(product?.name.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (product != null) {
                        IconButton(onClick = {
                            Intents.shareText(
                                context,
                                subject = product.name,
                                text = "${product.name} (${OrderRules.productCode(product.id)}) · " +
                                    "${Formatters.dimensions(product.widthCm, product.lengthCm)} · " +
                                    "${product.material} · ${Formatters.price(product.price)} — Lou Araya Macramé"
                            )
                        }) { Icon(Icons.Rounded.Share, contentDescription = "Compartir pieza") }
                    }
                    if (isAdmin) {
                        IconButton(onClick = { onEdit(productId) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Editar pieza")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Eliminar pieza")
                        }
                    } else {
                        FavoriteButton(favorite = favorite, onClick = vm::toggleFavorite)
                    }
                }
            )
        }
    ) { padding ->
        if (product == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { ProductHeader(product) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoTile(Modifier.weight(1f), Icons.Rounded.Straighten, "Medidas", Formatters.dimensions(product.widthCm, product.lengthCm))
                        InfoTile(Modifier.weight(1f), Icons.Rounded.Texture, "Material", product.material)
                        InfoTile(Modifier.weight(1f), Icons.Rounded.Payments, "Precio", Formatters.price(product.price))
                    }
                }
                if (product.description.isNotBlank()) {
                    item {
                        Text(product.description, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                if (!isAdmin) {
                    item {
                        Button(
                            onClick = { showSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(Icons.Rounded.ShoppingBag, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Solicitar reserva")
                        }
                    }
                }
            }
        }
    }

    if (showSheet && product != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                vm.resetForm()
            },
            sheetState = sheetState
        ) {
            OrderFormSheet(
                product = product,
                form = form,
                total = vm.estimatedTotal(),
                vm = vm,
                onSave = {
                    vm.submitOrder {
                        requestNotifications()
                        scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                        scope.launch { snackbar.showSnackbar("Solicitud registrada") }
                    }
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar pieza") },
            text = { Text("Se eliminarán también sus solicitudes y favoritos. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    vm.deleteProduct(onBack)
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ProductHeader(product: ProductEntity) {
    AppearAnimated {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            ProductImage(
                photoPath = product.photoPath,
                imageUrl = product.imageUrl,
                category = product.category,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = MaterialTheme.shapes.large,
                iconSize = 72.dp
            )
            Spacer(Modifier.height(12.dp))
            Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "${OrderRules.productCode(product.id)} · ${product.category}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoTile(modifier: Modifier, icon: ImageVector, title: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun OrderFormSheet(
    product: ProductEntity,
    form: OrderFormState,
    total: Int,
    vm: ProductDetailViewModel,
    onSave: () -> Unit
) {
    var showDate by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Solicitar reserva", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "${product.name} · ${Formatters.price(product.price)} c/u",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AppTextField(
            value = form.quantity,
            onValueChange = vm::onQuantityChange,
            label = "Cantidad",
            leadingIcon = Icons.Rounded.Inventory2,
            error = form.quantityError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
        )
        ClickableField(
            value = form.eventMillis?.let { Formatters.formatUtcDate(it) }.orEmpty(),
            label = "Fecha del evento",
            leadingIcon = Icons.Rounded.CalendarToday,
            onClick = { showDate = true },
            error = form.eventError
        )
        AppTextField(
            value = form.notes,
            onValueChange = vm::onNotesChange,
            label = "Notas (opcional)",
            leadingIcon = Icons.Rounded.Edit,
            error = form.notesError,
            singleLine = false,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        if (total > 0) {
            Text(
                "Total estimado: ${Formatters.price(total)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Button(
            onClick = onSave,
            enabled = form.isValid && !form.saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) { Text("Enviar solicitud") }
    }

    if (showDate) {
        AppDatePickerDialog(
            initialMillis = form.eventMillis,
            selectableDates = TodayOrFuture,
            onConfirm = {
                vm.onEventChange(it)
                showDate = false
            },
            onDismiss = { showDate = false }
        )
    }
}
