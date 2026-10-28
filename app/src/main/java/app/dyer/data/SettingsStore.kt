package app.dyer.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    /** Plants a week needs for its festival: 30, or 15 in a hard week. */
    val goal: Int = 30,
    /** The evening «Что сегодня было на тарелке?»; -1 for none. */
    val bellHour: Int = 20,
    /** The Monday of the first week, as an epoch day; -1 until the first plate. */
    val firstWeek: Long = -1,
    /** The plate share the meal sheet starts on: the last one chosen. */
    val plate: Float = .5f,
    /** The plant in its eight tries, and when they began. */
    val trialPlant: String = "",
    val trialStart: Long = 0,
    /** Plants already tried eight times, space-separated. */
    val trialsDone: String = "",
    /** The last week whose report has been shown. */
    val reportedWeek: Long = -1,
    /** When a quiet meal began; 0 when none is going on. */
    val quietSince: Long = 0,
    /** The Gloom has been painted. */
    val finaleSeen: Boolean = false,
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        goal = this[GOAL] ?: 30,
        bellHour = this[BELL_HOUR] ?: 20,
        firstWeek = this[FIRST_WEEK] ?: -1,
        plate = this[PLATE] ?: .5f,
        trialPlant = this[TRIAL] ?: "",
        trialStart = this[TRIAL_START] ?: 0,
        trialsDone = this[TRIALS_DONE] ?: "",
        reportedWeek = this[REPORTED] ?: -1,
        quietSince = this[QUIET] ?: 0,
        finaleSeen = this[FINALE] ?: false,
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[GOAL] = s.goal
            it[BELL_HOUR] = s.bellHour
            it[FIRST_WEEK] = s.firstWeek
            it[PLATE] = s.plate
            it[TRIAL] = s.trialPlant
            it[TRIAL_START] = s.trialStart
            it[TRIALS_DONE] = s.trialsDone
            it[REPORTED] = s.reportedWeek
            it[QUIET] = s.quietSince
            it[FINALE] = s.finaleSeen
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val GOAL = intPreferencesKey("goal")
        val BELL_HOUR = intPreferencesKey("bell_hour")
        val FIRST_WEEK = longPreferencesKey("first_week")
        val PLATE = floatPreferencesKey("plate")
        val TRIAL = stringPreferencesKey("trial")
        val TRIAL_START = longPreferencesKey("trial_start")
        val TRIALS_DONE = stringPreferencesKey("trials_done")
        val REPORTED = longPreferencesKey("reported_week")
        val QUIET = longPreferencesKey("quiet_since")
        val FINALE = booleanPreferencesKey("finale_seen")
    }
}
