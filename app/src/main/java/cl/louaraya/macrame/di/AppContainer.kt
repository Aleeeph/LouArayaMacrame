package cl.louaraya.macrame.di

import android.content.Context
import cl.louaraya.macrame.data.local.AppDatabase
import cl.louaraya.macrame.data.prefs.UserPreferences
import cl.louaraya.macrame.data.remote.ApiClient
import cl.louaraya.macrame.data.repository.AuthRepository
import cl.louaraya.macrame.data.repository.CampaignRepository
import cl.louaraya.macrame.data.repository.CatalogRepository
import cl.louaraya.macrame.data.repository.OrderRepository
import cl.louaraya.macrame.notifications.CampaignScheduler
import cl.louaraya.macrame.notifications.NotificationHelper
import cl.louaraya.macrame.domain.OrderRules

/** Contenedor de dependencias manual (suficiente para este proyecto, sin librerías extra). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val db = AppDatabase.get(appContext)
    val prefs = UserPreferences(appContext)
    val authRepo = AuthRepository(db.userDao(), prefs)
    val catalogRepo = CatalogRepository(db.productDao(), db.favoriteDao(), ApiClient.api)
    val orderRepo = OrderRepository(db.orderDao(), db.productDao(), ApiClient.api) { orderId, productName ->
        NotificationHelper.show(
            appContext,
            id = orderId.toInt(),
            title = "Solicitud registrada",
            text = "Tu solicitud de «$productName» quedó pendiente de confirmación."
        )
    }
    val campaignRepo = CampaignRepository(db.campaignDao(), CampaignScheduler(appContext))
}
