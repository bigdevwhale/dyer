package app.dyer.ui.square

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyer.BuildConfig
import app.dyer.container
import app.dyer.domain.Pigment
import app.dyer.domain.State
import app.dyer.domain.TRIES
import app.dyer.domain.WeekPaint
import app.dyer.ui.Home
import app.dyer.ui.Nav
import app.dyer.ui.Overlay
import app.dyer.ui.Speech
import app.dyer.ui.art.Look
import app.dyer.ui.art.Scene
import app.dyer.ui.art.Scenes
import app.dyer.ui.components.Btn
import app.dyer.ui.components.Dot
import app.dyer.ui.components.Kind
import app.dyer.ui.components.color
import app.dyer.ui.components.paper
import app.dyer.ui.theme.C
import app.dyer.ui.theme.Sign
import app.dyer.ui.theme.T
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/** The week's quarter, painted as far as the week's plates go; the townsfolk talk at the bottom. */
@Composable
fun SquareScreen(h: Home, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val p = h.paint
    val w = h.w
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            val look = rememberLook(h.s, p, h.settings.finaleSeen)
            Scene(Scenes.of(p.quarter), look, Modifier.fillMaxSize())
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 12.dp, end = 12.dp, top = 8.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.clip(RoundedCornerShape(10.dp)).paper(C.card.copy(alpha = .93f)).border(1.dp, C.line, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 7.dp)) {
                    Text(p.quarter.name(h.ru), style = T.Title.copy(fontSize = 21.sp, lineHeight = 24.sp))
                    Text(w.t("неделя ${h.number(p.week)} · ", "week ${h.number(p.week)} · ") + w.range(p.week), style = T.Tiny)
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(42.dp).clip(CircleShape).paper(C.card.copy(alpha = .93f)).border(1.dp, C.line, CircleShape).clickable { nav.push(Overlay.Settings) },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Settings, w.t("Настройки", "Settings"), tint = C.ink2, modifier = Modifier.size(22.dp)) }
            }
            BubbleView(h, nav, Modifier.align(Alignment.BottomCenter))
        }
        WeekStrip(h, p) { nav.push(Overlay.Week(p.week)) }
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn(w.t("Отметить трапезу", "Log a meal"), Modifier.weight(1f)) { nav.push(Overlay.Meal) }
            Btn(w.t("Тихая трапеза", "Quiet meal"), Modifier.width(140.dp), kind = Kind.GHOST) {
                c.write { settings.update { it.copy(quietSince = System.currentTimeMillis()) } }
            }
        }
    }
}

/** The look of a week's painting, animated as new paint comes in. Screenshots get it still. */
@Composable
fun rememberLook(s: State, p: WeekPaint, rainbow: Boolean, still: Boolean = BuildConfig.SHOTS): () -> Look {
    val anims = remember(p.week) { p.quarter.slots.associate { it.id to Animatable(p.cover(it)) } }
    val fest = remember(p.week) { Animatable(if (p.festival) 1f else 0f) }
    val det = remember(p.week) { Animatable(p.details) }
    LaunchedEffect(p) {
        var k = 0
        // Paint goes on in the order it was laid.
        val order = p.washes.asReversed().map { it.slot }.distinct().asReversed() + p.quarter.slots.map { it.id }
        for (id in order.distinct()) {
            val a = anims[id] ?: continue
            val target = p.cover(id)
            if (abs(a.targetValue - target) < .001f) continue
            if (still) { a.snapTo(target); continue }
            val d = k++ * 380L
            launch { delay(d); a.animateTo(target, tween(2200, easing = FastOutSlowInEasing)) }
        }
        val f = if (p.festival) 1f else 0f
        if (still) fest.snapTo(f) else launch { delay(k * 380L + 900); fest.animateTo(f, tween(1400)) }
        if (still) det.snapTo(p.details) else if (abs(det.value - p.details) > .001f) launch {
            det.animateTo(p.details, tween((abs(p.details - det.value) * 5000).toInt().coerceIn(400, 8000), easing = LinearEasing))
        }
    }
    val trial = s.trialPlant
    val mud = if (trial == null) emptyMap() else p.quarter.slots.associate { it.id to s.mud * p.share(it.id, trial) }.filterValues { it > 0f }
    return { Look(anims.mapValues { it.value.value }, mud, p.fixed, fest.value, det.value, rainbow) }
}

