package app.dyer.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import app.dyer.R
import app.dyer.container
import app.dyer.data.Settings
import app.dyer.domain.State
import app.dyer.domain.WeekPaint
import app.dyer.domain.date
import app.dyer.domain.week
import app.dyer.notify.Bell
import app.dyer.ru
import app.dyer.ui.components.LocalToast
import app.dyer.ui.components.ToastHost
import app.dyer.ui.components.Toaster
import app.dyer.ui.kingdom.Finale
import app.dyer.ui.kingdom.KingdomScreen
import app.dyer.ui.kingdom.WeekView
import app.dyer.ui.onboarding.Onboarding
import app.dyer.ui.settings.SettingsSheet
import app.dyer.ui.shelf.PlantSheet
import app.dyer.ui.shelf.RecipesSheet
import app.dyer.ui.shelf.ShelfScreen
import app.dyer.ui.square.MealSheet
import app.dyer.ui.square.QuietVeil
import app.dyer.ui.square.Report
import app.dyer.ui.square.SquareScreen
import app.dyer.ui.square.WeekSheet
import app.dyer.ui.theme.C
import app.dyer.ui.theme.T
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId

/** Everything a screen reads, at one moment. */
class Home(val s: State, val settings: Settings, val w: Words, val now: Long) {
    val ru get() = w.ru
    val zone: ZoneId get() = s.zone
    val week: Long get() = now.week(zone)
    val paint: WeekPaint get() = s.week(week)
    val today: LocalDate get() = now.date(zone)
    /** Weeks since the first, counting from one. */
    fun number(week: Long) = ((week - s.firstWeek) / 7 + 1).toInt()
}

enum class Tab { SQUARE, SHELF, KINGDOM }

sealed interface Overlay {
    data object Meal : Overlay
    data class Week(val week: Long) : Overlay
    data class Plant(val id: String) : Overlay
    data object Recipes : Overlay
    data object Settings : Overlay
    data class Report(val week: Long) : Overlay
    data class View(val week: Long) : Overlay
    data object Finale : Overlay
}

/** What the townsfolk say over the quarter, one after another. */
class Speech(val who: String, val text: String, val fact: String? = null, val color: Color = C.line)

class Bubble {
    val queue: SnapshotStateList<Speech> = mutableStateListOf()
    var current by mutableStateOf<Speech?>(null)
    var seq by mutableLongStateOf(0)
    fun say(list: List<Speech>) {
        if (list.isEmpty()) return
        queue.clear()
        queue.addAll(list.drop(1))
        current = list.first()
        seq++
    }
    fun next() {
        current = if (queue.isEmpty()) null else queue.removeAt(0)
        seq++
    }
}

class Nav(val overlays: SnapshotStateList<Overlay>, val tab: (Tab) -> Unit, val bubble: Bubble) {
    fun push(o: Overlay) = overlays.add(o)
    fun pop() { if (overlays.isNotEmpty()) overlays.removeAt(overlays.lastIndex) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DyerRoot(settings: Settings) {
    val ctx = LocalContext.current
    val c = ctx.container
    val state by c.repo.state.collectAsStateWithLifecycle()
    val toaster = remember { Toaster() }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val view = LocalView.current
    LaunchedEffect(Unit) {
        (ctx as? Activity)?.window?.let { WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = true }
    }
    // Settings feed the state (goal, trial plant), so a change in them is a fresh look at the ledger.
    LaunchedEffect(settings) { c.repo.refresh() }
    val s = state ?: run { Box(Modifier.fillMaxSize().background(C.bg)); return }
    val words = remember(ctx.ru, s.cat) { Words(ctx.ru, s.cat) }
    if (!settings.onboarded) {
        CompositionLocalProvider(LocalToast provides toaster) { Box(Modifier.fillMaxSize().background(C.bg)) { Onboarding(words); ToastHost(toaster) } }
        return
    }
    val life = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        life.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            c.repo.refresh()
            Bell.plan(ctx)
            while (true) { now = System.currentTimeMillis(); delay(30_000) }
        }
    }
    val home = Home(s, settings, words, now)

    var tab by remember { mutableStateOf(Tab.SQUARE) }
    val overlays = remember { mutableStateListOf<Overlay>() }
    val bubble = remember { Bubble() }
    val nav = remember { Nav(overlays, { tab = it }, bubble) }
    BackHandler(enabled = overlays.isEmpty() && tab != Tab.SQUARE) { tab = Tab.SQUARE }

