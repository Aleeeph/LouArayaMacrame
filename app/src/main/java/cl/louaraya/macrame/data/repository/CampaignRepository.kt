package cl.louaraya.macrame.data.repository

import cl.louaraya.macrame.data.local.CampaignDao
import cl.louaraya.macrame.data.local.CampaignEntity
import cl.louaraya.macrame.domain.SeedData
import cl.louaraya.macrame.notifications.CampaignScheduler
import kotlinx.coroutines.flow.Flow

class CampaignRepository(
    private val dao: CampaignDao,
    private val scheduler: CampaignScheduler
) {
    fun observeAll(): Flow<List<CampaignEntity>> = dao.observeAll()

    suspend fun save(campaign: CampaignEntity) {
        val id = dao.upsert(campaign)
        scheduler.schedule(if (campaign.id == 0L) id else campaign.id, campaign.dueMillis)
    }

    suspend fun delete(campaign: CampaignEntity) {
        dao.delete(campaign)
        scheduler.cancel(campaign.id)
    }

    suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            SeedData.campaigns().forEach { save(it) }
        }
    }
}
