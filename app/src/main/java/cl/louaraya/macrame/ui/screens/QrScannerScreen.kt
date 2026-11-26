package cl.louaraya.macrame.ui.screens

import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.louaraya.macrame.domain.QrProductCode
import cl.louaraya.macrame.viewmodel.QrScannerViewModel
import cl.louaraya.macrame.viewmodel.QrLookupState
import cl.louaraya.macrame.viewmodel.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    onBack: () -> Unit,
    onProductFound: (Long) -> Unit,
    vm: QrScannerViewModel = appViewModel { QrScannerViewModel(it.catalogRepo) }
) {
    val context = LocalContext.current
    val lookup by vm.lookup.collectAsStateWithLifecycle()
    var scanning by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Escanea el código QR de una pieza Lou Araya para abrir su ficha.") }
    var scannedValue by remember { mutableStateOf<String?>(null) }
    val scanner = remember(context) { GmsBarcodeScanning.getClient(context) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Escáner QR", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.QrCodeScanner,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(112.dp)
            )
            Text("Buscar una pieza", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(
                when (lookup) {
                    QrLookupState.NotFound -> "El código es válido, pero no existe una pieza con ese identificador en el catálogo local."
                    QrLookupState.Loading -> "Verificando la pieza en el catálogo..."
                    else -> message
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            scannedValue?.let { value ->
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth()) {
                    Text("Contenido leído: $value", Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    vm.reset()
                    scanning = true
                    scanner.startScan()
                        .addOnSuccessListener { barcode ->
                            scanning = false
                            val value = barcode.rawValue.orEmpty()
                            scannedValue = value
                            val id = QrProductCode.productIdFrom(value)
                            if (id != null) {
                                message = "Código reconocido. Verificando pieza en el catálogo..."
                                vm.findProduct(id, onProductFound)
                            } else {
                                message = "El QR se leyó, pero no contiene un código de producto válido. Formato esperado: MAC-001."
                            }
                        }
                        .addOnCanceledListener {
                            scanning = false
                            message = "Escaneo cancelado. Puedes intentarlo nuevamente."
                        }
                        .addOnFailureListener { error ->
                            scanning = false
                            message = "No fue posible iniciar el escáner: ${error.localizedMessage ?: "error desconocido"}"
                        }
                },
                enabled = !scanning && lookup != QrLookupState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (scanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                    Text("  Escanear código QR")
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Los códigos del catálogo deben incluir el identificador MAC- seguido del número de pieza. El escaneo utiliza Google Code Scanner.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
