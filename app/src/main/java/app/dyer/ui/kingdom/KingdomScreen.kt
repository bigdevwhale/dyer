package app.dyer.ui.kingdom

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyer.container
import app.dyer.domain.Quarter
import app.dyer.domain.State
import app.dyer.domain.WeekPaint
import app.dyer.share.ShareWeek
import app.dyer.ui.Home
import app.dyer.ui.Nav
import app.dyer.ui.Overlay
import app.dyer.ui.art.Look
import app.dyer.ui.art.Scene
import app.dyer.ui.art.Scenes
import app.dyer.ui.components.Btn
import app.dyer.ui.components.Card
import app.dyer.ui.components.Caps
import app.dyer.ui.components.Gap
import app.dyer.ui.components.Kind
import app.dyer.ui.components.Sheet
import app.dyer.ui.components.TopBar
import app.dyer.ui.components.paper
import app.dyer.ui.square.Summary
import app.dyer.ui.theme.C
import app.dyer.ui.theme.T
import kotlinx.coroutines.launch

/** A week's painting as it ended up, without animation. */
fun still(s: State, p: WeekPaint, rainbow: Boolean): Look {
    val trial = s.trialPlant
    return Look(
        p.quarter.slots.associate { it.id to p.cover(it) },
        if (trial == null) emptyMap() else p.quarter.slots.associate { it.id to s.mud * p.share(it.id, trial) },
        p.fixed, if (p.festival) 1f else 0f, p.details, rainbow,
    )
}

/** The four quarters as they were last painted, the Gloom, and every week in an album. */
@Composable
fun KingdomScreen(h: Home, nav: Nav) {
    val w = h.w
    val s = h.s
    val fests = s.festivals.values.sum()
    Column(Modifier.fillMaxSize()) {
        TopBar(w.t("Королевство", "Kingdom"), w.t("неделя ${h.number(h.week)} · праздников: $fests", "week ${h.number(h.week)} · festivals: $fests"))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp)) {
            for (pair in Quarter.entries.chunked(2)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (q in pair) QuarterCard(h, nav, q, Modifier.weight(1f))
                }
            }
            Gloom(h, nav)
            if (s.weeks.isNotEmpty()) Caps(w.t("Альбом недель", "Album of weeks"))
            for (week in s.weeks.take(40)) {
                val p = s.week(week)
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(10.dp)).paper(C.card).border(1.dp, C.line, RoundedCornerShape(10.dp))
                        .clickable { nav.push(Overlay.View(week)) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Scene(Scenes.of(p.quarter), { still(s, p, h.settings.finaleSeen) }, Modifier.size(58.dp, 72.dp).clip(RoundedCornerShape(6.dp)))
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(p.quarter.name(h.ru), style = T.BodyB)
                        Text(w.range(week) + (if (week == h.week) w.t(" · идёт сейчас", " · now") else ""), style = T.Small)
                        Text(w.plants(p.total) + (if (p.festival) w.t(" · праздник", " · festival") else ""), style = T.Small.copy(color = if (p.festival) C.wine else C.muted))
                    }
                    Text("›", style = T.Heading.copy(color = C.wine))
                }
            }
            Gap(20)
        }
    }
}

@Composable
private fun QuarterCard(h: Home, nav: Nav, q: Quarter, modifier: Modifier) {
    val w = h.w
    val s = h.s
    val last = s.weeks.firstOrNull { s.quarter(it) == q }
    Column(modifier.clip(RoundedCornerShape(10.dp)).paper(C.card).border(1.dp, C.line, RoundedCornerShape(10.dp)).clickable(enabled = last != null) { last?.let { nav.push(Overlay.View(it)) } }) {
        val p = last?.let { s.week(it) }
        Scene(Scenes.of(q), { if (p != null) still(s, p, h.settings.finaleSeen) else Look(emptyMap()) }, Modifier.fillMaxWidth().aspectRatio(.85f))
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(q.name(h.ru), style = T.BodyB.copy(fontSize = 15.sp))
            val n = s.festivals[q] ?: 0
            Text(
                if (last == null) w.t("ещё под Хмарью", "still under the Gloom") else w.t("праздников: $n", "festivals: $n"),
                style = T.Tiny,
            )
        }
    }
}

@Composable
private fun Gloom(h: Home, nav: Nav) {
    val w = h.w
    val s = h.s
    val done = Quarter.entries.count { (s.festivals[it] ?: 0) > 0 }
    Card {
        Text(w.t("Хмарь", "The Gloom"), style = T.Heading)
        when {
            h.settings.finaleSeen -> Text(w.t("Хмарь стала радугой. Её видно в каждом раскрашенном небе.", "The Gloom became a rainbow. You can see it in every painted sky."), style = T.Small)
            s.finaleReady -> {
                Text(w.t("Все четыре квартала отпраздновали. Пора раскрасить саму Хмарь.", "All four quarters have had their festival. Time to paint the Gloom itself."), style = T.Small)
                Btn(w.t("Раскрасить Хмарь", "Paint the Gloom"), Modifier.fillMaxWidth().padding(top = 10.dp), kind = Kind.WINE) { nav.push(Overlay.Finale) }
            }
            else -> Text(
                w.t("Праздник был в $done из 4 кварталов. Когда отпразднуют все четыре, можно будет раскрасить саму Хмарь.",
                    "$done of 4 quarters have had a festival. When all four have, you can paint the Gloom itself."),
                style = T.Small,
            )
        }
    }
}

