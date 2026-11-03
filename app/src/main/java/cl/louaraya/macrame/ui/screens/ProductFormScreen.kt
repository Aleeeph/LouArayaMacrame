@file:OptIn(ExperimentalMaterial3Api::class)

package cl.louaraya.macrame.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Texture
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.ui.components.AppTextField
import cl.louaraya.macrame.ui.components.DropdownField
import cl.louaraya.macrame.ui.components.ProductImage
import cl.louaraya.macrame.ui.components.SuccessOverlay
import cl.louaraya.macrame.ui.components.categoryIcon
import cl.louaraya.macrame.util.Intents
import cl.louaraya.macrame.util.PhotoStorage
import cl.louaraya.macrame.viewmodel.ProductFormViewModel
import cl.louaraya.macrame.viewmodel.appViewModel
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun ProductFormScreen(
    productId: Long,
    onBack: () -> Unit,
    vm: ProductFormViewModel = appViewModel(key = "product_form_$productId") {
        ProductFormViewModel(it.catalogRepo, productId)
    }
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showRationale by rememberSaveable { mutableStateOf(false) }
    var showDenied by rememberSaveable { mutableStateOf(false) }
    var pendingPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }

    // --- Recurso nativo 1: cámara (foto de la pieza) ---
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val path = pendingPhotoPath
        if (success && path != null) vm.onPhotoChange(path) else path?.let { File(it).delete() }
        pendingPhotoPath = null
    }

    fun launchCamera() {
        val file = PhotoStorage.newPhotoFile(context)
        pendingPhotoPath = file.absolutePath
        takePicture.launch(PhotoStorage.uriFor(context, file))
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else showDenied = true
    }

    fun onCameraClick() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) launchCamera() else showRationale = true
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            delay(1000)
            onBack()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (state.isEditing) "Editar pieza" else "Nueva pieza") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally)) {
                    ProductImage(
                        photoPath = state.photoPath,
                        imageUrl = null,
                        category = state.category,
                        size = 144.dp,
                        shape = RoundedCornerShape(24.dp),
                        iconSize = 56.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onCameraClick() }
                    )
                    FilledIconButton(
                        onClick = { onCameraClick() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Rounded.PhotoCamera, contentDescription = "Tomar foto")
                    }
                }
                Text(
                    "Toca la imagen para fotografiar la pieza",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                AppTextField(
                    value = state.name,
                    onValueChange = vm::onNameChange,
                    label = "Nombre del diseño",
                    leadingIcon = Icons.Rounded.Palette,
                    error = state.nameError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                DropdownField(
                    value = state.category,
                    label = "Categoría de campaña",
                    options = Catalog.categories,
                    icon = if (state.category.isBlank()) Icons.Rounded.Category else categoryIcon(state.category),
                    onSelect = vm::onCategoryChange,
                    error = state.categoryError
                )
                DropdownField(
                    value = state.material,
                    label = "Material",
                    options = Catalog.materials,
                    icon = Icons.Rounded.Texture,
                    onSelect = vm::onMaterialChange,
                    error = state.materialError
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = state.width,
                        onValueChange = vm::onWidthChange,
                        label = "Ancho (cm)",
                        leadingIcon = Icons.Rounded.Straighten,
                        error = state.widthError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = state.length,
                        onValueChange = vm::onLengthChange,
                        label = "Largo (cm)",
                        leadingIcon = Icons.Rounded.Straighten,
                        error = state.lengthError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                }
                AppTextField(
                    value = state.price,
                    onValueChange = vm::onPriceChange,
                    label = "Precio (CLP)",
                    leadingIcon = Icons.Rounded.Payments,
                    error = state.priceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                )
                AppTextField(
                    value = state.description,
                    onValueChange = vm::onDescriptionChange,
                    label = "Descripción (opcional)",
                    leadingIcon = Icons.Rounded.Description,
                    error = state.descriptionError,
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = vm::save,
                    enabled = state.isValid && !state.saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        SuccessOverlay(visible = state.saved, message = "¡Pieza guardada!")
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            icon = { Icon(Icons.Rounded.PhotoCamera, contentDescription = null) },
            title = { Text("Permiso de cámara") },
            text = { Text("La app usa la cámara solo para fotografiar las piezas del catálogo. La imagen se guarda únicamente en tu dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    cameraPermission.launch(Manifest.permission.CAMERA)
                }) { Text("Continuar") }
            },
            dismissButton = { TextButton(onClick = { showRationale = false }) { Text("Ahora no") } }
        )
    }

    if (showDenied) {
        AlertDialog(
            onDismissRequest = { showDenied = false },
            title = { Text("Sin acceso a la cámara") },
            text = { Text("Para tomar fotos debes permitir el acceso a la cámara desde los ajustes de la aplicación.") },
            confirmButton = {
                TextButton(onClick = {
                    showDenied = false
                    Intents.openAppSettings(context)
                }) { Text("Abrir ajustes") }
            },
            dismissButton = { TextButton(onClick = { showDenied = false }) { Text("Cerrar") } }
        )
    }
}
