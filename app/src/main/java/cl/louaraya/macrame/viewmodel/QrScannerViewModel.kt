package cl.louaraya.macrame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.louaraya.macrame.data.repository.CatalogRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QrScannerViewModel(
    private val catalogRepository: CatalogRepository
) : ViewModel() {
    private val _lookup = MutableStateFlow<QrLookupState>(QrLookupState.Idle)
    val lookup: StateFlow<QrLookupState> = _lookup.asStateFlow()

    fun findProduct(id: Long, onFound: (Long) -> Unit) {
        viewModelScope.launch {
            _lookup.value = QrLookupState.Loading
            val product = catalogRepository.getProduct(id)
            if (product == null) {
                _lookup.value = QrLookupState.NotFound
            } else {
                _lookup.value = QrLookupState.Found
                onFound(id)
            }
        }
    }

    fun reset() {
        _lookup.value = QrLookupState.Idle
    }
}

sealed interface QrLookupState {
    data object Idle : QrLookupState
    data object Loading : QrLookupState
    data object Found : QrLookupState
    data object NotFound : QrLookupState
}
