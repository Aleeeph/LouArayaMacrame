package cl.louaraya.macrame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.louaraya.macrame.data.local.CampaignEntity
import cl.louaraya.macrame.data.prefs.UserPreferences
import cl.louaraya.macrame.data.repository.CampaignRepository
import cl.louaraya.macrame.domain.Catalog
import cl.louaraya.macrame.domain.Validators
import cl.louaraya.macrame.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CampaignFormState(
    val title: String = "",
    val category: String = "",
    val message: String = "",
    val dateMillis: Long? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val titleError: String? = null,
    val categoryError: String? = null,
    val messageError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null
) {
    val isValid: Boolean
        get() = Validators.campaignTitle(title) == null &&
            Validators.category(category) == null &&
            Validators.campaignMessage(message) == null &&
            Validators.date(dateMillis) == null &&
            Validators.time(hour, minute) == null &&
            Validators.futureDateTime(dateMillis, hour, minute) == null
}

class CampaignsViewModel(
    prefs: UserPreferences,
    private val repo: CampaignRepository
) : ViewModel() {

    val campaigns: StateFlow<List<CampaignEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isAdmin: StateFlow<Boolean> = prefs.role.map { it == Catalog.ROLE_ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _form = MutableStateFlow(CampaignFormState())
    val form: StateFlow<CampaignFormState> = _form.asStateFlow()

    fun onTitleChange(v: String) = _form.update { it.copy(title = v, titleError = Validators.campaignTitle(v)) }
    fun onCategoryChange(v: String) = _form.update { it.copy(category = v, categoryError = Validators.category(v)) }
    fun onMessageChange(v: String) = _form.update { it.copy(message = v, messageError = Validators.campaignMessage(v)) }

    fun onDateChange(v: Long) = _form.update {
        it.copy(
            dateMillis = v,
            dateError = Validators.date(v),
            timeError = Validators.futureDateTime(v, it.hour, it.minute)
        )
    }

    fun onTimeChange(h: Int, m: Int) = _form.update {
        it.copy(
            hour = h,
            minute = m,
            timeError = Validators.time(h, m) ?: Validators.futureDateTime(it.dateMillis, h, m)
        )
    }

    fun resetForm() {
        _form.value = CampaignFormState()
    }

    fun save(onSaved: () -> Unit) {
        val f = _form.value
        if (!f.isValid) {
            _form.value = f.copy(
                titleError = Validators.campaignTitle(f.title),
                categoryError = Validators.category(f.category),
                messageError = Validators.campaignMessage(f.message),
                dateError = Validators.date(f.dateMillis),
                timeError = Validators.time(f.hour, f.minute)
                    ?: Validators.futureDateTime(f.dateMillis, f.hour, f.minute)
            )
            return
        }
        viewModelScope.launch {
            repo.save(
                CampaignEntity(
                    category = f.category,
                    title = f.title.trim(),
                    message = f.message.trim(),
                    dueMillis = Formatters.combineDateTime(f.dateMillis!!, f.hour!!, f.minute!!)
                )
            )
            _form.value = CampaignFormState()
            onSaved()
        }
    }

    fun delete(campaign: CampaignEntity) {
        viewModelScope.launch { repo.delete(campaign) }
    }
}
