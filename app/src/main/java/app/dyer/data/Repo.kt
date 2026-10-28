package app.dyer.data

import app.dyer.domain.Catalogue
import app.dyer.domain.Dish
import app.dyer.domain.Meal
import app.dyer.domain.Own
import app.dyer.domain.Quiet
import app.dyer.domain.State
import app.dyer.domain.TRIES
import app.dyer.domain.week
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.ZoneId

/** The dyehouse ledger: plates, own plants, dishes and quiet meals in Room, read together with the settings. */
class Repo(db: DyerDb, private val settings: SettingsStore, private val catalogue: () -> Catalogue) {
    private val dao = db.dao()
    private val mutex = Mutex()
    private val _state = MutableStateFlow<State?>(null)
    val state: StateFlow<State?> = _state

    /** The clock; the demo seed winds it back to lay down past weeks. */
    var clock: () -> Long = System::currentTimeMillis
    var zone: ZoneId = ZoneId.systemDefault()

    /** While true, changes aren't shown, so the demo seed doesn't flicker through its weeks. */
    @Volatile var hold = false

    suspend fun refresh(): State {
        val st = settings.now()
        val now = clock()
        val s = State(
            dao.meals(), dao.own(), dao.dishes(), dao.quiet(), catalogue(),
            firstWeek = if (st.firstWeek >= 0) st.firstWeek else now.week(zone),
            goal = st.goal,
            trialPlant = st.trialPlant.ifBlank { null },
            trialStart = st.trialStart,
            trialsDone = st.trialsDone.split(' ').filter { it.isNotBlank() }.toSet(),
            zone = zone,
        )
        if (!hold) _state.value = s
        return s
    }

    private suspend fun <T> op(block: suspend (Long) -> T): T = mutex.withLock { block(clock()).also { refresh() } }

    /** One plate. The first plate ever starts the first week; the eighth try of the trial plant finishes it. */
    suspend fun meal(plants: List<String>, plate: Float, at: Long? = null): Long = op { now ->
        val t = at ?: now
        val id = dao.insert(Meal(at = t, plants = plants.distinct().joinToString(" "), plate = plate))
        val st0 = settings.now()
        val tries = if (st0.trialPlant.isBlank()) 0 else dao.meals().count { it.at >= st0.trialStart && st0.trialPlant in it.ids }
        settings.update { st ->
            var s = st.copy(plate = plate)
            if (s.firstWeek < 0) s = s.copy(firstWeek = t.week(zone))
            if (s.trialPlant.isNotBlank()) {
                if (tries >= TRIES) s = s.copy(trialsDone = (s.trialsDone.split(' ') + s.trialPlant).filter { it.isNotBlank() }.distinct().joinToString(" "), trialPlant = "", trialStart = 0)
            }
            s
        }
        id
    }

    suspend fun deleteMeal(id: Long) = op { dao.deleteMeal(id) }

    /** A plant of one's own; returns its id. */
    suspend fun own(name: String, pigment: Char, kind: String): String = op { now ->
        "own" + dao.insert(Own(name = name.trim().take(40), pigment = pigment.toString(), kind = kind, at = now))
    }

    suspend fun dish(name: String, plants: List<String>, plate: Float) = op { dao.insert(Dish(name = name.trim().take(40), plants = plants.distinct().joinToString(" "), plate = plate)) }

    suspend fun deleteDish(id: Long) = op { dao.deleteDish(id) }

    suspend fun quiet(minutes: Float, at: Long? = null) = op { now -> if (minutes > .1f) dao.insert(Quiet(at = at ?: now, minutes = minutes)) }

    suspend fun trial(plant: String?) = op { now -> settings.update { it.copy(trialPlant = plant ?: "", trialStart = if (plant != null) now else 0) } }
}
