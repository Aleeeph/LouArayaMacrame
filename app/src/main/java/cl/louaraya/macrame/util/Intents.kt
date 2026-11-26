package cl.louaraya.macrame.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

object Intents {
    /** Recurso nativo extra: comparte una pieza del catálogo con otras apps. */
    fun shareText(context: Context, subject: String, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, "Compartir pieza"))
    }

    /** Abre una app de mapas; si no existe, intenta OpenStreetMap y luego Google Maps en navegador. */
    fun openNearbyFairs(context: Context) {
        val attempts = listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=feria+artesanal")),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/search?query=feria%20artesanal%20cerca")),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/feria+artesanal+cerca"))
        )
        for (intent in attempts) {
            try {
                context.startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                // se intenta con la siguiente opción
            }
        }
        Toast.makeText(context, "No se encontró una app para abrir mapas", Toast.LENGTH_SHORT).show()
    }

    fun openAppSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        )
    }
}
