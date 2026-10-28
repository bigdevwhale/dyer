package app.dyer.ui.square

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyer.container
import app.dyer.domain.Brush
import app.dyer.domain.Kind
import app.dyer.domain.Pigment
import app.dyer.domain.Plant
import app.dyer.domain.Presets
import app.dyer.domain.TRIES
import app.dyer.ui.Home
import app.dyer.ui.Nav
import app.dyer.ui.components.Btn
import app.dyer.ui.components.Caps
import app.dyer.ui.components.Chip
import app.dyer.ui.components.Dot
import app.dyer.ui.components.Field
import app.dyer.ui.components.Flow
import app.dyer.ui.components.Kind as BtnKind
import app.dyer.ui.components.LocalToast
import app.dyer.ui.components.PlateIcon
import app.dyer.ui.components.Sheet
import app.dyer.ui.components.color
import app.dyer.ui.theme.C
import app.dyer.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val GROUPS = listOf(
    setOf(Kind.VEG) to ("Овощи" to "Vegetables"),
    setOf(Kind.GREEN) to ("Зелень и салаты" to "Leafy greens"),
    setOf(Kind.FRUIT) to ("Фрукты" to "Fruit"),
    setOf(Kind.BERRY) to ("Ягоды" to "Berries"),
    setOf(Kind.GRAIN, Kind.REFINED) to ("Крупы и хлеб" to "Grains and bread"),
    setOf(Kind.LEGUME) to ("Бобовые" to "Legumes"),
    setOf(Kind.NUT, Kind.SEED) to ("Орехи и семена" to "Nuts and seeds"),
    setOf(Kind.MUSHROOM) to ("Грибы" to "Mushrooms"),
    setOf(Kind.HERB, Kind.SPICE) to ("Травы и пряности · капля ¼" to "Herbs and spices · a drop ¼"),
    setOf(Kind.FERMENT) to ("Квашеное · протрава" to "Fermented · a fixative"),
)