@Composable
private fun BubbleView(h: Home, nav: Nav, modifier: Modifier) {
    val b = nav.bubble
    val w = h.w
    val cur = b.current ?: if (h.paint.empty) Speech(
        w.t("Мастер-красильщик", "The master dyer"),
        w.t(
            "Хмарь опустилась на ${onto(h.paint.quarter)}. Отметь, что было на тарелке, — из этого и будем красить.",
            "The Gloom has settled on ${h.paint.quarter.en}. Log what was on your plate, and we’ll paint with that.",
        ),
        w.t("Тридцать разных растений за неделю — и квартал устроит праздник.", "Thirty different plants in a week, and the quarter throws a festival."),
    ) else null
    LaunchedEffect(b.seq) {
        if (b.current != null && !BuildConfig.SHOTS) { delay(if (b.queue.isEmpty()) 9000 else 5200); b.next() }
    }
    AnimatedVisibility(cur != null, modifier, enter = fadeIn() + slideInVertically { it / 3 }, exit = fadeOut()) {
        val s = cur ?: return@AnimatedVisibility
        Box(Modifier.fillMaxWidth().padding(10.dp).clip(RoundedCornerShape(12.dp)).paper(C.card.copy(alpha = .96f)).border(1.dp, C.line, RoundedCornerShape(12.dp)).clickable { b.next() }) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Dot(s.color, 24.dp, Modifier.padding(top = 1.dp))
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.who, style = T.BodyB.copy(fontSize = 14.sp), modifier = Modifier.weight(1f))
                        if (b.queue.isNotEmpty()) Text(w.t("ещё ${b.queue.size} ›", "${b.queue.size} more ›"), style = T.Tiny)
                    }
                    Text(s.text, style = T.Voice, modifier = Modifier.padding(top = 2.dp))
                    if (s.fact != null) Text(s.fact, style = T.Tiny.copy(fontSize = 12.sp, lineHeight = 16.sp), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun WeekStrip(h: Home, p: WeekPaint, onClick: () -> Unit) {
    val w = h.w
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp).clip(RoundedCornerShape(12.dp)).paper(C.card).border(1.dp, C.line, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(w.count(p.total), style = T.Heading.copy(fontSize = 30.sp, lineHeight = 32.sp))
            Text(
                if (p.festival) w.t("  растений — праздник!", "  plants: a festival!") else w.t("  из ${p.goal} растений за неделю", "  of ${p.goal} plants this week"),
                style = T.Small.copy(color = if (p.festival) C.wine else C.muted), modifier = Modifier.padding(bottom = 4.dp).weight(1f),
            )
            if (p.fixed) Text(
                w.t("протрава", "fixed"), style = T.Small.copy(color = C.wine, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                modifier = Modifier.padding(bottom = 6.dp).rotate(-5f).border(1.5.dp, C.wine, RoundedCornerShape(3.dp)).padding(horizontal = 5.dp),
            )
        }
        Box(Modifier.padding(vertical = 8.dp).fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(C.line)) {
            Box(Modifier.fillMaxWidth((p.total / p.goal).coerceIn(0f, 1f)).height(6.dp).clip(RoundedCornerShape(3.dp)).background(C.wine))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (pig in Pigment.entries) {
                val on = pig in p.pigments
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Dot(pig.color(), 16.dp, empty = !on)
                    Text(w.pigment(pig), style = T.Tiny.copy(fontSize = 10.sp, color = if (on) C.ink2 else C.muted))
                }
            }
        }
        val trial = h.s.trialPlant
        if (trial != null) {
            Box(Modifier.padding(top = 8.dp).fillMaxWidth().height(1.dp).background(C.line))
            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(w.t("Восемь проб · ", "Eight tries · ") + w.name(trial), style = T.Small, modifier = Modifier.weight(1f))
                for (i in 0 until TRIES) Dot(C.wine, 9.dp, Modifier.padding(start = 3.dp), empty = i >= h.s.tries)
            }
        }
    }
}

/** «на Рыночную площадь». */
private fun onto(q: app.dyer.domain.Quarter) = when (q) {
    app.dyer.domain.Quarter.MARKET -> "Рыночную площадь"
    app.dyer.domain.Quarter.MILL -> "мельницу"
    app.dyer.domain.Quarter.HARBOUR -> "гавань"
    app.dyer.domain.Quarter.CASTLE -> "замок"
}
