package app.dyer

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import app.dyer.data.DemoSeed
import app.dyer.data.DyerDb
import app.dyer.data.Repo
import app.dyer.data.SettingsStore
import app.dyer.domain.Catalogue
import app.dyer.notify.Bell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val app: Context = context.applicationContext
    val catalogue: Catalogue by lazy { Catalogue.load { runCatching { app.assets.open(it) }.getOrNull() } }
    val db = DyerDb.create(app)
    val settings = SettingsStore(app)
    val repo = Repo(db, settings) { catalogue }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Fire-and-forget so a write survives the screen being closed right after tapping. */
    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }
}

class DyerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Bell.channel(this)
        // The demo shows nothing until its history is in place, so the first screen doesn't jump.
        if (BuildConfig.DEMO) container.repo.hold = true
        container.scope.launch {
            container.catalogue
            if (BuildConfig.DEMO) DemoSeed.seedIfEmpty(container)
            container.repo.hold = false
            container.repo.refresh()
        }
    }
}

val Context.container: AppContainer get() = (applicationContext as DyerApp).container

/** Resources in the in-app language, which AppCompat doesn't localise outside activities before Android 13. */
fun Context.localized(): Context {
    val tags = AppCompatDelegate.getApplicationLocales()
    if (tags.isEmpty) return this
    val config = Configuration(resources.configuration)
    config.setLocale(tags[0])
    return createConfigurationContext(config)
}

/** Is this context in Russian? */
val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"
