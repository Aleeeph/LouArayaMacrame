package cl.louaraya.macrame.domain

/** Catálogos fijos del negocio (categorías de campaña, materiales, estados y perfiles). */
object Catalog {
    val categories = listOf("Matrimonios", "Festivales", "Decoración de Interiores", "Viajes")
    val materials = listOf("Algodón", "Cáñamo", "Yute", "Lino", "Mezcla de fibras")

    const val STATUS_EXPLORATION = "Exploración"
    const val STATUS_PENDING = "Pendiente"
    const val STATUS_CONFIRMED = "Confirmado"
    const val STATUS_DELIVERED = "Entregado"
    val statuses = listOf(STATUS_EXPLORATION, STATUS_PENDING, STATUS_CONFIRMED, STATUS_DELIVERED)

    const val ROLE_CLIENT = "CLIENTE"
    const val ROLE_ADMIN = "ADMIN"
    val roleLabels = linkedMapOf(
        ROLE_CLIENT to "Cliente / Planificador de eventos",
        ROLE_ADMIN to "Dueña / Administradora"
    )

    fun roleCode(label: String): String =
        roleLabels.entries.firstOrNull { it.value == label }?.key ?: ROLE_CLIENT
}
