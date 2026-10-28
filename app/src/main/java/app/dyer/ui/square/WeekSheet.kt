package app.dyer.ui.square

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyer.container
import app.dyer.domain.Pigment
import app.dyer.domain.Quarter
import app.dyer.domain.WeekPaint
import app.dyer.domain.date
import app.dyer.share.ShareWeek
import app.dyer.ui.Home
import app.dyer.ui.Nav
import app.dyer.ui.Overlay
import app.dyer.ui.art.Scene
import app.dyer.ui.art.Scenes
import app.dyer.ui.components.Btn
import app.dyer.ui.components.Caps
import app.dyer.ui.components.Dot
import app.dyer.ui.components.Kind
import app.dyer.ui.components.PlateIcon
import app.dyer.ui.components.Sheet
import app.dyer.ui.components.color
import app.dyer.ui.components.paper
import app.dyer.ui.theme.C
import app.dyer.ui.theme.T
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/** The week's plates, day by day; a wrong one can be taken back. */
@Composable
fun WeekSheet(h: Home, nav: Nav, week: Long, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    val w = h.w
    val p = h.s.week(week)
    Sheet(p.quarter.name(h.ru) + " · " + w.range(week), onClose, w.t("Закрыть", "Close"), foot = {
        Btn(w.t("Поделиться картинкой недели", "Share the week’s picture"), Modifier.fillMaxWidth(), kind = Kind.GHOST) { ShareWeek.share(ctx, h, p) }
    }) {
        Summary(h, p)
        val hm = DateTimeFormatter.ofPattern("HH:mm")
        val byDay = p.meals.sortedBy { it.at }.groupBy { it.at.date(h.zone) }
        if (byDay.isEmpty()) Text(w.t("На этой неделе тарелок ещё не было.", "No plates this week yet."), style = T.Italic, modifier = Modifier.padding(top = 16.dp))
        for ((d, meals) in byDay.entries.sortedByDescending { it.key }) {
            Caps(w.weekday(d) + ", " + w.date(d))
            for (m in meals) {
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(10.dp)).paper(C.card).border(1.dp, C.line, RoundedCornerShape(10.dp)).padding(10.dp), verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PlateIcon(m.plate)
                        Text(java.time.Instant.ofEpochMilli(m.at).atZone(h.zone).format(hm), style = T.Tiny)
                    }
                    Text(m.ids.joinToString(", ") { w.name(it) }, style = T.Small.copy(color = C.ink), modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
                    Text(
                        "×", style = T.Heading.copy(color = C.muted),
                        modifier = Modifier.clickable { c.write { repo.deleteMeal(m.id) } }.padding(horizontal = 6.dp),
                    )
                }
            }
        }
        if (byDay.isNotEmpty()) Text(
            w.t("Ошибся тарелкой — нажми ×, и краска сойдёт.", "Wrong plate? Tap × and the paint comes off."),
            style = T.Tiny, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
    }
}

/** The week in numbers: plants, colours, families, what was new. */
@Composable
fun Summary(h: Home, p: WeekPaint) {
    val w = h.w
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
        Text(w.count(p.total), style = T.Heading.copy(fontSize = 34.sp, lineHeight = 36.sp))
        Text(
            "  " + (if (p.festival) w.t("растений — был праздник", "plants: a festival") else w.t("из ${p.goal} растений", "of ${p.goal} plants")),
            style = T.Small, modifier = Modifier.padding(bottom = 5.dp),
        )
    }
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        for (pig in Pigment.entries) {
            val on = pig in p.pigments
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Dot(pig.color(), 18.dp, empty = !on)
                Text(w.pigment(pig), style = T.Tiny.copy(fontSize = 10.sp))
            }
        }
    }
    val fresh = h.s.newIn(p)
    Text(
        w.t("Семейств растений: ", "Plant families: ") + p.families.size + (if (p.fixed) w.t(" · была протрава", " · fixed") else "") +
            (if (p.details >= 1f) w.t(" · тихих деталей: ", " · quiet details: ") + p.details.toInt() else ""),
        style = T.Small, modifier = Modifier.padding(top = 10.dp),
    )
    if (fresh.isNotEmpty()) Text(
        w.t("Впервые: ", "First time: ") + fresh.joinToString(", ") { w.name(it).lowercase() },
        style = T.Small.copy(color = C.wine), modifier = Modifier.padding(top = 4.dp),
    )
    val missing = Pigment.entries.filter { it !in p.pigments }
    if (missing.isNotEmpty() && !p.empty) Text(
        w.t("Не хватило: ", "Missing: ") + missing.joinToString(", ") { w.pigmentOf(it) },
        style = T.Small.copy(color = C.muted), modifier = Modifier.padding(top = 4.dp),
    )
}

