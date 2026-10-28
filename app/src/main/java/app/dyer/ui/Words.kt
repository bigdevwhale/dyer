package app.dyer.ui

import app.dyer.domain.Catalogue
import app.dyer.domain.Kind
import app.dyer.domain.Pigment
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

/** The words that come from code rather than resources, in both languages. */
class Words(val ru: Boolean, val cat: Catalogue) {
    fun t(r: String, e: String) = if (ru) r else e

    fun plural(n: Int, one: String, few: String, many: String): String {
        val a = abs(n) % 100
        val b = a % 10
        return when {
            !ru -> if (n == 1) one else many
            a in 11..14 -> many
            b == 1 -> one
            b in 2..4 -> few
            else -> many
        }
    }

    /** 12.75 → «12¾». */
    fun count(n: Float): String {
        val w = floor(n).toInt()
        val f = when (((n - w) * 4).toInt()) { 1 -> "¼"; 2 -> "½"; 3 -> "¾"; else -> "" }
        return if (w == 0 && f.isNotEmpty()) f else "$w$f"
    }

    fun plants(n: Float): String {
        val w = floor(n).toInt()
        val word = if (n != w.toFloat()) t("растения", "plants") else plural(w, t("растение", "plant"), t("растения", "plants"), t("растений", "plants"))
        return "${count(n)} $word"
    }

    fun name(id: String) = cat[id]?.name(ru) ?: id

    fun pigment(p: Pigment) = when (p) {
        Pigment.RED -> t("красный", "red")
        Pigment.ORANGE -> t("оранжевый", "orange")
        Pigment.YELLOW -> t("жёлтый", "yellow")
        Pigment.GREEN -> t("зелёный", "green")
        Pigment.BLUE -> t("синий", "blue")
        Pigment.WHITE -> t("белый", "white")
        Pigment.EARTH -> t("земля", "earth")
    }

    /** «не хватает синего». */
    fun pigmentOf(p: Pigment) = when (p) {
        Pigment.RED -> t("красного", "red")
        Pigment.ORANGE -> t("оранжевого", "orange")
        Pigment.YELLOW -> t("жёлтого", "yellow")
        Pigment.GREEN -> t("зелёного", "green")
        Pigment.BLUE -> t("синего", "blue")
        Pigment.WHITE -> t("белого", "white")
        Pigment.EARTH -> t("земляной краски", "earth paint")
    }

    fun kind(k: Kind) = when (k) {
        Kind.VEG -> t("овощ", "vegetable")
        Kind.GREEN -> t("зелень", "leafy green")
        Kind.FRUIT -> t("фрукт", "fruit")
        Kind.BERRY -> t("ягода", "berry")
        Kind.GRAIN -> t("крупа", "grain")
        Kind.REFINED -> t("побелка, ½", "whitewash, ½")
        Kind.LEGUME -> t("бобовое", "legume")
        Kind.NUT -> t("орех", "nut")
        Kind.SEED -> t("семена", "seeds")
        Kind.MUSHROOM -> t("гриб", "mushroom")
        Kind.HERB -> t("трава, капля ¼", "herb, a drop ¼")
        Kind.SPICE -> t("пряность, капля ¼", "spice, a drop ¼")
        Kind.FERMENT -> t("квашеное, протрава", "fermented, a fixative")
    }

    /** How to get a plant onto a plate with no fuss. */
    fun tip(k: Kind) = when (k) {
        Kind.VEG -> t("натереть в салат или запечь", "grate into a salad or roast it")
        Kind.GREEN -> t("горсть в салат или в суп под конец", "a handful in a salad, or in soup at the end")
        Kind.FRUIT -> t("на перекус или в кашу", "as a snack or in porridge")
        Kind.BERRY -> t("в кашу, в йогурт или просто горстью", "in porridge, in yogurt, or just by the handful")
        Kind.GRAIN -> t("кашей или гарниром", "as porridge or a side")
        Kind.LEGUME -> t("в суп, в салат или пюре вроде хумуса", "in soup, in salad, or mashed like hummus")
        Kind.NUT -> t("горсть на перекус или в кашу", "a handful as a snack or in porridge")
        Kind.SEED -> t("посыпать кашу или салат", "sprinkled over porridge or salad")
        Kind.MUSHROOM -> t("обжарить с луком", "fried with an onion")
        else -> t("щепотку в любое блюдо", "a pinch in anything")
    }

    fun month(m: Int, short: Boolean = false): String =
        java.time.Month.of(m).getDisplayName(if (short) TextStyle.SHORT_STANDALONE else TextStyle.FULL_STANDALONE, locale).let { if (ru) it.lowercase().trimEnd('.') else it }

    val locale: Locale get() = if (ru) Locale.forLanguageTag("ru") else Locale.ENGLISH

    fun date(d: LocalDate): String = if (ru) "${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.FULL, locale)}" else "${d.month.getDisplayName(TextStyle.SHORT, locale)} ${d.dayOfMonth}"

    fun weekday(d: LocalDate): String = d.dayOfWeek.getDisplayName(TextStyle.FULL, locale).let { if (ru) it else it.replaceFirstChar(Char::uppercase) }

    /** «27 октября — 2 ноября». */
    fun range(week: Long): String {
        val a = LocalDate.ofEpochDay(week)
        val b = a.plusDays(6)
        return if (ru) {
            if (a.month == b.month) "${a.dayOfMonth}–${b.dayOfMonth} ${b.month.getDisplayName(TextStyle.FULL, locale)}"
            else "${date(a)} — ${date(b)}"
        } else {
            if (a.month == b.month) "${a.month.getDisplayName(TextStyle.SHORT, locale)} ${a.dayOfMonth}–${b.dayOfMonth}" else "${date(a)} – ${date(b)}"
        }
    }

    /** Season as «июль — сентябрь». */
    fun season(months: Set<Int>): String {
        if (months.isEmpty()) return t("круглый год", "all year")
        val sorted = months.sorted()
        // A season that wraps over the new year starts after the gap.
        val start = sorted.firstOrNull { (it + 10) % 12 + 1 !in months } ?: sorted.first()
        val end = sorted.firstOrNull { it % 12 + 1 !in months } ?: sorted.last()
        return if (start == end) month(start) else "${month(start)} — ${month(end)}"
    }
}
