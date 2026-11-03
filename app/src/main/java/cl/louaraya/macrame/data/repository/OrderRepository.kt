package cl.louaraya.macrame.data.repository

import cl.louaraya.macrame.data.local.OrderDao
import cl.louaraya.macrame.data.local.OrderEntity
import cl.louaraya.macrame.data.local.ProductDao
import cl.louaraya.macrame.data.remote.MacrameApi
import cl.louaraya.macrame.data.remote.toDto
import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.domain.OrderRules
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class OrderRepository(
    private val orderDao: OrderDao,
    private val productDao: ProductDao,
    private val api: MacrameApi,
    private val onOrderCreated: (orderId: Long, productName: String) -> Unit
) {
    fun observeByUser(userId: Long): Flow<List<OrderEntity>> = orderDao.observeByUser(userId)
    fun observeAll(): Flow<List<OrderEntity>> = orderDao.observeAll()

    /** Registra la solicitud (estado Pendiente) y la envía a la API si hay conexión. */
    suspend fun createOrder(
        userId: Long,
        productId: Long,
        quantity: Int,
        eventMillis: Long?,
        notes: String
    ): Long {
        val product = productDao.getById(productId) ?: return -1L
        val order = OrderEntity(
            productId = productId,
            userId = userId,
            clientAlias = OrderRules.clientAlias(userId),
            createdMillis = System.currentTimeMillis(),
            eventMillis = eventMillis,
            quantity = quantity,
            total = OrderRules.total(product.price, quantity),
            status = Catalog.STATUS_PENDING,
            notes = notes
        )
        val id = orderDao.insert(order)
        push(order.copy(id = id), product.remoteId)
        onOrderCreated(id, product.name)
        return id
    }

    suspend fun setStatus(order: OrderEntity, status: String) {
        orderDao.setStatus(order.id, status)
        val product = productDao.getById(order.productId)
        pushUpdate(order.copy(status = status), product?.remoteId)
    }

    suspend fun delete(order: OrderEntity) = orderDao.delete(order)

    private suspend fun push(order: OrderEntity, productRemoteId: Long?) {
        try {
            api.createOrder(order.toDto(productRemoteId))
            orderDao.setSynced(order.id, true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // queda pendiente de sincronizar (synced = false)
        }
    }

    private suspend fun pushUpdate(order: OrderEntity, productRemoteId: Long?) {
        try {
            api.updateOrder(order.id, order.toDto(productRemoteId))
            orderDao.setSynced(order.id, true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // se mantiene el cambio local
        }
    }
}
