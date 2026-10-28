package app.dyer.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.dyer.BuildConfig
import app.dyer.container
import app.dyer.data.Settings
import app.dyer.notify.Bell
import app.dyer.ui.Home
import app.dyer.ui.Words
import app.dyer.ui.components.Card
import app.dyer.ui.components.Chip
import app.dyer.ui.components.Flow
import app.dyer.ui.components.Gap
import app.dyer.ui.components.Sheet
import app.dyer.ui.theme.C
import app.dyer.ui.theme.T

/** The StillCraft bot: deep links file a bug or an idea for this app, or follow its releases. */
object Community {
    private const val BOT = "https://t.me/ArcaneWhisperBot?start="
    const val SLUG = "dyer"
    const val BUG = "${BOT}bug_$SLUG"
    const val IDEA = "${BOT}wish_$SLUG"
    const val FOLLOW = "${BOT}f_$SLUG"
    const val CHAT = "https://t.me/+Q4gFEDk8O842OGVi"
}

@Composable
fun SettingsSheet(h: Home, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    val w = h.w
    val st = h.settings
    fun save(f: (Settings) -> Settings) = c.write { settings.update(f); Bell.plan(ctx) }

    Sheet(w.t("Настройки", "Settings"), onClose, w.t("Закрыть", "Close")) {
        Card {
            Text(w.t("Язык", "Language"), style = T.Heading)
            Flow(Modifier.padding(top = 8.dp)) {
                Chip("Русский", h.ru) { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru")) }
                Chip("English", !h.ru) { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) }
            }
        }
        Gap(10)
        Card {
            Text(w.t("Сколько растений до праздника", "Plants for a festival"), style = T.Heading)
            Text(
                w.t("Тридцать разных растений в неделю — столько ели люди с самой разнообразной микрофлорой в исследовании American Gut. В трудную неделю можно взять малую кисть.",
                    "Thirty different plants a week: that’s what the people with the most varied gut flora ate in the American Gut study. In a hard week, take the small brush."),
                style = T.Small,
            )
            Flow(Modifier.padding(top = 8.dp)) {
                Chip(w.t("30 — полная кисть", "30, the full brush"), st.goal == 30) { save { it.copy(goal = 30) } }
                Chip(w.t("15 — малая кисть", "15, the small brush"), st.goal == 15) { save { it.copy(goal = 15) } }
            }
        }
        Gap(10)
        Card {
            Text(w.t("Вечерний вопрос", "The evening question"), style = T.Heading)
            Text(w.t("«Что сегодня было на тарелке?» — только если за день ещё ничего не отмечено.", "“What was on your plate today?”, only if nothing was logged that day."), style = T.Small)
            Flow(Modifier.padding(top = 8.dp)) {
                for (hr in listOf(19, 20, 21)) Chip("$hr:00", st.bellHour == hr) { save { it.copy(bellHour = hr) } }
                Chip(w.t("не спрашивать", "never"), st.bellHour < 0) { save { it.copy(bellHour = -1) } }
            }
        }
        Gap(10)
        Card(onClick = { c.write { settings.update { it.copy(onboarded = false) } }; onClose() }) {
            Text(w.t("Знакомство", "Introductions"), style = T.Heading)
            Text(w.t("Посмотреть, как всё устроено, с начала ›", "See how it all works from the start ›"), style = T.Small.copy(color = C.wine))
        }
        Gap(10)
        CommunityLinks(w)
        Gap(10)
        Card {
            Text(w.t("О «Красильне»", "About Dyer"), style = T.Heading)
            Text(
                w.t(
                    "Игра про разнообразную еду без подсчёта калорий. Каждое растение отдаёт свой пигмент и раскрашивает королевство, выцветшее от Хмари. Тридцать растений в неделю — наблюдение, а не закон: разнообразие растений связано с разнообразием микрофлоры, но приложение не врач. Без интернета, рекламы и аккаунта; всё хранится на телефоне.",
                    "A game about eating a variety of plants, with no calorie counting. Each plant gives its pigment and repaints a kingdom faded by the Gloom. Thirty plants a week is an observation, not a law: plant variety goes with varied gut flora, but the app is not a doctor. No internet, no ads, no account; everything stays on the phone.",
                ),
                style = T.Small,
            )
            Text(
                w.t("Версия ${BuildConfig.VERSION_NAME} · шрифты PT Serif, Ruslan Display и Caveat (OFL) · исходники на GitHub", "Version ${BuildConfig.VERSION_NAME} · PT Serif, Ruslan Display and Caveat fonts (OFL) · source on GitHub"),
                style = T.Small.copy(color = C.wine),
                modifier = Modifier.padding(top = 8.dp).clickable { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bigdevwhale/dyer"))) } },
            )
        }
        Gap(20)
    }
}

/** Report a bug, suggest an idea, follow updates, join the chat; each opens Telegram. */
@Composable
private fun CommunityLinks(w: Words) {
    val ctx = LocalContext.current
    fun open(url: String) { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
    Card {
        Text(w.t("Сообщество", "Community"), style = T.Heading)
        Text(
            w.t("Приложение делается вместе с теми, кто им пользуется. Всё идёт через бота StillCraft в Telegram.", "The app is made together with the people who use it. It all goes through the StillCraft bot on Telegram."),
            style = T.Small, modifier = Modifier.padding(bottom = 4.dp),
        )
        for ((icon, label, sub, url) in listOf(
            Link("🐞", w.t("Сообщить о баге", "Report a bug"), w.t("что сломалось и как повторить", "what broke and how to repeat it"), Community.BUG),
            Link("💡", w.t("Предложить идею", "Suggest an idea"), w.t("растение, квартал, функция", "a plant, a quarter, a feature"), Community.IDEA),
            Link("🔔", w.t("Подписаться на обновления", "Follow updates"), w.t("бот напишет, когда выйдет версия", "the bot writes when a version is out"), Community.FOLLOW),
            Link("💬", w.t("Чат StillCraft", "StillCraft chat"), w.t("все приложения и люди", "all the apps and people"), Community.CHAT),
        )) {
            Row(Modifier.fillMaxWidth().clickable { open(url) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(icon, style = T.Heading, modifier = Modifier.width(34.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, style = T.BodyB)
                    Text(sub, style = T.Small)
                }
                Text("›", style = T.Heading.copy(color = C.wine))
            }
        }
    }
}

private data class Link(val icon: String, val label: String, val sub: String, val url: String)
