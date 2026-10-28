package app.dyer.domain

/** Twenty quiet minutes draw one small detail. */
const val QUIET_MINUTES = 20f

/** How far a plate's paint spreads: a quarter of the plate gives a thin brush, half a middling one, nearly all a wide one. */
object Brush {
    fun strength(plate: Float): Float = when {
        plate <= .3f -> .5f
        plate <= .6f -> .8f
        else -> 1f
    }
}

/** One plant's paint going onto one thing. */
class Wash(
    val plant: String,
    val slot: String,
    val amount: Float,
    val meal: Long,
    /** Paint touched this thing for the first time this week. */
    val first: Boolean,
    /** This wash finished the thing. */
    val filled: Boolean,
    /** The thing was already done: a second coat. */
    val extra: Boolean,
)

/** A week's painting of its quarter, worked out from the week's plates in order. */
class WeekPaint(
    val week: Long,
    val quarter: Quarter,
    val meals: List<Meal>,
    /** Paint on each thing, in washes. */
    val paint: Map<String, Float>,
    val washes: List<Wash>,
    /** Everything on the week's plates, once each, in order. */
    val entries: List<String>,
    /** The plants counted towards the week, once each, in order. */
    val plants: List<String>,
    val total: Float,
    val pigments: Set<Pigment>,
    val families: Set<String>,
    /** Something fermented was eaten: the week's colours are fixed. */
    val fixed: Boolean,
    val quietMinutes: Float,
    val goal: Int,
) {
    val festival get() = total >= goal
    val empty get() = meals.isEmpty()

    /** How much of a thing is painted, 0..1. */
    fun cover(slot: Slot): Float = ((paint[slot.id] ?: 0f) / slot.need).coerceIn(0f, 1f)
    fun cover(id: String): Float = cover(quarter.slot(id))

    /** The share of a thing's paint that came from [plant]. */
    fun share(slot: String, plant: String): Float {
        val all = washes.filter { it.slot == slot && !it.extra }.sumOf { it.amount.toDouble() }
        if (all <= 0) return 0f
        return (washes.filter { it.slot == slot && it.plant == plant && !it.extra }.sumOf { it.amount.toDouble() } / all).toFloat()
    }

    /** Quiet-meal details drawn so far: 1.5 is one done and the next half drawn. */
    val details: Float get() = (quietMinutes / QUIET_MINUTES).coerceAtMost(quarter.details.size.toFloat())

    val painted: Float get() = quarter.slots.sumOf { cover(it).toDouble() }.toFloat() / quarter.slots.size

    companion object {
        fun compute(week: Long, quarter: Quarter, meals: List<Meal>, quiet: List<Quiet>, cat: Catalogue, goal: Int): WeekPaint {
            val paint = quarter.slots.associate { it.id to 0f }.toMutableMap()
            val washes = mutableListOf<Wash>()
            val entries = LinkedHashSet<String>()
            val plants = LinkedHashSet<String>()
            val pigments = mutableSetOf<Pigment>()
            val families = mutableSetOf<String>()
            var total = 0f
            var fixed = false
            for (meal in meals.sortedWith(compareBy({ it.at }, { it.id }))) {
                val brush = Brush.strength(meal.plate)
                for (id in meal.ids) {
                    val entry = cat[id] ?: continue
                    entries += id
                    if (entry.ferment) fixed = true
                    val p = cat.counted(entry) ?: continue
                    if (!plants.add(p.id)) continue
                    total += p.kind.value
                    p.pigment?.let { pigments += it }
                    if (p.family != "own") families += p.family
                    val pig = p.pigment ?: continue
                    val amount = brush * (if (p.kind.drop) .25f else if (p.kind == Kind.REFINED) .5f else 1f)
                    val choices = quarter.slots.filter { it.pigment == pig }.ifEmpty { quarter.slots }
                    val slot = choices.minBy { (paint[it.id] ?: 0f) / it.need }
                    val before = paint[slot.id] ?: 0f
                    val after = (before + amount).coerceAtMost(slot.need)
                    paint[slot.id] = after
                    washes += Wash(p.id, slot.id, after - before, meal.id, first = before == 0f, filled = before < slot.need && after >= slot.need, extra = before >= slot.need)
                }
            }
            return WeekPaint(
                week, quarter, meals, paint, washes, entries.toList(), plants.toList(), total, pigments, families, fixed,
                quiet.sumOf { it.minutes.toDouble() }.toFloat(), goal,
            )
        }
    }
}
