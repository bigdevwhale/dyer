package app.dyer.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import app.dyer.R
import app.dyer.domain.WeekPaint
import app.dyer.ui.Home
import app.dyer.ui.art.Look
import app.dyer.ui.art.Scenes
import app.dyer.ui.art.renderScene
import java.io.File

/** A painted week as a picture: the quarter, and a strip of paper with its name and count. */
object ShareWeek {
    private const val W = 1080
    private const val H = 1500
    private const val BAND = 220

    fun paint(ctx: Context, h: Home, p: WeekPaint): Bitmap {
        val look = Look(p.quarter.slots.associate { it.id to p.cover(it) }, fixed = p.fixed, festival = if (p.festival) 1f else 0f, details = p.details, rainbow = h.settings.finaleSeen)
        val scene = renderScene(Scenes.of(p.quarter), look, W, H - BAND, Density(W / 400f)).asAndroidBitmap()
        val out = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        c.drawColor(0xFFF5F0E4.toInt())
        c.drawBitmap(scene, 0f, 0f, null)
        val sign = ResourcesCompat.getFont(ctx, R.font.ruslan) ?: Typeface.SERIF
        val serif = ResourcesCompat.getFont(ctx, R.font.pt_serif_regular) ?: Typeface.SERIF
        val ink = 0xFF2B241D.toInt()
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = sign; textSize = 64f; color = ink }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = serif; textSize = 36f; color = 0xFF5A4E40.toInt() }
        val w = h.w
        c.drawText(p.quarter.name(h.ru), 56f, H - BAND + 92f, title)
        val line = w.range(p.week) + " · " + w.plants(p.total) + (if (p.festival) w.t(" · праздник", " · a festival") else "")
        c.drawText(line, 58f, H - BAND + 150f, small)
        c.drawText(w.t("Красильня", "Dyer"), W - 56f - small.measureText(w.t("Красильня", "Dyer")), H - BAND + 150f, small)
        return out
    }

    fun share(ctx: Context, h: Home, p: WeekPaint) {
        val dir = File(ctx.cacheDir, "weeks").apply { mkdirs() }
        val f = File(dir, "week-${p.week}.png")
        f.outputStream().use { paint(ctx, h, p).compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".files", f)
        val send = Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        ctx.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
