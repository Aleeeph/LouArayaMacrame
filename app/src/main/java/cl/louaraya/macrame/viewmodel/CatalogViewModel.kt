package cl.louaraya.macrame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.louaraya.macrame.data.local.ProductEntity
import cl.louaraya.macrame.data.prefs.UserPreferences
import cl.louaraya.macrame.data.repository.CatalogRepository
import cl.louaraya.macrame.domain.Catalog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductItem(val product: ProductEntity, val favorite: Boolean)

enum class SyncState { IDLE, SYNCING, OK, OFFLINE }

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(
    private val prefs: UserPreferences,
    private val repo: CatalogRepository
) : ViewModel() {

    private val userId = prefs.userId.filterNotNull()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** null = todas las categorías. */
    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    private val _sync = MutableStateFlow(SyncState.IDLE)
    val sync: StateFlow<SyncState> = _sync.asStateFlow()

    val isAdmin: StateFlow<Boolean> = prefs.role.map { it == Catalog.ROLE_ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Total de piezas del catálogo (null mientras carga). */
    val total: StateFlow<Int?> = repo.observeProducts().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Piezas filtradas por búsqueda, categoría y colección guardada. */
    val items: StateFlow<List<ProductItem>> = combine(
        repo.observeProducts(),
        userId.flatMapLatest { repo.observeFavoriteIds(it) },
        _query,
        _category,
        _onlyFavorites
    ) { products, favorites, query, category, onlyFav ->
        val q = query.trim()
        products
            .filter { category == null || it.category == category }
            .filter { q.isEmpty() || it.name.contains(q, ignoreCase = true) || it.material.contains(q, ignoreCase = true) }
            .map { ProductItem(it, it.id in favorites) }
            .filter { !onlyFav || it.favorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onCategoryChange(value: String?) {
        _category.value = value
    }

    fun onOnlyFavoritesChange(value: Boolean) {
        _onlyFavorites.value = value
    }

    fun toggleFavorite(productId: Long) {
        viewModelScope.launch {
            val uid = prefs.userIdOrNull() ?: return@launch
            repo.toggleFavorite(uid, productId)
        }
    }

    /** Sincroniza el catálogo con la API REST (si no hay conexión, se usa lo guardado). */
    fun refresh() {
        if (_sync.value == SyncState.SYNCING) return
        viewModelScope.launch {
            _sync.update { SyncState.SYNCING }
            val ok = repo.refreshFromApi()
            _sync.update { if (ok) SyncState.OK else SyncState.OFFLINE }
        }
    }
}

/** Lee el id de usuario actual una sola vez. */
suspend fun UserPreferences.userIdOrNull(): Long? =
    userId.firstOrNull()
