package cl.louaraya.macrame.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/** Programa (y cancela) las alertas de campaña usando WorkManager. */
class CampaignScheduler(private val context: Context) {

    fun schedule(campaignId: Long, dueMillis: Long) {
        val delay = (dueMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<CampaignWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(CampaignWorker.KEY_CAMPAIGN_ID to campaignId))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("campaign_$campaignId", ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(campaignId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("campaign_$campaignId")
    }
}