/** A past week up close. */
@Composable
fun WeekView(h: Home, nav: Nav, week: Long, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val w = h.w
    val p = h.s.week(week)
    Sheet(p.quarter.name(h.ru) + " · " + w.range(week), onClose, w.t("Закрыть", "Close"), foot = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn(w.t("Тарелки", "Plates"), Modifier.weight(1f), kind = Kind.GHOST) { nav.push(Overlay.Week(week)) }
            Btn(w.t("Поделиться", "Share"), Modifier.weight(1f)) { ShareWeek.share(ctx, h, p) }
        }
    }) {
        Scene(Scenes.of(p.quarter), { still(h.s, p, h.settings.finaleSeen) }, Modifier.fillMaxWidth().aspectRatio(.8f).clip(RoundedCornerShape(10.dp)))
        Summary(h, p)
        Gap(16)
    }
}

/** The Gloom, painted at last. */
@Composable
fun Finale(h: Home, onClose: () -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    val k = remember { Animatable(0f) }
    var done by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun close() { c.write { settings.update { it.copy(finaleSeen = true) } }; onClose() }
    BackHandler { if (done) close() else onClose() }
    Box(Modifier.fillMaxSize().background(C.night)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(w.t("Хмарь", "The Gloom"), style = T.Title.copy(color = C.nightText))
            Canvas(Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp)) { gloom(k.value) }
            Text(
                if (!done) w.t("Четыре квартала снова в цвете. Осталась только она сама — большая, серая и, кажется, грустная.",
                    "Four quarters are in colour again. Only she is left: big, grey and, it seems, sad.")
                else w.t("Хмарь вовсе не злая. Ей просто нечего было есть, кроме серого. Теперь она радуга, и в каждом раскрашенном небе её видно.",
                    "The Gloom was never wicked. She simply had nothing to eat but grey. Now she is a rainbow, and you’ll see her in every painted sky."),
                style = T.Body.copy(color = C.nightText, textAlign = TextAlign.Center),
            )
            Btn(
                if (!done) w.t("Раскрасить Хмарь", "Paint the Gloom") else w.t("Красота", "Lovely"), Modifier.fillMaxWidth().padding(top = 20.dp), kind = Kind.PAPER,
            ) {
                if (done) close() else scope.launch { k.animateTo(1f, tween(4200)); done = true }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.gloom(t: Float) {
    val cx = size.width / 2
    val cy = size.height * .45f
    val r = size.minDimension * .2f
    val cloud = app.dyer.ui.art.union(listOf(
        Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx - 2.1f * r, cy - .6f * r), Size(2.2f * r, 1.5f * r))) },
        Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx - 1.1f * r, cy - 1.3f * r), Size(2.2f * r, 2.2f * r))) },
        Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx - .1f * r, cy - .7f * r), Size(2.2f * r, 1.6f * r))) },
        Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(cx - 1.9f * r, cy - .1f * r, cx + 1.9f * r, cy + .9f * r, androidx.compose.ui.geometry.CornerRadius(.45f * r))) },
    ))
    drawPath(cloud, Color(0xFF8E8A84))
    val bands = listOf(0xFFC4472F, 0xFFEF8424, 0xFFE7AD1C, 0xFF5F8A37, 0xFF4F7FC0, 0xFF6B3FA0)
    clipPath(cloud) {
        val reach = t * 3.4f * r
        val h = 2.4f * r / bands.size
        bands.forEachIndexed { i, col ->
            drawRect(Color(col), Offset(cx - 2.2f * r, cy - 1.4f * r + i * h), Size(reach * (1f + (i % 2) * .06f), h + 1))
        }
    }
    drawPath(cloud, Color(0xFF2B241D), style = Stroke(2.5f))
    // Closed eyes, and a mouth that turns up as she fills with colour.
    val eye = Stroke(3f)
    drawArc(Color(0xFF2B241D), 0f, 180f, false, Offset(cx - .55f * r, cy - .1f * r), Size(.35f * r, .22f * r), style = eye)
    drawArc(Color(0xFF2B241D), 0f, 180f, false, Offset(cx + .2f * r, cy - .1f * r), Size(.35f * r, .22f * r), style = eye)
    val smile = -1f + 2f * t
    drawArc(Color(0xFF2B241D), if (smile >= 0) 20f else 200f, 140f, false, Offset(cx - .25f * r, cy + .25f * r - (if (smile < 0) 0f else .1f * r)), Size(.5f * r, .3f * r * kotlin.math.abs(smile).coerceAtLeast(.3f)), style = eye)
    if (t > .95f) for (i in bands.indices) {
        val rr = 3.2f * r - i * .14f * r
        drawArc(Color(bands[i]), 200f, 140f, false, Offset(cx - rr, cy + 2.2f * r - rr), Size(2 * rr, 2 * rr), alpha = (t - .95f) * 20f * .6f, style = Stroke(.12f * r))
    }
}
