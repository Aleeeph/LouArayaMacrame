package cl.louaraya.macrame

import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.domain.OrderRules
import cl.louaraya.macrame.domain.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class ValidatorsTest {

    private fun utcMillis(y: Int, m: Int, d: Int): Long =
        LocalDate.of(y, m, d).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun email_valido_no_retorna_error() {
        assertNull(Validators.email("ana@correo.cl"))
    }

    @Test
    fun email_invalido_retorna_mensaje() {
        assertEquals("Ingresa un correo válido", Validators.email("ana@correo"))
        assertEquals("El correo es obligatorio", Validators.email("  "))
    }

    @Test
    fun password_requiere_8_caracteres_y_un_numero() {
        assertNotNull(Validators.password("abc123"))
        assertNotNull(Validators.password("abcdefghi"))
        assertNull(Validators.password("abcdefg1"))
    }

    @Test
    fun confirmacion_debe_coincidir() {
        assertEquals("Las contraseñas no coinciden", Validators.confirmPassword("abcdefg1", "abcdefg2"))
        assertNull(Validators.confirmPassword("abcdefg1", "abcdefg1"))
    }

    @Test
    fun nombre_de_diseno_valida_largo_y_caracteres() {
        assertNotNull(Validators.productName(""))
        assertNotNull(Validators.productName("ab"))
        assertNotNull(Validators.productName("Tapiz $%&"))
        assertNull(Validators.productName("Tapiz Aurora Boho"))
    }

    @Test
    fun dimensiones_deben_estar_entre_5_y_500_cm() {
        assertNull(Validators.dimension("60", "ancho"))
        assertNotNull(Validators.dimension("4", "ancho"))
        assertNotNull(Validators.dimension("501", "largo"))
        assertNotNull(Validators.dimension("abc", "largo"))
        assertEquals("El ancho es obligatorio", Validators.dimension("", "ancho"))
    }

    @Test
    fun precio_solo_acepta_enteros_en_rango() {
        assertNull(Validators.price("54900"))
        assertNotNull(Validators.price("500"))
        assertNotNull(Validators.price("9000000"))
        assertNotNull(Validators.price("12,5"))
    }

    @Test
    fun cantidad_debe_estar_entre_1_y_20() {
        assertNull(Validators.quantity("1"))
        assertNull(Validators.quantity("20"))
        assertNotNull(Validators.quantity("0"))
        assertNotNull(Validators.quantity("21"))
        assertNotNull(Validators.quantity(""))
    }

    @Test
    fun fecha_de_evento_no_puede_ser_pasada() {
        val now = utcMillis(2026, 10, 29) + 12 * 3_600_000L
        assertNull(Validators.eventDate(utcMillis(2026, 12, 1), now))
        assertEquals("La fecha no puede ser pasada", Validators.eventDate(utcMillis(2026, 1, 1), now))
        assertNotNull(Validators.eventDate(null, now))
    }

    @Test
    fun perfil_es_obligatorio_y_se_traduce_a_codigo() {
        assertNotNull(Validators.role(""))
        assertEquals(Catalog.ROLE_ADMIN, Catalog.roleCode("Dueña / Administradora"))
        assertEquals(Catalog.ROLE_CLIENT, Catalog.roleCode("Cliente / Planificador de eventos"))
    }

    @Test
    fun total_y_codigos_del_pedido() {
        assertEquals(109_800, OrderRules.total(54_900, 2))
        assertEquals("MAC-007", OrderRules.productCode(7))
        assertEquals("CLI-0012", OrderRules.clientAlias(12))
    }
}
