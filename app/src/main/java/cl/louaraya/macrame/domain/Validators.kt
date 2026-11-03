package cl.louaraya.macrame.domain

import cl.louaraya.macrame.util.Formatters
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Reglas de validación de todos los formularios.
 * Cada función devuelve el mensaje de error, o null si el valor es válido.
 * Viven fuera de la UI para poder probarlas con tests unitarios.
 */
object Validators {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun personName(value: String): String? {
        val v = value.trim()
        return when {
            v.isEmpty() -> "El nombre es obligatorio"
            v.length < 2 -> "Mínimo 2 caracteres"
            v.length > 30 -> "Máximo 30 caracteres"
            !v.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras y espacios"
            else -> null
        }
    }

    fun email(value: String): String? {
        val v = value.trim()
        return when {
            v.isEmpty() -> "El correo es obligatorio"
            !EMAIL_REGEX.matches(v) -> "Ingresa un correo válido"
            else -> null
        }
    }

    fun password(value: String): String? = when {
        value.isEmpty() -> "La contraseña es obligatoria"
        value.length < 8 -> "Mínimo 8 caracteres"
        value.none { it.isDigit() } -> "Debe incluir al menos un número"
        else -> null
    }

    fun loginPassword(value: String): String? =
        if (value.isEmpty()) "La contraseña es obligatoria" else null

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Confirma tu contraseña"
        confirm != password -> "Las contraseñas no coinciden"
        else -> null
    }

    fun role(value: String): String? =
        if (value.isBlank()) "Selecciona tu perfil" else null

    // ---------- Ficha de producto ----------

    fun productName(value: String): String? {
        val v = value.trim()
        return when {
            v.isEmpty() -> "El nombre del diseño es obligatorio"
            v.length < 3 -> "Mínimo 3 caracteres"
            v.length > 40 -> "Máximo 40 caracteres"
            !v.all { it.isLetterOrDigit() || it == ' ' || it == '-' } -> "Usa solo letras, números o espacios"
            else -> null
        }
    }

    fun category(value: String): String? =
        if (value.isBlank()) "Selecciona una categoría" else null

    fun material(value: String): String? =
        if (value.isBlank()) "Selecciona un material" else null

    fun parseInt(value: String): Int? = value.trim().toIntOrNull()

    /** Largo o ancho en centímetros (5 a 500). */
    fun dimension(value: String, name: String): String? {
        if (value.isBlank()) return "El $name es obligatorio"
        val n = parseInt(value) ?: return "Ingresa un número entero"
        return when {
            n < 5 -> "El mínimo es 5 cm"
            n > 500 -> "El máximo es 500 cm"
            else -> null
        }
    }

    /** Precio ficticio en pesos chilenos. */
    fun price(value: String): String? {
        if (value.isBlank()) return "El precio es obligatorio"
        val n = parseInt(value) ?: return "Ingresa un monto válido (solo números)"
        return when {
            n < 1_000 -> "El precio mínimo es \$1.000"
            n > 5_000_000 -> "El precio máximo es \$5.000.000"
            else -> null
        }
    }

    fun description(value: String): String? =
        if (value.trim().length > 200) "Máximo 200 caracteres" else null

    // ---------- Solicitud / pedido ----------

    fun quantity(value: String): String? {
        if (value.isBlank()) return "Indica la cantidad"
        val n = parseInt(value) ?: return "Ingresa un número entero"
        return when {
            n < 1 -> "La cantidad mínima es 1"
            n > 20 -> "La cantidad máxima es 20"
            else -> null
        }
    }

    /** La fecha del evento (DatePicker, UTC) no puede ser anterior a hoy. */
    fun eventDate(millis: Long?, now: Long = System.currentTimeMillis()): String? {
        if (millis == null) return "Selecciona la fecha del evento"
        val today = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val picked: LocalDate = Formatters.utcDate(millis)
        return if (picked.isBefore(today)) "La fecha no puede ser pasada" else null
    }

    fun notes(value: String): String? =
        if (value.trim().length > 100) "Máximo 100 caracteres" else null

    // ---------- Campañas ----------

    fun campaignTitle(value: String): String? {
        val v = value.trim()
        return when {
            v.isEmpty() -> "El título es obligatorio"
            v.length < 3 -> "Mínimo 3 caracteres"
            v.length > 40 -> "Máximo 40 caracteres"
            else -> null
        }
    }

    fun campaignMessage(value: String): String? {
        val v = value.trim()
        return when {
            v.isEmpty() -> "El mensaje es obligatorio"
            v.length < 5 -> "Mínimo 5 caracteres"
            v.length > 100 -> "Máximo 100 caracteres"
            else -> null
        }
    }

    fun date(millis: Long?): String? =
        if (millis == null) "Selecciona la fecha" else null

    fun time(hour: Int?, minute: Int?): String? =
        if (hour == null || minute == null) "Selecciona la hora" else null

    /** Solo valida cuando fecha y hora ya fueron elegidas. */
    fun futureDateTime(dateMillis: Long?, hour: Int?, minute: Int?, now: Long = System.currentTimeMillis()): String? {
        if (dateMillis == null || hour == null || minute == null) return null
        return if (Formatters.combineDateTime(dateMillis, hour, minute) <= now) {
            "La fecha y hora deben ser futuras"
        } else {
            null
        }
    }
}
