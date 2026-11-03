package cl.louaraya.macrame.data.repository

import cl.louaraya.macrame.data.local.FavoriteDao
import cl.louaraya.macrame.data.local.FavoriteEntity
import cl.louaraya.macrame.data.local.ProductDao
import cl.louaraya.macrame.data.local.ProductEntity
import cl.louaraya.macrame.data.remote.MacrameApi
import cl.louaraya.macrame.data.remote.toDto
import cl.louaraya.macrame.data.remote.toEntity
import cl.louaraya.macrame.domain.SeedData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

/**
 * Catálogo: Room es la fuente de verdad (funciona sin conexión) y la API REST
 * se usa para sincronizar. Si la red falla, la app sigue con los datos locales.
 */
class CatalogRepository(
    private val productDao: ProductDao,
    private val favoriteDao: FavoriteDao,
    private val api: MacrameApi
) {
    fun observeProducts(): Flow<List<ProductEntity>> = productDao.observeAll()
    fun observeProduct(id: Long): Flow<ProductEntity?> = productDao.observe(id)
    suspend fun getProduct(id: Long): ProductEntity? = productDao.getById(id)

    fun observeFavoriteIds(userId: Long): Flow<Set<Long>> =
        favoriteDao.observeIds(userId).map { it.toSet() }

    suspend fun toggleFavorite(userId: Long, productId: Long) {
        if (favoriteDao.exists(userId, productId) > 0) {
            favoriteDao.remove(userId, productId)
        } else {
            favoriteDao.add(FavoriteEntity(userId, productId))
        }
    }

    /** Guarda la pieza localmente y luego intenta reflejarla en el backend. */
    suspend fun saveProduct(product: ProductEntity) {
        val id = productDao.upsert(product)
        val saved = if (product.id == 0L) product.copy(id = id) else product
        try {
            if (saved.remoteId == null) {
                val created = api.createProduct(saved.toDto())
                created.id?.let { productDao.upsert(saved.copy(remoteId = it)) }
            } else {
                api.updateProduct(saved.remoteId, saved.toDto())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // sin conexión: el cambio queda guardado localmente
        }
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.delete(product) // favoritos y pedidos asociados se eliminan en cascada
        product.photoPath?.let { File(it).delete() }
    }

    /** Carga la matriz de prueba la primera vez que se abre la app. */
    suspend fun seedIfEmpty() {
        if (productDao.count() == 0) {
            SeedData.products.forEach { productDao.upsert(it) }
        }
    }

    /** Descarga el catálogo desde la API. Devuelve true si la sincronización funcionó. */
    suspend fun refreshFromApi(): Boolean = try {
        api.getProducts().forEach { dto ->
            val remoteId = dto.id ?: return@forEach
            val entity = dto.toEntity(productDao.getByRemoteId(remoteId))
            if (entity != null) productDao.upsert(entity)
        }
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }
}