/** On the first visit of a new week: how the last one went, and where the Gloom has gone now. */
@Composable
fun Report(h: Home, week: Long, onClose: () -> Unit) {
    val w = h.w
    val p = h.s.week(week)
    val next = h.paint.quarter
    androidx.activity.compose.BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(C.scrim)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(12.dp).clip(RoundedCornerShape(16.dp)).paper()
                .verticalScroll(rememberScrollState()).padding(18.dp),
        ) {
            Text(w.t("Неделя позади", "A week behind you"), style = T.Title.copy(fontSize = 26.sp))
            Text(p.quarter.name(h.ru) + " · " + w.range(week), style = T.Small)
            val look = rememberLook(h.s, p, h.settings.finaleSeen, still = true)
            Scene(Scenes.of(p.quarter), look, Modifier.padding(vertical = 12.dp).fillMaxWidth().aspectRatio(.82f).clip(RoundedCornerShape(10.dp)))
            Summary(h, p)
            Text(
                if (p.festival) w.t("Квартал отпраздновал, и краски на нём остаются. ", "The quarter had its festival, and its colours stay. ")
                else w.t("До праздника не хватило совсем немного — краски всё равно остаются. ", "The festival was close; the colours stay all the same. "),
                style = T.Italic, modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                w.t("Теперь Хмарь опустилась на квартал «${next.ru}». Новая неделя — новая кисть.", "Now the Gloom has settled on ${next.en}. A new week, a new brush."),
                style = T.Italic,
            )
            Btn(w.t("К новому кварталу", "On to the new quarter"), Modifier.fillMaxWidth().padding(top = 16.dp), onClick = onClose)
        }
    }
}

/** While a quiet meal goes on: the phone lies face down, and the brush draws. */
@Composable
fun QuietVeil(h: Home, nav: Nav) {
    val c = LocalContext.current.container
    val w = h.w
    val since = h.settings.quietSince
    var now by androidx.compose.runtime.remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis()) }
    androidx.compose.runtime.LaunchedEffect(since) { while (true) { now = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }
    val minutes = ((now - since) / 60_000f).coerceIn(0f, 60f)
    fun finish(keep: Boolean) {
        val before = h.s
        c.scope.launch {
            c.settings.update { it.copy(quietSince = 0) }
            if (!keep || minutes < .5f) return@launch
            c.repo.quiet(minOf(minutes, 40f))
            val after = c.repo.state.value ?: return@launch
            val said = quietSpeeches(w, before, after, h.week)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { nav.bubble.say(said) }
        }
    }
    androidx.activity.compose.BackHandler { finish(true) }
    Box(Modifier.fillMaxSize().background(C.night).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(w.t("Тихая трапеза", "A quiet meal"), style = T.Title.copy(color = C.nightText))
            Text(
                w.t(
                    "Положи телефон экраном вниз и ешь не торопясь. Сытость приходит минут через двадцать, а пока ты ешь, кисть дорисует на площади маленькую деталь.",
                    "Put the phone face down and eat slowly. Fullness arrives after about twenty minutes, and while you eat the brush draws a small detail into the quarter.",
                ),
                style = T.Body.copy(color = C.nightText, textAlign = TextAlign.Center), modifier = Modifier.padding(top = 14.dp),
            )
            val m = minutes.toInt()
            val sec = ((now - since) / 1000 % 60).toInt()
            Text("%d:%02d".format(m, sec), style = T.Title.copy(color = C.gold, fontSize = 44.sp, lineHeight = 50.sp), modifier = Modifier.padding(top = 26.dp))
            Box(Modifier.padding(top = 10.dp).fillMaxWidth(.7f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color4)) {
                Box(Modifier.fillMaxWidth((minutes / 20f).coerceIn(0f, 1f)).height(5.dp).background(C.gold))
            }
            Text(w.t("двадцать минут — одна деталь", "twenty minutes, one detail"), style = T.Small.copy(color = C.nightMuted), modifier = Modifier.padding(top = 6.dp))
            Btn(w.t("Я поел(а)", "I’ve finished"), Modifier.fillMaxWidth().padding(top = 30.dp), kind = Kind.PAPER) { finish(true) }
            Text(w.t("отменить", "cancel"), style = T.Small.copy(color = C.nightMuted), modifier = Modifier.padding(top = 14.dp).clickable { finish(false) }.padding(8.dp))
        }
    }
}

private val Color4 = androidx.compose.ui.graphics.Color(0xFF4A4136)

private fun quietSpeeches(w: app.dyer.ui.Words, before: app.dyer.domain.State, after: app.dyer.domain.State, week: Long): List<app.dyer.ui.Speech> {
    val a = before.week(week).details
    val p = after.week(week)
    val b = p.details
    val q: Quarter = p.quarter
    val out = mutableListOf<app.dyer.ui.Speech>()
    for (i in q.details.indices) {
        if (a < i + 1 && b >= i + 1) out += app.dyer.ui.Speech(q.details[i].name(w.ru), q.details[i].done(w.ru), w.t("Тихая трапеза: телефон лежал, кисть рисовала.", "A quiet meal: the phone lay still, the brush drew."), C.gold)
    }
    if (out.isEmpty()) {
        val i = b.toInt()
        val frac = ((b - i) * 100).toInt()
        out += if (i >= q.details.size) app.dyer.ui.Speech(w.t("Мастер-красильщик", "The master dyer"), w.t("Все детали этой недели уже нарисованы. Тихо поесть всё равно хорошо.", "All of this week’s details are drawn. Eating quietly is still good."))
        else app.dyer.ui.Speech(
            w.t("Мастер-красильщик", "The master dyer"),
            w.t("«${q.details[i].ru}» дорисована на $frac%. В следующую тихую трапезу кисть продолжит.", "“${q.details[i].en}” is $frac% drawn. The brush carries on at the next quiet meal."),
        )
    }
    return out
}

