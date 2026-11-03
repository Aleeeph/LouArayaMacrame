package cl.louaraya.macrame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.louaraya.macrame.data.local.ProductEntity
import cl.louaraya.macrame.data.prefs.UserPreferences
import cl.louaraya.macrame.data.repository.CatalogRepository
import cl.louaraya.macrame.data.repository.OrderRepository
import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.domain.OrderRules
import cl.louaraya.macrame.domain.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderFormState(
    val quantity: String = "1",
    val eventMillis: Long? = null,
    val notes: String = "",
    val quantityError: String? = null,
    val eventError: String? = null,
    val notesError: String? = null,
    val saving: Boolean = false
) {
    val isValid: Boolean
        get() = Validators.quantity(quantity) == null &&
            Validators.eventDate(eventMillis) == null &&
            Validators.notes(notes) == null
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModel(
    private val prefs: UserPreferences,
    private val catalogRepo: CatalogRepository,
    private val orderRepo: OrderRepository,
    private val productId: Long
) : ViewModel() {

    val product: StateFlow<ProductEntity?> = catalogRepo.observeProduct(productId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isAdmin: StateFlow<Boolean> = prefs.role.map { it == Catalog.ROLE_ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val favorite: StateFlow<Boolean> = prefs.userId.filterNotNull()
        .flatMapLatest { catalogRepo.observeFavoriteIds(it) }
        .map { productId in it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _form = MutableStateFlow(OrderFormState())
    val form: StateFlow<OrderFormState> = _form.asStateFlow()

    /** Total estimado según la cantidad ingresada (0 si la cantidad no es válida). */
    fun estimatedTotal(): Int {
        val p = product.value ?: return 0
        val q = Validators.parseInt(_form.value.quantity) ?: return 0
        return if (Validators.quantity(_form.value.quantity) == null) OrderRules.total(p.price, q) else 0
    }

    fun onQuantityChange(v: String) = _form.update { it.copy(quantity = v, quantityError = Validators.quantity(v)) }
    fun onEventChange(v: Long) = _form.update { it.copy(eventMillis = v, eventError = Validators.eventDate(v)) }
    fun onNotesChange(v: String) = _form.update { it.copy(notes = v, notesError = Validators.notes(v)) }

    fun resetForm() {
        _form.value = OrderFormState()
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val uid = prefs.userIdOrNull() ?: return@launch
            catalogRepo.toggleFavorite(uid, productId)
        }
    }

    fun submitOrder(onSaved: () -> Unit) {
        val f = _form.value
        if (f.saving) return
        if (!f.isValid) {
            _form.value = f.copy(
                quantityError = Validators.quantity(f.quantity),
                eventError = Validators.eventDate(f.eventMillis),
                notesError = Validators.notes(f.notes)
            )
            return
        }
        viewModelScope.launch {
            val uid = prefs.userIdOrNull() ?: return@launch
            _form.update { it.copy(saving = true) }
            orderRepo.createOrder(
                userId = uid,
                productId = productId,
                quantity = Validators.parseInt(f.quantity)!!,
                eventMillis = f.eventMillis,
                notes = f.notes.trim()
            )
            _form.value = OrderFormState()
            onSaved()
        }
    }

    fun deleteProduct(onDeleted: () -> Unit) {
        val current = product.value ?: return
        viewModelScope.launch {
            catalogRepo.deleteProduct(current)
            onDeleted()
        }
    }
}
