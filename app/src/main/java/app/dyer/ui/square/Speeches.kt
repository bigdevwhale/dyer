package app.dyer.ui.square

import app.dyer.domain.Kind
import app.dyer.domain.Quarter
import app.dyer.domain.State
import app.dyer.domain.TRIES
import app.dyer.domain.week
import app.dyer.ui.Speech
import app.dyer.ui.Words
import app.dyer.ui.components.color
import app.dyer.ui.theme.C

/** What the eight tries sound like, whoever lives by the thing. */
private val TRIAL_RU = listOf(
    "Цвет пока мутный. Ничего: к новому вкусу привыкают не сразу.",
    "Уже чуть яснее.",
    "Сквозь муть проступает цвет.",
    "Полпути. Ещё четыре раза — и цвет будет чистый.",
    "Почти чисто. Глаз уже радуется.",
    "Ещё два раза.",
    "Остался последний раз.",
    "Свой цвет! Чистый, как у всех. Спасибо за терпение.",
)
private val TRIAL_EN = listOf(
    "The colour is muddy for now. That’s fine: a new taste takes a while.",
    "A little clearer already.",
    "The colour is showing through the mud.",
    "Halfway. Four more times and it’ll be clear.",
    "Nearly clear. It’s a pleasure to look at.",
    "Two more times.",
    "One last time.",
    "Its own colour! Clear as any other. Thank you for your patience.",
)

/** The townsfolk's answer to one plate: who got paint, what came true, a fact for each new plant. */
fun speeches(w: Words, before: State?, after: State, mealId: Long): List<Speech> {
    val meal = after.meals.firstOrNull { it.id == mealId } ?: return emptyList()
    val week = meal.at.week(after.zone)
    val p = after.week(week)
    val prev = before?.week(week)
    val master = w.t("Мастер-красильщик", "The master dyer")
    val out = mutableListOf<Speech>()
    val seen = before?.jars?.keys ?: emptySet()

    if (p.fixed && prev?.fixed != true) {
        val f = meal.ids.mapNotNull { after.cat[it] }.firstOrNull { it.ferment }
        out += Speech(master, w.t("Протрава схватила краски. Эта неделя не выцветет.", "The fixative has caught the colours. This week won’t fade."), f?.fact(w.ru), C.white)
    }
    val trialBefore = before?.trialPlant
    for (wash in p.washes.filter { it.meal == mealId }) {
        val plant = after.cat[wash.plant] ?: continue
        val slot = p.quarter.slot(wash.slot)
        val fact = buildList {
            plant.fact(w.ru).takeIf { it.isNotBlank() }?.let(::add)
            if (plant.id !in seen) add(w.t("Новая банка на полке.", "A new jar on the shelf."))
            if (plant.kind.drop) add(w.t("Капля: четыре капли — одна краска.", "A drop: four drops make one paint."))
            if (plant.kind == Kind.REFINED) add(w.t("Побелка: половина краски.", "Whitewash: half a paint."))
        }.joinToString(" ").ifBlank { null }
        val text = if (plant.id == trialBefore) {
            val n = if (after.trialPlant == plant.id) after.tries else TRIES
            (if (w.ru) TRIAL_RU else TRIAL_EN)[(n - 1).coerceIn(0, TRIES - 1)]
        } else when {
            wash.extra -> w.t("Ещё слой! Цвет стал глубже.", "Another coat! The colour’s deeper now.")
            wash.filled -> slot.full(w.ru)
            wash.first -> slot.first(w.ru)
            else -> w.t("Ещё немного — и будет готово.", "A little more and it’s done.")
        }
        val trialFact = if (plant.id == trialBefore) {
            val n = if (after.trialPlant == plant.id) after.tries else TRIES
            if (n == 1) w.t("Проба 1 из 8. К новому вкусу обычно привыкают за 8–15 проб, и хватает кусочка.", "Try 1 of 8. A new taste usually takes 8 to 15 tries, and a bite is enough.")
            else w.t("Проба $n из 8.", "Try $n of 8.")
        } else null
        out += Speech(slot.who(w.ru), text, listOfNotNull(trialFact, fact).joinToString(" ").ifBlank { null }, plant.pigment.color())
    }
    if (out.isEmpty()) out += Speech(
        master,
        w.t("Всё это уже есть на этой неделе. В следующий раз попробуй что-нибудь новое.", "All of this is on this week’s plates already. Try something new next time."),
        w.t("Повторы в счёт недели не идут, но тарелка всё равно хорошая.", "Repeats don’t count towards the week, but it’s still a good plate."),
    )
    if (p.festival && prev?.festival != true) {
        val next = Quarter.of(week + 7, after.firstWeek)
        out += Speech(
            p.quarter.name(w.ru),
            w.t("Праздник! Ставни настежь, гирлянды, музыка. Квартал ожил.", "A festival! Shutters open, garlands up, music playing. The quarter is alive."),
            w.t("На следующей неделе Хмарь опустится на квартал «${next.ru}».", "Next week the Gloom settles on ${next.en}."),
            C.gold,
        )
    }
    return out
}
