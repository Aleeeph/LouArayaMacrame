package cl.louaraya.macrame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.louaraya.macrame.data.local.OrderEntity
import cl.louaraya.macrame.data.prefs.UserPreferences
import cl.louaraya.macrame.data.repository.CatalogRepository
import cl.louaraya.macrame.data.repository.OrderRepository
import cl.louaraya.macrame.domain.Catalog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrderItemUi(val order: OrderEntity, val productName: String, val category: String)

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModel(
    private val prefs: UserPreferences,
    private val orderRepo: OrderRepository,
    catalogRepo: CatalogRepository
) : ViewModel() {

    val isAdmin: StateFlow<Boolean> = prefs.role.map { it == Catalog.ROLE_ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** null = todos los estados. */
    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    /** La administradora ve todas las solicitudes; el cliente solo las suyas. */
    private val orders = combine(prefs.userId.filterNotNull(), prefs.role) { id, role -> id to role }
        .flatMapLatest { (id, role) ->
            if (role == Catalog.ROLE_ADMIN) orderRepo.observeAll() else orderRepo.observeByUser(id)
        }

    val allItems: StateFlow<List<OrderItemUi>> = combine(orders, catalogRepo.observeProducts()) { list, products ->
        val byId = products.associateBy { it.id }
        list.map { OrderItemUi(it, byId[it.productId]?.name.orEmpty(), byId[it.productId]?.category.orEmpty()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val items: StateFlow<List<OrderItemUi>> = combine(allItems, _statusFilter) { list, filter ->
        if (filter == null) list else list.filter { it.order.status == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onFilterChange(status: String?) {
        _statusFilter.value = status
    }

    fun setStatus(order: OrderEntity, status: String) {
        if (order.status == status) return
        viewModelScope.launch { orderRepo.setStatus(order, status) }
    }

    fun delete(order: OrderEntity) {
        viewModelScope.launch { orderRepo.delete(order) }
    }
}
