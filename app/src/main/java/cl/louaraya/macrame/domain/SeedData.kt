package cl.louaraya.macrame.domain

import cl.louaraya.macrame.data.local.CampaignEntity
import cl.louaraya.macrame.data.local.ProductEntity
import java.util.concurrent.TimeUnit

/** Listado sintético de piezas y campañas (datos ficticios para el MVP). */
object SeedData {
    private fun p(name: String, cat: String, mat: String, w: Int, l: Int, price: Int, desc: String) =
        ProductEntity(
            name = name, category = cat, material = mat,
            widthCm = w, lengthCm = l, price = price, description = desc
        )

    val products = listOf(
        p("Tapiz Aurora Boho", "Decoración de Interiores", "Algodón", 60, 90, 54_900,
            "Tapiz mural con nudos en espiga y flecos largos, ideal para livings luminosos."),
        p("Cortina Ceremonia Nube", "Matrimonios", "Algodón", 120, 220, 189_900,
            "Telón de macramé para ceremonias; crea un fondo suave para fotografías."),
        p("Arco Floral Altar", "Matrimonios", "Cáñamo", 150, 200, 249_900,
            "Estructura de cáñamo trenzado pensada para ceremonias al aire libre."),
        p("Guirnalda Sol de Festival", "Festivales", "Yute", 30, 300, 39_900,
            "Guirnalda liviana para ambientar escenarios y stands de feria."),
        p("Colgante Plantas Selva", "Decoración de Interiores", "Algodón", 25, 110, 24_900,
            "Porta macetas de tres niveles con nudos cuadrados."),
        p("Atrapasueños Ruta Costera", "Viajes", "Cáñamo", 35, 70, 19_900,
            "Pieza compacta y resistente, perfecta como recuerdo de viaje."),
        p("Camino de Mesa Gala", "Festivales", "Lino", 40, 200, 34_900,
            "Camino de mesa con flecos para cenas de gala y muestras culturales."),
        p("Llavero Mochilero", "Viajes", "Cáñamo", 8, 15, 4_900,
            "Llavero pequeño de nudo plano, ideal como obsequio para invitados."),
        p("Cortina Puerta Zen", "Decoración de Interiores", "Mezcla de fibras", 90, 200, 79_900,
            "Cortina de puerta con patrón geométrico y cuentas de madera.")
    )

    fun campaigns(now: Long = System.currentTimeMillis()): List<CampaignEntity> = listOf(
        CampaignEntity(
            category = "Matrimonios", title = "Temporada de Matrimonios",
            message = "Reserva tu ambientación de ceremonia con anticipación.",
            dueMillis = now + TimeUnit.DAYS.toMillis(3)
        ),
        CampaignEntity(
            category = "Festivales", title = "Festival de Verano",
            message = "Nuevas guirnaldas y caminos de mesa para tu festival.",
            dueMillis = now + TimeUnit.DAYS.toMillis(10)
        ),
        CampaignEntity(
            category = "Viajes", title = "Colección Viajes",
            message = "Llaveros y atrapasueños para tu próxima aventura.",
            dueMillis = now + TimeUnit.DAYS.toMillis(20)
        )
    )
}
