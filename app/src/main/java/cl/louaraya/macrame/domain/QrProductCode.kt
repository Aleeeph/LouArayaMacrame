package cl.louaraya.macrame.domain

/**
 * Convierte códigos QR del catálogo (por ejemplo MAC-001) al identificador local.
 * El código numérico corresponde al correlativo de las piezas de demostración.
 */
object QrProductCode {
    private val productCode = Regex("""(?i)(?:^|\b)MAC-(\d{1,6})(?:\b|$)""")

    fun productIdFrom(rawValue: String?): Long? {
        if (rawValue.isNullOrBlank()) return null
        val match = productCode.find(rawValue.trim()) ?: return null
        return match.groupValues[1].toLongOrNull()?.takeIf { it > 0L }
    }
}
