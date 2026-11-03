@file:OptIn(ExperimentalMaterial3Api::class)

package cl.louaraya.macrame.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.louaraya.macrame.ui.components.AppTextField
import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.ui.components.DropdownField
import cl.louaraya.macrame.ui.components.ErrorBanner
import cl.louaraya.macrame.ui.components.PasswordField
import cl.louaraya.macrame.viewmodel.RegisterViewModel
import cl.louaraya.macrame.viewmodel.appViewModel

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    vm: RegisterViewModel = appViewModel { RegisterViewModel(it.authRepo) }
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.success) { if (state.success) onRegistered() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
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
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Regístrate para explorar piezas, guardar tu colección y solicitar reservas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedVisibility(visible = state.formError != null) {
                ErrorBanner(state.formError ?: "", Modifier.fillMaxWidth())
            }
            AppTextField(
                value = state.name,
                onValueChange = vm::onNameChange,
                label = "Nombre",
                leadingIcon = Icons.Rounded.Person,
                error = state.nameError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            AppTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                label = "Correo electrónico",
                leadingIcon = Icons.Rounded.Email,
                error = state.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            DropdownField(
                value = state.roleLabel,
                label = "Perfil",
                options = Catalog.roleLabels.values.toList(),
                icon = Icons.Rounded.Badge,
                onSelect = vm::onRoleChange,
                error = state.roleError
            )
            PasswordField(
                value = state.password,
                onValueChange = vm::onPasswordChange,
                label = "Contraseña",
                error = state.passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
            )
            PasswordField(
                value = state.confirm,
                onValueChange = vm::onConfirmChange,
                label = "Confirmar contraseña",
                error = state.confirmError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { vm.submit() })
            )
            Button(
                onClick = vm::submit,
                enabled = state.canSubmit && !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Crear cuenta")
                }
            }
        }
    }
}
