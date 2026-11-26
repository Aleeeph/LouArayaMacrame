package cl.louaraya.macrame

import cl.louaraya.macrame.domain.QrProductCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrProductCodeTest {
    @Test fun readsCatalogCode() {
        assertEquals(1L, QrProductCode.productIdFrom("MAC-001"))
    }

    @Test fun readsCodeInsideUrlOrText() {
        assertEquals(27L, QrProductCode.productIdFrom("https://louaraya.cl/pieza/MAC-000027"))
    }

    @Test fun rejectsUnknownOrEmptyQr() {
        assertNull(QrProductCode.productIdFrom(null))
        assertNull(QrProductCode.productIdFrom(""))
        assertNull(QrProductCode.productIdFrom("https://example.com"))
        assertNull(QrProductCode.productIdFrom("MAC-000"))
    }
}
