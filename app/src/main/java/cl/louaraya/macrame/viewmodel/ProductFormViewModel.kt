package cl.louaraya.macrame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.louaraya.macrame.data.local.ProductEntity
import cl.louaraya.macrame.data.repository.CatalogRepository
import cl.louaraya.macrame.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductFormState(
    val isEditing: Boolean = false,
    val name: String = "",
    val category: String = "",
    val material: String = "",
    val width: String = "",
    val length: String = "",
    val price: String = "",
    val description: String = "",
    val photoPath: String? = null,
    val nameError: String? = null,
    val categoryError: String? = null,
    val materialError: String? = null,
    val widthError: String? = null,
    val lengthError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val saving: Boolean = false,
    val saved: Boolean = false
) {
    val isValid: Boolean
        get() = Validators.productName(name) == null &&
            Validators.category(category) == null &&
            Validators.material(material) == null &&
            Validators.dimension(width, "ancho") == null &&
            Validators.dimension(length, "largo") == null &&
            Validators.price(price) == null &&
            Validators.description(description) == null
}

class ProductFormViewModel(
    private val repo: CatalogRepository,
    private val productId: Long
) : ViewModel() {

    private var original: ProductEntity? = null

    private val _state = MutableStateFlow(ProductFormState(isEditing = productId != NEW))
    val state: StateFlow<ProductFormState> = _state.asStateFlow()

    init {
        if (productId != NEW) {
            viewModelScope.launch {
                repo.getProduct(productId)?.let { p ->
                    original = p
                    _state.update {
                        it.copy(
                            name = p.name,
                            category = p.category,
                            material = p.material,
                            width = p.widthCm.toString(),
                            length = p.lengthCm.toString(),
                            price = p.price.toString(),
                            description = p.description,
                            photoPath = p.photoPath
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v, nameError = Validators.productName(v)) }
    fun onCategoryChange(v: String) = _state.update { it.copy(category = v, categoryError = Validators.category(v)) }
    fun onMaterialChange(v: String) = _state.update { it.copy(material = v, materialError = Validators.material(v)) }
    fun onWidthChange(v: String) = _state.update { it.copy(width = v, widthError = Validators.dimension(v, "ancho")) }
    fun onLengthChange(v: String) = _state.update { it.copy(length = v, lengthError = Validators.dimension(v, "largo")) }
    fun onPriceChange(v: String) = _state.update { it.copy(price = v, priceError = Validators.price(v)) }
    fun onDescriptionChange(v: String) =
        _state.update { it.copy(description = v, descriptionError = Validators.description(v)) }
    fun onPhotoChange(path: String) = _state.update { it.copy(photoPath = path) }

    fun save() {
        val s = _state.value
        if (s.saving) return
        if (!s.isValid) {
            // muestra todos los errores pendientes
            _state.value = s.copy(
                nameError = Validators.productName(s.name),
                categoryError = Validators.category(s.category),
                materialError = Validators.material(s.material),
                widthError = Validators.dimension(s.width, "ancho"),
                lengthError = Validators.dimension(s.length, "largo"),
                priceError = Validators.price(s.price),
                descriptionError = Validators.description(s.description)
            )
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            val base = original
            repo.saveProduct(
                ProductEntity(
                    id = if (productId == NEW) 0 else productId,
                    remoteId = base?.remoteId,
                    name = s.name.trim(),
                    category = s.category,
                    material = s.material,
                    widthCm = Validators.parseInt(s.width)!!,
                    lengthCm = Validators.parseInt(s.length)!!,
                    price = Validators.parseInt(s.price)!!,
                    description = s.description.trim(),
                    photoPath = s.photoPath,
                    imageUrl = base?.imageUrl
                )
            )
            _state.update { it.copy(saving = false, saved = true) }
        }
    }

    companion object {
        const val NEW = -1L
    }
}
