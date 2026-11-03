package cl.louaraya.macrame.data.remote

import cl.louaraya.macrame.data.local.OrderEntity
import cl.louaraya.macrame.data.local.ProductEntity
import com.google.gson.annotations.SerializedName

/**
 * Objetos de transferencia con la API REST del backend de la cátedra.
 * Todos los campos son opcionales para tolerar respuestas incompletas.
 * Si el backend usa otros nombres de campo, solo hay que ajustar los @SerializedName.
 */
data class ProductDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("nombre") val name: String? = null,
    @SerializedName("categoria") val category: String? = null,
    @SerializedName("material") val material: String? = null,
    @SerializedName("ancho") val widthCm: Int? = null,
    @SerializedName("largo") val lengthCm: Int? = null,
    @SerializedName("precio") val price: Int? = null,
    @SerializedName("descripcion") val description: String? = null,
    @SerializedName("imagenUrl") val imageUrl: String? = null
)

data class OrderDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("productoId") val productId: Long? = null,
    @SerializedName("cliente") val clientAlias: String? = null,
    @SerializedName("cantidad") val quantity: Int? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("estado") val status: String? = null,
    @SerializedName("fecha") val createdMillis: Long? = null,
    @SerializedName("notas") val notes: String? = null
)

/** Convierte la respuesta de la API en una entidad local; null si faltan datos mínimos. */
fun ProductDto.toEntity(existing: ProductEntity?): ProductEntity? {
    val remote = id ?: return null
    val title = name?.takeIf { it.isNotBlank() } ?: return null
    return ProductEntity(
        id = existing?.id ?: 0,
        remoteId = remote,
        name = title,
        category = category ?: existing?.category ?: "Decoración de Interiores",
        material = material ?: existing?.material ?: "Algodón",
        widthCm = widthCm ?: existing?.widthCm ?: 0,
        lengthCm = lengthCm ?: existing?.lengthCm ?: 0,
        price = price ?: existing?.price ?: 0,
        description = description ?: existing?.description.orEmpty(),
        photoPath = existing?.photoPath,
        imageUrl = imageUrl ?: existing?.imageUrl
    )
}

fun ProductEntity.toDto() = ProductDto(
    id = remoteId, name = name, category = category, material = material,
    widthCm = widthCm, lengthCm = lengthCm, price = price,
    description = description, imageUrl = imageUrl
)

fun OrderEntity.toDto(productRemoteId: Long?) = OrderDto(
    productId = productRemoteId ?: productId,
    clientAlias = clientAlias,
    quantity = quantity,
    total = total,
    status = status,
    createdMillis = createdMillis,
    notes = notes
)
