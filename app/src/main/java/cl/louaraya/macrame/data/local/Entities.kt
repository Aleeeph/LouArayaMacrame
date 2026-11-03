package cl.louaraya.macrame.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String
)

/** Ficha de producto. El id es el código correlativo (se muestra como MAC-001). */
@Entity(tableName = "products", indices = [Index("category"), Index("remoteId")])
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: Long? = null,
    val name: String,
    val category: String,
    val material: String,
    val widthCm: Int,
    val lengthCm: Int,
    val price: Int,
    val description: String,
    val photoPath: String? = null,
    val imageUrl: String? = null
)

/** Colección personal: piezas guardadas por cada usuario. */
@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class FavoriteEntity(val userId: Long, val productId: Long)

/** Registro de pedido / interacción. El cliente se guarda como alias anonimizado. */
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId"), Index("userId")]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val userId: Long,
    val clientAlias: String,
    val createdMillis: Long,
    val eventMillis: Long?,
    val quantity: Int,
    val total: Int,
    val status: String,
    val notes: String,
    val synced: Boolean = false
)

/** Alerta promocional de campaña (se muestra como notificación en la fecha indicada). */
@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val title: String,
    val message: String,
    val dueMillis: Long,
    val sent: Boolean = false
)
