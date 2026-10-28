package app.dyer.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.dyer.MainActivity
import app.dyer.R
import app.dyer.container
import app.dyer.domain.Guild
import app.dyer.domain.Pigment
import app.dyer.domain.date
import app.dyer.domain.day
import app.dyer.domain.week
import app.dyer.localized
import app.dyer.ru
import app.dyer.ui.Words
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** One evening knock, «Что сегодня было на тарелке?», only on days with nothing logged. */
object Bell {
    private const val CHANNEL = "evening"
    private const val WORK = "evening"

    fun channel(c: Context) {
        val ch = NotificationChannel(CHANNEL, c.localized().let { if (it.ru) "Вечерний вопрос" else "The evening question" }, NotificationManager.IMPORTANCE_DEFAULT)
        c.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    /** The next time the clock reaches [hour] after [now]. */
    fun next(hour: Int, now: Long, zone: ZoneId): Long {
        val today = LocalDate.ofEpochDay(now.day(zone))
        val t = today.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
        return if (t > now) t else today.plusDays(1).atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
    }

    suspend fun plan(c: Context) {
        val app = c.applicationContext
        val st = app.container.settings.now()
        val wm = WorkManager.getInstance(app)
        if (!st.onboarded || st.bellHour < 0) { wm.cancelUniqueWork(WORK); return }
        val now = System.currentTimeMillis()
        val req = OneTimeWorkRequestBuilder<BellWorker>().setInitialDelay(next(st.bellHour, now, ZoneId.systemDefault()) - now, TimeUnit.MILLISECONDS).build()
        wm.enqueueUniqueWork(WORK, ExistingWorkPolicy.REPLACE, req)
    }

    fun post(c: Context, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val pi = PendingIntent.getActivity(c, 1, Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_jar)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(c).notify(1, n)
    }
}

class BellWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val c = ctx.container
        val st = c.settings.now()
        val s = c.repo.refresh()
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val today = now.day(zone)
        if (st.onboarded && s.meals.none { it.at.day(zone) == today }) {
            val w = Words(ctx.localized().ru, s.cat)
            val p = s.week(now.week(zone))
            val missing = Pigment.entries.filter { it !in p.pigments }
            val picks = Guild.order(s, p, now.date(zone)).joinToString(", ") { it.plant.name(w.ru).lowercase() }
            val text = (if (missing.isNotEmpty()) w.t("Кварталу не хватает ${w.pigmentOf(missing.first())}. ", "The quarter is short of ${w.pigmentOf(missing.first())}. ") else "") +
                w.t("Гильдия просит: $picks.", "The guild asks for: $picks.")
            Bell.post(ctx, w.t("Что сегодня было на тарелке?", "What was on your plate today?"), text)
        }
        Bell.plan(ctx)
        return Result.success()
    }
}
