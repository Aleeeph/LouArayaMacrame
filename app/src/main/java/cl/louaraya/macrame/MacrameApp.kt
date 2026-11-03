package cl.louaraya.macrame

import android.app.Application
import cl.louaraya.macrame.di.AppContainer
import cl.louaraya.macrame.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MacrameApp : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)
        // Matriz de prueba: piezas y campañas sintéticas la primera vez.
        appScope.launch {
            container.catalogRepo.seedIfEmpty()
            container.campaignRepo.seedIfEmpty()
        }
    }
}
