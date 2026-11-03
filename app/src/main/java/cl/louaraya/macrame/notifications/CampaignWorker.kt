package cl.louaraya.macrame.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.louaraya.macrame.data.local.AppDatabase
import cl.louaraya.macrame.data.prefs.UserPreferences
import kotlinx.coroutines.flow.first

class CampaignWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_CAMPAIGN_ID, -1L)
        val dao = AppDatabase.get(applicationContext).campaignDao()
        val campaign = dao.getById(id) ?: return Result.success() // la campaña fue eliminada
        if (campaign.sent) return Result.success()
        if (!UserPreferences(applicationContext).notificationsEnabled.first()) return Result.success()

        NotificationHelper.show(
            context = applicationContext,
            id = (id + 1000).toInt(),
            title = "${campaign.title} · ${campaign.category}",
            text = campaign.message
        )
        dao.markSent(id)
        return Result.success()
    }

    companion object {
        const val KEY_CAMPAIGN_ID = "campaign_id"
    }
}