    // A new week: first, how the last one went.
    LaunchedEffect(home.week) {
        if (settings.reportedWeek < home.week) {
            val last = home.week - 7
            if (last >= s.firstWeek && !s.week(last).empty) nav.push(Overlay.Report(last))
            else c.write { this.settings.update { it.copy(reportedWeek = home.week) } }
        }
    }

    CompositionLocalProvider(LocalToast provides toaster) {
        Box(Modifier.fillMaxSize().background(C.bg)) {
            Column(Modifier.fillMaxSize().imePadding()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        Tab.SQUARE -> SquareScreen(home, nav)
                        Tab.SHELF -> ShelfScreen(home, nav)
                        Tab.KINGDOM -> KingdomScreen(home, nav)
                    }
                }
                if (!WindowInsets.isImeVisible) TabBar(tab) { tab = it }
            }
            when (val o = overlays.lastOrNull()) {
                Overlay.Meal -> MealSheet(home, nav) { nav.pop() }
                is Overlay.Week -> WeekSheet(home, nav, o.week) { nav.pop() }
                is Overlay.Plant -> PlantSheet(home, nav, o.id) { nav.pop() }
                Overlay.Recipes -> RecipesSheet(home, nav) { nav.pop() }
                Overlay.Settings -> SettingsSheet(home) { nav.pop() }
                is Overlay.Report -> Report(home, o.week) {
                    nav.pop()
                    c.write { this.settings.update { it.copy(reportedWeek = home.week) } }
                }
                is Overlay.View -> WeekView(home, nav, o.week) { nav.pop() }
                Overlay.Finale -> Finale(home) { nav.pop() }
                null -> Unit
            }
            if (settings.quietSince > 0) QuietVeil(home, nav)
            ToastHost(toaster)
        }
    }
}

@Composable
private fun TabBar(tab: Tab, onTab: (Tab) -> Unit) {
    val items = listOf(Tab.SQUARE to R.string.tab_square, Tab.SHELF to R.string.tab_shelf, Tab.KINGDOM to R.string.tab_kingdom)
    Column(Modifier.fillMaxWidth().background(C.paper)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(C.line))
        Row(Modifier.fillMaxWidth().navigationBarsPadding()) {
            for ((t, label) in items) {
                val sel = t == tab
                val color = if (sel) C.ink else C.muted
                Column(Modifier.weight(1f).clickable { onTab(t) }.padding(top = 8.dp, bottom = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(24.dp)) { icon(t, color, sel) }
                    Text(stringResource(label), style = T.Small.copy(color = color, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal))
                }
            }
        }
    }
}

private fun DrawScope.icon(t: Tab, color: Color, sel: Boolean) {
    val k = size.width / 24f
    val st = Stroke(1.6f * k)
    when (t) {
        // A little house with a red roof once painted.
        Tab.SQUARE -> {
            drawRect(color, Offset(5f * k, 11f * k), Size(14f * k, 9f * k), style = st)
            val roof = androidx.compose.ui.graphics.Path().apply { moveTo(3f * k, 12f * k); lineTo(12f * k, 4f * k); lineTo(21f * k, 12f * k); close() }
            if (sel) drawPath(roof, Color(0xFFC4472F))
            drawPath(roof, color, style = st)
            drawRect(color, Offset(10.5f * k, 15f * k), Size(3f * k, 5f * k))
        }
        // A jar with paint in it.
        Tab.SHELF -> {
            if (sel) drawRoundRect(Color(0xFF4F9C9A), Offset(6f * k, 12f * k), Size(12f * k, 8f * k), CornerRadius(2f * k))
            drawRoundRect(color, Offset(6f * k, 7f * k), Size(12f * k, 13f * k), CornerRadius(2.5f * k), style = st)
            drawRect(color, Offset(7f * k, 3.5f * k), Size(10f * k, 3f * k), style = st)
        }
        // Three little flags on a hill: the kingdom.
        Tab.KINGDOM -> {
            drawArc(color, 180f, 180f, false, Offset(2f * k, 12f * k), Size(20f * k, 14f * k), style = st)
            drawLine(color, Offset(12f * k, 12f * k), Offset(12f * k, 3f * k), 1.6f * k)
            val flag = androidx.compose.ui.graphics.Path().apply { moveTo(12f * k, 3f * k); lineTo(19f * k, 5.5f * k); lineTo(12f * k, 8f * k); close() }
            if (sel) drawPath(flag, Color(0xFFE7AD1C))
            drawPath(flag, color, style = st)
        }
    }
}
