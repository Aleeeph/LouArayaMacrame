package cl.louaraya.macrame.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Gestiona las fotos de piezas tomadas con la cámara (almacenamiento interno de la app). */
object PhotoStorage {
    fun newPhotoFile(context: Context): File {
        val dir = File(context.filesDir, "product_photos").apply { mkdirs() }
        return File(dir, "pieza_${System.currentTimeMillis()}.jpg")
    }

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
