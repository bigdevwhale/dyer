package app.dyer.data

import app.dyer.AppContainer
import app.dyer.domain.week
import app.dyer.domain.weekStart

/**
 * The demo's history: four painted weeks (the market, the mill, the harbour that fell short, the castle)
 * and this week's market half done, with beetroot five tries into its eight and a couple of saved dishes.
 */
object DemoSeed {
    private val past = listOf(
        // Market Square: a festival.
        listOf(
            listOf("oats", "strawberry", "walnut", "flax") to .5f, listOf("tomato", "cucumber", "pepper", "onion", "dill") to 1f,
            listOf("buckwheat", "champignon", "carrot") to .5f, listOf("apple", "kefir") to 1f,
            listOf("beet", "cabbage", "potato", "garlic", "bay") to 1f, listOf("banana", "hazelnut") to 1f,
            listOf("lentil", "spinach", "lemon", "cumin") to 1f, listOf("rye", "redcabbage", "parsley") to .5f,
            listOf("blueberry", "plum", "yogurt") to 1f, listOf("zucchini", "eggplant", "basil", "tomato") to 1f,
            listOf("pumpkin", "millet", "cinnamon") to .5f, listOf("chickpea", "sesame", "corn", "orange", "peas") to 1f,
        ),
        // The Mill: a festival.
        listOf(
            listOf("oats", "raspberry", "almond") to .5f, listOf("cabbage", "carrot", "sunflower", "apple") to 1f,
            listOf("barley", "porcini", "onion", "dill") to .5f, listOf("pear", "sauerkraut") to 1f,
            listOf("tomato", "radish", "lettuce", "greenonion") to 1f, listOf("buckwheat", "beans", "garlic") to .5f,
            listOf("pumpkin", "millet", "quince") to 1f, listOf("plum", "blackcurrant", "mint", "tea") to 1f,
            listOf("potato", "chanterelle", "parsley") to .5f, listOf("cranberry", "beet", "walnut") to 1f,
            listOf("rye", "corn", "peas", "chickpea", "lemon") to 1f,
        ),
        // The Harbour: a week that fell short.
        listOf(
            listOf("oats", "banana") to .25f, listOf("whitebread", "tomato", "cucumber") to .25f,
            listOf("pasta", "onion", "basil", "garlic") to .5f, listOf("apple", "kefir") to 1f,
            listOf("whiterice", "carrot", "peas", "soy") to .5f, listOf("buckwheat", "champignon") to .5f,
            listOf("orange", "beet", "seabuckthorn") to 1f, listOf("lentil", "spinach", "lime") to 1f,
            listOf("potato", "dill", "fennel") to .5f, listOf("blueberry", "pistachio") to 1f,
        ),
        // The Castle: a full festival.
        listOf(
            listOf("oats", "blueberry", "walnut", "chia") to .5f, listOf("tomato", "pepper", "eggplant", "onion", "oregano") to 1f,
            listOf("quinoa", "avocado", "lime", "coriander") to 1f, listOf("apple", "persimmon", "yogurt") to 1f,
            listOf("beet", "cabbage", "carrot", "potato", "dill") to 1f, listOf("brownrice", "broccoli", "gingerroot", "sesame") to 1f,
            listOf("chickpea", "turmeric", "spinach", "cumin") to 1f, listOf("pomegranate", "mandarin", "hazelnut") to 1f,
            listOf("barley", "leek", "celery", "parsley") to .5f, listOf("grape", "fig", "almond") to 1f,
            listOf("lentil", "pumpkin", "kale", "paprika") to 1f, listOf("rye", "kimchi", "mung") to .5f,
            listOf("cranberry", "lingonberry", "honeysuckle", "redcabbage") to 1f,
        ),
    )

    private val now = listOf(
        listOf("oats", "blueberry", "walnut", "flax") to .5f,
        listOf("beet", "cabbage", "carrot", "potato", "onion", "tomato", "dill", "garlic") to 1f,
        listOf("buckwheat", "champignon", "kefir") to .5f,
        listOf("apple", "pear", "own1") to 1f,
        listOf("chickpea", "sesame", "lemon", "cucumber", "beet") to 1f,
        listOf("redcabbage", "pumpkin", "saffron", "beet") to 1f,
    )

    suspend fun seedIfEmpty(c: AppContainer) {
        val r = c.repo
        if (r.refresh().meals.isNotEmpty()) return
        val real = r.clock
        val t = real()
        val w = t.week(r.zone)
        val first = w - 28
        c.settings.update { it.copy(onboarded = true, firstWeek = first, reportedWeek = w, trialPlant = "beet", trialStart = weekStart(w - 14, r.zone), trialsDone = "eggplant") }
        r.own("Ирга", 'B', "BERRY")
        past.forEachIndexed { i, plates ->
            val start = weekStart(first + i * 7L, r.zone)
            plates.forEachIndexed { k, (plants, plate) ->
                val at = start + (k / 2) * DAY + (if (k % 2 == 0) 9 else 19) * HOUR
                r.meal(plants, plate, at)
            }
            r.quiet(if (i == 1) 60f else 25f, start + 3 * DAY + 13 * HOUR)
        }
        val start = weekStart(w, r.zone)
        now.forEachIndexed { k, (plants, plate) ->
            val at = maxOf(start + (k + 1) * 20 * 60_000L, minOf(start + (k / 2) * DAY + (if (k % 2 == 0) 9 else 19) * HOUR, t - (now.size - k) * 40 * 60_000L))
            r.meal(plants, plate, at)
        }
        r.quiet(30f, maxOf(start + 10 * 60_000L, t - 2 * HOUR))
        r.dish("Мамин салат", listOf("tomato", "cucumber", "greenonion", "dill", "sunflower"), 1f)
        r.dish("Сырники с ягодами", listOf("strawberry", "blueberry"), .25f)
        r.clock = real
    }

    private const val HOUR = 3_600_000L
    private const val DAY = 24 * HOUR
}
