package cl.louaraya.macrame.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserEntity?>
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun observe(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(product: ProductEntity): Long

    @Delete
    suspend fun delete(product: ProductEntity)
}

@Dao
interface FavoriteDao {
    @Query("SELECT productId FROM favorites WHERE userId = :userId")
    fun observeIds(userId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId AND productId = :productId")
    suspend fun exists(userId: Long, productId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND productId = :productId")
    suspend fun remove(userId: Long, productId: Long)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdMillis DESC")
    fun observeByUser(userId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdMillis DESC")
    fun observeAll(): Flow<List<OrderEntity>>

    @Insert
    suspend fun insert(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status, synced = 0 WHERE id = :id")
    suspend fun setStatus(id: Long, status: String)

    @Query("UPDATE orders SET synced = :synced WHERE id = :id")
    suspend fun setSynced(id: Long, synced: Boolean)

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OrderEntity?

    @Delete
    suspend fun delete(order: OrderEntity)
}

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY dueMillis ASC")
    fun observeAll(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CampaignEntity?

    @Query("SELECT COUNT(*) FROM campaigns")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(campaign: CampaignEntity): Long

    @Delete
    suspend fun delete(campaign: CampaignEntity)

    @Query("UPDATE campaigns SET sent = 1 WHERE id = :id")
    suspend fun markSent(id: Long)
}
