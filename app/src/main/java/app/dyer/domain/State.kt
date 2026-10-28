package app.dyer.domain

import java.time.ZoneId

/** A jar on the shelf: a plant someone has eaten at least once. */
class Jar(val id: String, val times: Int, val first: Long, val last: Long)

/** Eight tries of a plant someone doesn't like yet: its paint starts muddy and clears with each meal it's in. */
const val TRIES = 8

/** Everything the dyehouse remembers, and what follows from it. */
class State(
    val meals: List<Meal>,
    val own: List<Own>,
    val dishes: List<Dish>,
    val quiet: List<Quiet>,
    base: Catalogue,
    val firstWeek: Long,
    val goal: Int,
    /** The plant in its eight tries, if any. */
    val trialPlant: String?,
    val trialStart: Long,
    /** Plants already tried eight times: their own colour now. */
    val trialsDone: Set<String>,
    val zone: ZoneId,
) {
    val cat: Catalogue = base.with(own.map { it.plant() })

    private val cache = HashMap<Long, WeekPaint>()

    fun quarter(week: Long) = Quarter.of(week, firstWeek)

    fun week(w: Long): WeekPaint = cache.getOrPut(w) {
        val end = w + 7
        WeekPaint.compute(
            w, quarter(w),
            meals.filter { it.at.day(zone) in w until end },
            quiet.filter { it.at.day(zone) in w until end },
            cat, goal,
        )
    }

    /** Weeks with something in them, newest first, from the first week on. */
    val weeks: List<Long> by lazy {
        (meals.map { it.at.week(zone) } + quiet.map { it.at.week(zone) }).filter { it >= firstWeek }.distinct().sortedDescending()
    }

    /** Every entry ever eaten; a ferment also puts its plant on the shelf. */
    val jars: Map<String, Jar> by lazy {
        val seen = HashMap<String, MutableList<Long>>()
        for (m in meals) for (id in m.ids.toSet()) {
            val p = cat[id] ?: continue
            seen.getOrPut(id) { mutableListOf() } += m.at
            cat.counted(p)?.takeIf { it.id != id }?.let { seen.getOrPut(it.id) { mutableListOf() } += m.at }
        }
        seen.mapValues { (id, at) -> Jar(id, at.size, at.min(), at.max()) }
    }

    /** Meals with the trial plant since its tries began. */
    val tries: Int by lazy {
        val p = trialPlant ?: return@lazy 0
        meals.count { it.at >= trialStart && p in it.ids }.coerceAtMost(TRIES)
    }

    /** How muddy the trial plant's paint still is: .8 at the first try, clear at the eighth. */
    val mud: Float get() = if (trialPlant == null || tries == 0) 0f else .8f * (TRIES - tries) / (TRIES - 1)

    /** Weeks that reached the goal, per quarter. */
    val festivals: Map<Quarter, Int> by lazy { weeks.map(::week).filter { it.festival }.groupingBy { it.quarter }.eachCount() }

    /** Every quarter has had its festival: the Gloom itself can be painted. */
    val finaleReady: Boolean get() = Quarter.entries.all { (festivals[it] ?: 0) > 0 }

    /** Plants never eaten before [week] that first appeared in it. */
    fun newIn(w: WeekPaint): List<String> {
        val start = weekStart(w.week, zone)
        val before = meals.filter { it.at < start }.flatMap { it.ids }.toSet()
        return w.entries.filter { it !in before }
    }
}
