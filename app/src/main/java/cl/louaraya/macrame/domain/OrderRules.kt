package cl.louaraya.macrame.domain

/** Reglas de negocio de pedidos, sin dependencias de Android (fáciles de probar). */
object OrderRules {
    fun total(price: Int, quantity: Int): Int = price * quantity

    fun clientAlias(userId: Long): String = "CLI-%04d".format(userId)

    fun productCode(id: Long): String = "MAC-%03d".format(id)
}
