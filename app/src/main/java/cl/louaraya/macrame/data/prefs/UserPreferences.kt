package cl.louaraya.macrame.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.louaraya.macrame.domain.Catalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "macrame_prefs")

/** Preferencias y sesión del usuario guardadas con DataStore. */
class UserPreferences(context: Context) {
    private val store = context.applicationContext.dataStore

    /** Id del usuario con sesión iniciada (null si no hay sesión). */
    val userId: Flow<Long?> = store.data.map { it[KEY_USER_ID] }

    /** Perfil de la sesión: [Catalog.ROLE_CLIENT] o [Catalog.ROLE_ADMIN]. */
    val role: Flow<String> = store.data.map { it[KEY_ROLE] ?: Catalog.ROLE_CLIENT }

    /** true/false si el usuario eligió un tema; null = seguir el del sistema. */
    val darkMode: Flow<Boolean?> = store.data.map { it[KEY_DARK] }

    val notificationsEnabled: Flow<Boolean> = store.data.map { it[KEY_NOTIFICATIONS] ?: true }

    suspend fun setSession(id: Long?, role: String?) {
        store.edit { prefs ->
            if (id == null) {
                prefs.remove(KEY_USER_ID)
                prefs.remove(KEY_ROLE)
            } else {
                prefs[KEY_USER_ID] = id
                prefs[KEY_ROLE] = role ?: Catalog.ROLE_CLIENT
            }
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        store.edit { it[KEY_DARK] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        store.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    private companion object {
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_DARK = booleanPreferencesKey("dark_mode")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
    }
}
