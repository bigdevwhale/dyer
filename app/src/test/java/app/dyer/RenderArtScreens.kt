package app.dyer

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import app.dyer.data.DemoSeed
import app.dyer.domain.Quarter
import app.dyer.ui.Bubble
import app.dyer.ui.Home
import app.dyer.ui.Nav
import app.dyer.ui.Speech
import app.dyer.ui.Words
import app.dyer.ui.art.Look
import app.dyer.ui.art.Scenes
import app.dyer.ui.art.renderScene
import app.dyer.ui.components.LocalToast
import app.dyer.ui.components.Toaster
import app.dyer.ui.kingdom.Finale
import app.dyer.ui.kingdom.KingdomScreen
import app.dyer.ui.onboarding.Onboarding
import app.dyer.ui.settings.SettingsSheet
import app.dyer.ui.shelf.PlantSheet
import app.dyer.ui.shelf.ShelfScreen
import app.dyer.ui.square.MealSheet
import app.dyer.ui.square.Report
import app.dyer.ui.square.SquareScreen
import app.dyer.ui.square.WeekSheet
import app.dyer.ui.theme.C
import app.dyer.ui.theme.DyerTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Every main screen over the demo history, drawn to PNGs (run with `-Prender`), English and Russian. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }
    private val app get() = ApplicationProvider.getApplicationContext<DyerApp>()

    private fun home(): Home = runBlocking {
        val c = app.container
        DemoSeed.seedIfEmpty(c)
        val s = c.repo.refresh()
        Home(s, c.settings.now(), Words(lang == "ru", s.cat), System.currentTimeMillis())
    }

    private fun nav() = Nav(mutableStateListOf(), {}, Bubble())

    private fun shot(name: String, h: Home, content: @Composable (Home) -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            DyerTheme {
                CompositionLocalProvider(LocalToast provides Toaster()) {
                    Box(Modifier.fillMaxSize().background(C.bg)) { content(h) }
                }
            }
        }
        rule.mainClock.advanceTimeBy(3_000)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    @Test fun welcome() = shot("0-welcome", home()) { Onboarding(it.w) }
    @Test fun square() = shot("1-square", home()) { SquareScreen(it, nav()) }
    @Test fun spoken() {
        val h = home()
        val n = nav()
        val slot = Quarter.MARKET.slot("rose")
        n.bubble.say(listOf(Speech(slot.who(h.ru), slot.first(h.ru), h.s.cat["redcabbage"]!!.fact(h.ru), androidx.compose.ui.graphics.Color(0xFF4A4592)), Speech("x", "y")))
        shot("2-spoken", h) { SquareScreen(it, n) }
    }
    @Test fun meal() = shot("3-meal", home()) { h -> SquareScreen(h, nav()); MealSheet(h, nav()) {} }
    @Test fun week() = shot("4-week", home()) { h -> SquareScreen(h, nav()); WeekSheet(h, nav(), h.week) {} }
    @Test fun shelf() = shot("5-shelf", home()) { ShelfScreen(it, nav()) }
    @Test fun plant() = shot("6-plant", home()) { h -> ShelfScreen(h, nav()); PlantSheet(h, nav(), "beet") {} }
    @Test fun kingdom() = shot("7-kingdom", home()) { KingdomScreen(it, nav()) }
    @Test fun report() = shot("8-report", home()) { h -> SquareScreen(h, nav()); Report(h, h.week - 7) {} }
    @Test fun settings() = shot("x-settings", home()) { h -> SquareScreen(h, nav()); SettingsSheet(h) {} }
    @Test fun finale() = shot("x-finale", home()) { Finale(it) {} }

    /** Each quarter grey and fully painted, straight from the painter. */
    @Test fun quarters() {
        for (q in Quarter.entries) {
            val art = Scenes.of(q)
            for ((tag, look) in listOf("grey" to Look(emptyMap()), "full" to Look(q.slots.associate { it.id to 1f }, fixed = true, festival = 1f, details = 3f))) {
                val bmp = renderScene(art, look, 800, 1200, Density(2f)).asAndroidBitmap()
                File(out, "q-${q.name.lowercase()}-$tag.png").outputStream().use { bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            }
        }
    }
}

class RenderArtScreensEn : ScreensBase() {
    override val lang = "en"
}

@Config(qualifiers = "ru-w411dp-h891dp-xxhdpi")
class RenderArtScreensRu : ScreensBase() {
    override val lang = "ru"
}
