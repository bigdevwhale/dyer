package app.dyer.domain

import java.io.InputStream

/** The seven paints of the dyehouse. Grains, legumes and nuts give the earth paint that holds everything up. */
enum class Pigment(val code: Char, val argb: Long) {
    RED('R', 0xFFC4472F),
    ORANGE('O', 0xFFEF8424),
    YELLOW('Y', 0xFFE7AD1C),
    GREEN('G', 0xFF5F8A37),
    BLUE('B', 0xFF4A4592),
    WHITE('W', 0xFFF1E3BE),
    EARTH('E', 0xFF9C7A52);

    companion object {
        fun of(c: Char): Pigment? = entries.firstOrNull { it.code == c }
    }
}

/** What sort of thing a plant is on the plate; it decides how much paint it gives. */
enum class Kind(val value: Float) {
    VEG(1f), GREEN(1f), FRUIT(1f), BERRY(1f), GRAIN(1f), LEGUME(1f), NUT(1f), SEED(1f), MUSHROOM(1f),
    /** White rice, white bread, pasta: whitewash, half a paint. */
    REFINED(.5f),
    /** Herbs and spices go in drops: four drops make one paint. */
    HERB(.25f), SPICE(.25f),
    /** Kefir, sauerkraut and the like fix the week's colours; a fermented plant also counts as that plant. */
    FERMENT(0f);

    val drop get() = this == HERB || this == SPICE

    companion object {
        fun of(s: String): Kind? = entries.firstOrNull { it.name.equals(s, ignoreCase = true) }
    }
}

data class Family(val id: String, val ru: String, val en: String) {
    fun name(ru: Boolean) = if (ru) this.ru else en
}

data class Plant(
    val id: String,
    val ru: String,
    val en: String,
    val latin: String,
    val family: String,
    val pigment: Pigment?,
    val kind: Kind,
    /** Months it is in season, 1..12; empty for all year. */
    val season: Set<Int>,
    /** A ferment counts as this plant (sauerkraut as cabbage); null when it isn't a plant at all. */
    val asPlant: String?,
    val factRu: String,
    val factEn: String,
    val own: Boolean = false,
) {
    fun name(ru: Boolean) = if (ru) this.ru else en
    fun fact(ru: Boolean) = if (ru) factRu else factEn
    val ferment get() = kind == Kind.FERMENT
    fun inSeason(month: Int) = season.isEmpty() || month in season
    val seasonal get() = season.isNotEmpty()
}

/** Every plant the dyehouse knows, from assets/plants.tsv, plus the ones people add themselves. */
class Catalogue(val plants: List<Plant>, val families: Map<String, Family>) {
    private val byId = plants.associateBy { it.id }
    operator fun get(id: String): Plant? = byId[id]
    fun family(id: String) = families[id]

    /** The same catalogue with someone's own plants added. */
    fun with(own: List<Plant>): Catalogue = if (own.isEmpty()) this else Catalogue(plants + own, families)

    /** What a plate entry counts as: itself, or the plant a ferment is made of. */
    fun counted(p: Plant): Plant? = if (p.ferment) p.asPlant?.let { byId[it] } else p

    companion object {
        fun load(open: (String) -> InputStream?): Catalogue {
            val fam = rows(open("families.tsv")).associate { it[0] to Family(it[0], it[1], it[2]) }
            val plants = rows(open("plants.tsv")).map { r ->
                Plant(
                    id = r[0], ru = r[1], en = r[2], latin = r[3], family = r[4],
                    pigment = r[5].firstOrNull()?.let(Pigment::of),
                    kind = Kind.of(r[6]) ?: error("kind ${r[6]} of ${r[0]}"),
                    season = months(r[7]), asPlant = r[8].ifBlank { null },
                    factRu = r[9], factEn = r[10],
                )
            }
            return Catalogue(plants, fam)
        }

        private fun rows(s: InputStream?): List<List<String>> =
            s?.bufferedReader(Charsets.UTF_8)?.use { r -> r.readLines().drop(1).filter { it.isNotBlank() }.map { it.split('\t') } } ?: emptyList()

        /** «7-9» → July to September; «12-2» wraps over the new year. */
        fun months(s: String): Set<Int> {
            val p = s.trim().split('-').mapNotNull { it.trim().toIntOrNull() }
            if (p.isEmpty()) return emptySet()
            val (a, b) = if (p.size == 1) p[0] to p[0] else p[0] to p[1]
            return if (a <= b) (a..b).toSet() else ((a..12) + (1..b)).toSet()
        }
    }
}
