@file:OptIn(ExperimentalMaterial3Api::class)

package cl.louaraya.macrame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.ui.components.AppDatePickerDialog
import cl.louaraya.macrame.ui.components.AppTextField
import cl.louaraya.macrame.ui.components.AppTimePickerDialog
import cl.louaraya.macrame.ui.components.CampaignItem
import cl.louaraya.macrame.ui.components.ClickableField
import cl.louaraya.macrame.ui.components.DropdownField
import cl.louaraya.macrame.ui.components.EmptyState
import cl.louaraya.macrame.ui.components.NoInsets
import cl.louaraya.macrame.ui.components.TodayOrFuture
import cl.louaraya.macrame.ui.components.categoryIcon
import cl.louaraya.macrame.ui.components.rememberNotificationPermissionRequest
import cl.louaraya.macrame.util.Formatters
import cl.louaraya.macrame.viewmodel.CampaignFormState
import cl.louaraya.macrame.viewmodel.CampaignsViewModel
import cl.louaraya.macrame.viewmodel.appViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun CampaignsScreen(
    vm: CampaignsViewModel = appViewModel { CampaignsViewModel(it.prefs, it.campaignRepo) }
) {
    val campaigns by vm.campaigns.collectAsStateWithLifecycle()
    val form by vm.form.collectAsStateWithLifecycle()
    val isAdmin by vm.isAdmin.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by rememberSaveable { mutableStateOf(false) }

    // --- Recurso nativo 2: notificaciones (permiso en Android 13+) ---
    val requestNotifications = rememberNotificationPermissionRequest()

    Scaffold(
        contentWindowInsets = NoInsets,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Campañas", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { showSheet = true },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Nueva campaña") }
                )
            }
        }
    ) { padding ->
        if (campaigns.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Campaign,
                title = "Sin campañas",
                subtitle = if (isAdmin) {
                    "Toca «Nueva campaña» para programar una alerta de temporada."
                } else {
                    "Aquí verás las campañas temáticas de matrimonios, festivales y viajes."
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(campaigns, key = { it.id }) { campaign ->
                    CampaignItem(
                        campaign = campaign,
                        onDelete = if (isAdmin) ({ vm.delete(campaign) }) else null,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                vm.resetForm()
            },
            sheetState = sheetState
        ) {
            CampaignFormSheet(
                form = form,
                vm = vm,
                onSave = {
                    vm.save {
                        requestNotifications()
                        scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                        scope.launch { snackbar.showSnackbar("Campaña programada") }
                    }
                }
            )
        }
    }
}

@Composable
private fun CampaignFormSheet(form: CampaignFormState, vm: CampaignsViewModel, onSave: () -> Unit) {
    var showDate by rememberSaveable { mutableStateOf(false) }
    var showTime by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nueva campaña", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        AppTextField(
            value = form.title,
            onValueChange = vm::onTitleChange,
            label = "Título",
            leadingIcon = Icons.Rounded.Title,
            error = form.titleError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        DropdownField(
            value = form.category,
            label = "Categoría de campaña",
            options = Catalog.categories,
            icon = if (form.category.isBlank()) Icons.Rounded.Category else categoryIcon(form.category),
            onSelect = vm::onCategoryChange,
            error = form.categoryError
        )
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ClickableField(
                value = form.dateMillis?.let { Formatters.formatUtcDate(it) }.orEmpty(),
                label = "Fecha",
                leadingIcon = Icons.Rounded.CalendarToday,
                onClick = { showDate = true },
                error = form.dateError,
                modifier = Modifier.weight(1f)
            )
            ClickableField(
                value = if (form.hour != null && form.minute != null) Formatters.formatTime(form.hour, form.minute) else "",
                label = "Hora",
                leadingIcon = Icons.Rounded.Schedule,
                onClick = { showTime = true },
                error = form.timeError,
                modifier = Modifier.weight(1f)
            )
        }
        AppTextField(
            value = form.message,
            onValueChange = vm::onMessageChange,
            label = "Mensaje de la alerta",
            leadingIcon = Icons.Rounded.Edit,
            error = form.messageError,
            singleLine = false,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Button(
            onClick = onSave,
            enabled = form.isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) { Text("Programar campaña") }
    }

    if (showDate) {
        AppDatePickerDialog(
            initialMillis = form.dateMillis,
            selectableDates = TodayOrFuture,
            onConfirm = {
                vm.onDateChange(it)
                showDate = false
            },
            onDismiss = { showDate = false }
        )
    }
    if (showTime) {
        val now = LocalTime.now()
        AppTimePickerDialog(
            initialHour = form.hour ?: now.hour,
            initialMinute = form.minute ?: now.minute,
            onConfirm = { h, m ->
                vm.onTimeChange(h, m)
                showTime = false
            },
            onDismiss = { showTime = false }
        )
    }
}