/** One plate: pick everything that was on it, say once how much of it was plants, and paint. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MealSheet(h: Home, nav: Nav, onClose: () -> Unit) {
    val c = LocalContext.current.container
    val toast = LocalToast.current
    val w = h.w
    val s = h.s
    val picked = remember { mutableStateListOf<String>() }
    var plate by remember { mutableFloatStateOf(h.settings.plate) }
    var day by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val week = h.paint

    fun toggle(id: String) { if (id in picked) picked.remove(id) else picked.add(id) }

    fun paint() {
        val ids = picked.toList()
        val share = plate
        val at = h.now - day * 86_400_000L
        val before = s
        onClose()
        c.scope.launch {
            val id = c.repo.meal(ids, share, at)
            val after = c.repo.state.value ?: return@launch
            val said = speeches(w, before, after, id)
            withContext(Dispatchers.Main) { nav.bubble.say(said) }
        }
    }

    Sheet(
        w.t("Что было на тарелке?", "What was on the plate?"), onClose, w.t("Закрыть", "Close"), scroll = false,
        foot = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(w.t("Сколько тарелки занимали растения?", "How much of the plate was plants?"), style = T.Small, modifier = Modifier.weight(1f))
                Text(brushWord(w.ru, plate), style = T.Tiny)
            }
            Row(Modifier.padding(top = 6.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for ((f, label) in listOf(.25f to w.t("четверть", "a quarter"), .5f to w.t("половина", "half"), 1f to w.t("почти всё", "nearly all"))) {
                    val on = plate == f
                    Row(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (on) C.white else C.card).border(if (on) 2.dp else 1.dp, if (on) C.ink else C.line, RoundedCornerShape(10.dp))
                            .clickable { plate = f }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlateIcon(f)
                        Text(label, style = T.Small.copy(color = C.ink, fontSize = 13.sp), modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
            if (saving) {
                var name by remember { mutableStateOf("") }
                fun save() {
                    if (name.isBlank()) return
                    val ids = picked.toList()
                    val share = plate
                    c.write { repo.dish(name, ids, share) }
                    toast.show(w.t("Блюдо «${name.trim()}» сохранено", "Saved “${name.trim()}”"))
                    saving = false
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Field(name, { name = it }, w.t("Как назвать блюдо?", "Name the dish"), Modifier.weight(1f), max = 40) { save() }
                    Text(w.t("Сохранить", "Save"), style = T.BodyB.copy(color = C.wine), modifier = Modifier.clickable { save() }.padding(start = 12.dp, top = 8.dp, bottom = 8.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val n = picked.size
                Btn(
                    if (n == 0) w.t("Выбери, что было на тарелке", "Pick what was on the plate") else w.t("Покрасить · ", "Paint · ") + "$n " + w.plural(n, w.t("растение", "plant"), w.t("растения", "plants"), w.t("растений", "plants")),
                    Modifier.weight(1f), enabled = n > 0,
                ) { paint() }
                if (n >= 2 && !saving) Text(
                    w.t("В блюда", "Save dish"), style = T.Small.copy(color = C.wine), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.width(76.dp).clickable { saving = true }.padding(start = 8.dp),
                )
            }
        },
    ) {
        Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip(w.t("Сегодня", "Today"), day == 0) { day = 0 }
            Chip(w.t("Вчера", "Yesterday"), day == 1) { day = 1 }
        }
        Field(query, { query = it; adding = false }, w.t("Найти растение", "Find a plant"), max = 40)
        val dishes = s.dishes
        LazyRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(dishes, key = { "d${it.id}" }) { d ->
                DishChip(d.name, Modifier.combinedClickable(onLongClick = {
                    c.write { repo.deleteDish(d.id) }
                    toast.show(w.t("Блюдо «${d.name}» убрано", "Removed “${d.name}”"))
                }) { d.ids.forEach { if (it !in picked && s.cat[it] != null) picked.add(it) }; plate = d.plate })
            }
            items(Presets.all, key = { "p${it.en}" }) { pr ->
                DishChip(pr.name(h.ru), Modifier.clickable { pr.plants.forEach { if (it !in picked) picked.add(it) }; plate = pr.plate })
            }
        }
        if (picked.isNotEmpty()) Text(
            picked.joinToString(", ") { w.name(it) }, style = T.Small.copy(color = C.ink), maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        val q = query.trim().lowercase()
        val all = s.cat.plants
        val month = h.today.monthValue
        LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(bottom = 12.dp)) {
            if (q.isNotEmpty()) {
                val found = all.filter { it.ru.lowercase().contains(q) || it.en.lowercase().contains(q) || it.latin.lowercase().contains(q) }
                item { Caps(if (found.isEmpty()) w.t("Ничего не нашлось", "Nothing found") else w.t("Нашлось", "Found")) }
                items(found.chunked(2), key = { r -> "f" + r.joinToString { it.id } }) { row -> PlantRow(h, row, picked) { toggle(it) } }
                item {
                    if (!adding) Text(
                        w.t("+ Добавить своё растение «${query.trim()}»", "+ Add your own plant “${query.trim()}”"), style = T.BodyB.copy(color = C.wine),
                        modifier = Modifier.padding(vertical = 12.dp).clickable { adding = true },
                    ) else OwnForm(h, query.trim()) { id -> picked.add(id); query = ""; adding = false }
                }
            } else {
                val recent = s.meals.asReversed().flatMap { it.ids }.distinct().mapNotNull { s.cat[it] }.take(8)
                if (recent.isNotEmpty()) {
                    item { Caps(w.t("Недавно", "Recently")) }
                    items(recent.chunked(2), key = { r -> "r" + r.joinToString { it.id } }) { row -> PlantRow(h, row, picked) { toggle(it) } }
                }
                for ((kinds, title) in GROUPS) {
                    val list = all.filter { it.kind in kinds }.sortedWith(compareBy({ !it.inSeason(month) || !it.seasonal }, { it.name(h.ru) }))
                    if (list.isEmpty()) continue
                    item(key = "h${title.second}") { Caps(w.t(title.first, title.second)) }
                    items(list.chunked(2), key = { r -> "g" + r.joinToString { it.id } }) { row -> PlantRow(h, row, picked) { toggle(it) } }
                }
            }
        }
    }
}

private fun brushWord(ru: Boolean, plate: Float) = when (Brush.strength(plate)) {
    .5f -> if (ru) "тонкая кисть" else "a thin brush"
    .8f -> if (ru) "средняя кисть" else "a middling brush"
    else -> if (ru) "широкая кисть" else "a wide brush"
}

@Composable
private fun DishChip(name: String, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(C.card).border(1.dp, C.lineDark, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(name, style = T.Small.copy(color = C.ink))
    }
}

@Composable
private fun PlantRow(h: Home, row: List<Plant>, picked: List<String>, onTap: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (p in row) PlantChip(h, p, p.id in picked, Modifier.weight(1f)) { onTap(p.id) }
        if (row.size == 1) Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun PlantChip(h: Home, p: Plant, on: Boolean, modifier: Modifier, onTap: () -> Unit) {
    val w = h.w
    val s = h.s
    val sub = when {
        p.id == s.trialPlant -> w.t("нелюбимое · проба ${s.tries} из $TRIES", "unloved · try ${s.tries} of $TRIES")
        s.cat.counted(p)?.id in h.paint.plants -> w.t("✓ уже на этой неделе", "✓ already this week")
        p.ferment -> p.asPlant?.let { w.t("протрава · как ", "fixative · counts as ") + w.name(it).lowercase() } ?: w.t("протрава", "fixative")
        p.kind.drop -> w.t("капля ¼", "a drop ¼")
        p.kind == Kind.REFINED -> w.t("побелка ½", "whitewash ½")
        p.id !in s.jars -> w.t("ещё не пробовал(а)", "not tried yet")
        else -> p.pigment?.let { w.pigment(it) } ?: ""
    }
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier.heightIn(min = 50.dp).clip(shape).background(if (on) C.white else C.card)
            .border(if (on) 2.dp else 1.dp, if (on) C.ink else if (p.id == s.trialPlant) C.wine else C.line, shape)
            .clickable(onClick = onTap).padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Dot(p.pigment.color(), 16.dp)
        Column(Modifier.padding(start = 8.dp).weight(1f)) {
            Text(p.name(h.ru), style = T.Small.copy(color = C.ink, fontSize = 14.sp, lineHeight = 17.sp), maxLines = 2)
            if (sub.isNotEmpty()) Text(sub, style = T.Tiny.copy(fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (on) Text("✓", style = T.BodyB.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(start = 4.dp))
    }
}

/** A plant the catalogue lacks: a name, a colour and what sort of thing it is. */
@Composable
private fun OwnForm(h: Home, name0: String, onDone: (String) -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    var name by remember { mutableStateOf(name0) }
    var pig by remember { mutableStateOf<Pigment?>(null) }
    var kind by remember { mutableStateOf(Kind.VEG) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Column(Modifier.padding(vertical = 10.dp)) {
        Field(name, { name = it }, w.t("Название", "Name"), max = 40)
        Text(w.t("Какого оно цвета?", "What colour is it?"), style = T.Small, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
        Flow {
            for (p in Pigment.entries) Row(
                Modifier.clip(RoundedCornerShape(16.dp)).background(if (pig == p) C.white else C.card).border(if (pig == p) 2.dp else 1.dp, if (pig == p) C.ink else C.line, RoundedCornerShape(16.dp))
                    .clickable { pig = p }.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) { Dot(p.color(), 14.dp); Text(w.pigment(p), style = T.Small.copy(color = C.ink), modifier = Modifier.padding(start = 6.dp)) }
        }
        Text(w.t("Что это?", "What is it?"), style = T.Small, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
        Flow {
            for (k in listOf(Kind.VEG, Kind.GREEN, Kind.FRUIT, Kind.BERRY, Kind.GRAIN, Kind.LEGUME, Kind.NUT, Kind.MUSHROOM, Kind.HERB)) {
                Chip(w.kind(k).substringBefore(','), kind == k) { kind = k }
            }
        }
        Btn(w.t("Добавить на полку", "Add to the shelf"), Modifier.fillMaxWidth().padding(top = 12.dp), enabled = name.isNotBlank() && pig != null) {
            val p = pig ?: return@Btn
            scope.launch { onDone(c.repo.own(name, p.code, k(kind))) }
        }
        Spacer(Modifier.size(4.dp))
    }
}

private fun k(kind: Kind) = kind.name
