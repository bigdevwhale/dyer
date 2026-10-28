package app.dyer.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.dyer.container
import app.dyer.domain.Quarter
import app.dyer.ui.Words
import app.dyer.ui.art.Look
import app.dyer.ui.art.Scene
import app.dyer.ui.art.Scenes
import app.dyer.ui.components.Btn
import app.dyer.ui.components.Dot
import app.dyer.ui.components.Kind
import app.dyer.ui.components.paper
import app.dyer.ui.theme.C
import app.dyer.ui.theme.T

/** Three pages: the Gloom, a tomato painting the roofs, and the rules. */
@Composable
fun Onboarding(w: Words, startPage: Int = 0) {
    val c = LocalContext.current.container
    var page by remember { mutableIntStateOf(startPage) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { c.write { settings.update { it.copy(onboarded = true) } } }
    val art = Scenes.of(Quarter.MARKET)
    val ids = Quarter.MARKET.slots.map { it.id }
    val anims = remember { ids.associateWith { Animatable(0f) } }
    val fest = remember { Animatable(0f) }
    LaunchedEffect(page) {
        when (page) {
            0 -> ids.forEach { anims[it]!!.snapTo(0f) }
            1 -> for (id in listOf("roofs", "sky", "windows")) anims[id]!!.animateTo(1f, tween(1800))
            else -> { for (id in ids) anims[id]!!.snapTo(1f); fest.animateTo(1f, tween(1200)) }
        }
    }
    val pages = listOf(
        w.t("Хмарь выпила краски", "The Gloom drank the colours") to w.t(
            "Над королевством прошла Серая Хмарь, и всё стало цвета пепла: черепица, луга, вечернее небо. Ты ученик красильщика, а краски можно вернуть только из живых растений.",
            "The Grey Gloom passed over the kingdom, and everything turned the colour of ash: roof tiles, meadows, the evening sky. You are the dyer’s apprentice, and colours come back only from living plants.",
        ),
        w.t("Тарелка — это краски", "A plate is paint") to w.t(
            "Отмечай, что было на тарелке. Помидор красит черепицу, черника — небо, лук зажигает окна, гречка и чечевица кладут мостовую. Чем больше тарелки занимали растения, тем шире кисть.",
            "Log what was on your plate. Tomato paints the roof tiles, bilberries the sky, onions light the windows, buckwheat and lentils lay the cobbles. The more of the plate was plants, the wider the brush.",
        ),
        w.t("Тридцать за неделю", "Thirty a week") to w.t(
            "Тридцать разных растений за неделю — и квартал устраивает праздник. Пряности и травы идут каплями по четверти, квашеное закрепляет краски. Никаких калорий, весов и запретов: чипсы просто ничего не красят.",
            "Thirty different plants in a week, and the quarter throws a festival. Herbs and spices are drops worth a quarter; fermented food fixes the colours. No calories, no scales, no bans: crisps simply paint nothing.",
        ),
    )
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Scene(art, { Look(anims.mapValues { it.value.value }, festival = fest.value) }, Modifier.fillMaxSize())
        }
        Column(Modifier.fillMaxWidth().paper().navigationBarsPadding().padding(20.dp)) {
            Text(pages[page].first, style = T.Title.copy(fontSize = 25.sp))
            Text(pages[page].second, style = T.Body, modifier = Modifier.padding(top = 8.dp))
            Row(Modifier.padding(top = 14.dp)) {
                for (i in pages.indices) Dot(C.ink, 8.dp, Modifier.padding(end = 6.dp), empty = i != page)
            }
            Btn(if (page < pages.lastIndex) w.t("Дальше", "Next") else w.t("Взять кисть", "Pick up the brush"), Modifier.fillMaxWidth().padding(top = 14.dp)) {
                if (page < pages.lastIndex) page++
                else if (Build.VERSION.SDK_INT >= 33) ask.launch(Manifest.permission.POST_NOTIFICATIONS)
                else c.write { settings.update { it.copy(onboarded = true) } }
            }
        }
    }
}

private val Int.sp get() = androidx.compose.ui.unit.TextUnit(toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
