package app.dyer.ui.shelf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.dyer.container
import app.dyer.domain.Kind
import app.dyer.domain.Recipes
import app.dyer.domain.TRIES
import app.dyer.domain.date
import app.dyer.ui.Home
import app.dyer.ui.Nav
import app.dyer.ui.Overlay
import app.dyer.ui.art.Jar
import app.dyer.ui.components.Btn
import app.dyer.ui.components.Card
import app.dyer.ui.components.Gap
import app.dyer.ui.components.Kind as BtnKind
import app.dyer.ui.components.Sheet
import app.dyer.ui.theme.C
import app.dyer.ui.theme.T

/** One jar up close: what the plant is, how often it was eaten, a fact, and its eight tries. */
@Composable
fun PlantSheet(h: Home, nav: Nav, id: String, onClose: () -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    val s = h.s
    val p = s.cat[id] ?: run { onClose(); return }
    val jar = s.jars[id]
    Sheet(p.name(h.ru), onClose, w.t("Закрыть", "Close")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Jar(p, jar?.times ?: 0, Modifier.size(72.dp, 86.dp))
            Column(Modifier.padding(start = 14.dp)) {
                if (p.latin.isNotBlank()) Text(p.latin, style = T.Italic.copy(color = C.ink2))
                s.cat.family(p.family)?.let { Text(it.name(h.ru), style = T.Small) }
                Text(w.kind(p.kind) + (p.pigment?.let { " · " + w.pigment(it) } ?: ""), style = T.Small)
                if (!p.ferment && !p.own) Text(w.season(p.season), style = T.Small)
            }
        }
        Gap(12)
        Text(
            if (jar == null) w.t("Ещё не пробовал(а).", "Not tried yet.")
            else w.t("На тарелке: ${jar.times} ${w.plural(jar.times, "раз", "раза", "раз")}, впервые — ${w.date(jar.first.date(h.zone))}.", "On the plate ${jar.times} ${if (jar.times == 1) "time" else "times"}, first on ${w.date(jar.first.date(h.zone))}."),
            style = T.Body,
        )
        if (p.ferment && p.asPlant != null) Text(w.t("Засчитывается как ", "Counts as ") + w.name(p.asPlant).lowercase() + w.t(" и закрепляет краски недели.", " and fixes the week’s colours."), style = T.Small, modifier = Modifier.padding(top = 4.dp))
        if (p.fact(h.ru).isNotBlank()) Text(p.fact(h.ru), style = T.Italic, modifier = Modifier.padding(top = 10.dp))
        Recipes.of(p.id)?.let { r ->
            Gap(12)
            Card(onClick = { nav.push(Overlay.Recipes) }) {
                Text(w.t("В книге красильщика: ", "In the dyer’s book: ") + r.title(h.ru), style = T.BodyB)
                Text(if (jar != null) w.t("Открыто ›", "Open ›") else w.t("Откроется, когда банка появится на полке.", "Opens once the jar is on the shelf."), style = T.Small.copy(color = if (jar != null) C.wine else C.muted))
            }
        }
        if (!p.ferment && !p.kind.drop && p.kind != Kind.REFINED) {
            Gap(12)
            Card {
                Text(w.t("Восемь проб", "Eight tries"), style = T.Heading)
                when {
                    p.id in s.trialsDone -> Text(w.t("Восемь проб позади: у этого растения свой, чистый цвет.", "Eight tries behind you: this plant has its own clear colour."), style = T.Small)
                    s.trialPlant == p.id -> {
                        Text(w.t("В работе: проба ${s.tries} из $TRIES. Каждая тарелка с ним очищает краску.", "In progress: try ${s.tries} of $TRIES. Every plate with it clears the paint."), style = T.Small)
                        Btn(w.t("Бросить пробы", "Stop the tries"), Modifier.fillMaxWidth().padding(top = 10.dp), kind = BtnKind.GHOST) { c.write { repo.trial(null) } }
                    }
                    else -> {
                        Text(
                            w.t("Не любишь его? К новому вкусу обычно привыкают за 8–15 проб, и хватает кусочка. Краска начнёт мутной и с каждой тарелкой будет чище.",
                                "Not fond of it? A new taste usually takes 8 to 15 tries, and a bite is enough. Its paint starts muddy and clears with every plate."),
                            style = T.Small,
                        )
                        if (s.trialPlant != null) Text(w.t("Сейчас в работе: ", "Now in progress: ") + w.name(s.trialPlant).lowercase() + w.t(". Новое растение его заменит.", ". A new one replaces it."), style = T.Tiny, modifier = Modifier.padding(top = 4.dp))
                        Btn(w.t("Взять в работу", "Start the tries"), Modifier.fillMaxWidth().padding(top = 10.dp), kind = BtnKind.WINE) { c.write { repo.trial(p.id) } }
                    }
                }
            }
        }
        Gap(20)
    }
}

/** The dyer's book: real experiments, each opening once its plant is on the shelf. */
@Composable
fun RecipesSheet(h: Home, nav: Nav, onClose: () -> Unit) {
    val w = h.w
    Sheet(w.t("Книга красильщика", "The dyer’s book"), onClose, w.t("Закрыть", "Close")) {
        Text(w.t("Тысячи лет ткани красили растениями. Вот что из этого можно повторить на кухне.", "For thousands of years cloth was dyed with plants. Here is what you can repeat in a kitchen."), style = T.Italic)
        for (r in Recipes.all) {
            val open = r.plant in h.s.jars
            Gap(10)
            Card {
                Text(r.title(h.ru), style = T.Heading.copy(color = if (open) C.ink else C.muted))
                if (open) Text(r.steps(h.ru), style = T.Small.copy(color = C.ink), modifier = Modifier.padding(top = 4.dp))
                else Text(w.t("Откроется, когда на полке появится банка: ", "Opens when this jar is on the shelf: ") + w.name(r.plant).lowercase() + ".", style = T.Small.copy(color = C.muted))
            }
        }
        Gap(20)
    }
}
