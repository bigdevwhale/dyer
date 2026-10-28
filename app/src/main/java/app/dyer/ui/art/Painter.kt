package app.dyer.ui.art

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/** How a quarter looks at one moment: the paint on each thing, the trial plant's mud, and what's been drawn in quiet. */
class Look(
    /** 0..1 per thing; a thing at 0 is still the Gloom's grey. */
    val cover: Map<String, Float>,
    /** Muddy overlay per thing, 0..1. */
    val mud: Map<String, Float> = emptyMap(),
    /** Fermented food fixed the colours: fuller and a touch brighter. */
    val fixed: Boolean = false,
    /** The festival garlands, 0..1. */
    val festival: Float = 0f,
    /** Quiet-meal details: 1.5 is the first done and the second half drawn. */
    val details: Float = 0f,
    /** After the Gloom is painted, a faint rainbow stays in every painted sky. */
    val rainbow: Boolean = false,
)

private val INK = Color(0xFF2B241D)
private val HATCH = Color(0xFF5F584E)
private val MUD = Color(0xFF6E5D4E)
private val saturate = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1.18f) })

/** A quarter, filling its box from the bottom so the sky is what gets cut on short screens. */
@Composable
fun Scene(art: SceneArt, look: () -> Look, modifier: Modifier = Modifier) {
    Spacer(
        modifier.drawWithCache {
            val base = layer { fitted { drawBase(art) } }
            val ink = layer { fitted { drawInk(art) } }
            onDrawBehind { paint(art, look(), base, ink) }
        },
    )
}

/** The same picture off screen, for sharing and for the album. */
fun renderScene(art: SceneArt, look: Look, w: Int, h: Int, density: Density): ImageBitmap {
    val size = Size(w.toFloat(), h.toFloat())
    fun layer(block: DrawScope.() -> Unit) = ImageBitmap(w, h).also { CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(it), size, block) }
    val base = layer { fitted { drawBase(art) } }
    val ink = layer { fitted { drawInk(art) } }
    return layer { paint(art, look, base, ink) }
}

private fun DrawScope.paint(art: SceneArt, look: Look, base: ImageBitmap, ink: ImageBitmap) {
    drawImage(base)
    fitted { drawColour(art, look) }
    drawImage(ink)
    fitted {
        drawDetails(art, look.details)
        if (look.festival > 0f) drawGarlands(art, look.festival)
    }
    drawRect(ShaderBrush(ImageShader(grain, TileMode.Repeated, TileMode.Repeated)), alpha = .55f)
}

private fun CacheDrawScope.layer(block: DrawScope.() -> Unit): ImageBitmap {
    val bmp = ImageBitmap(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1))
    CanvasDrawScope().draw(this, layoutDirection, Canvas(bmp), size, block)
    return bmp
}

/** Scales the 400 × 600 scene to cover the box, anchored at the bottom. */
private inline fun DrawScope.fitted(block: DrawScope.() -> Unit) {
    val s = max(size.width / 400f, size.height / 600f)
    val dx = (size.width - 400f * s) / 2f
    val dy = size.height - 600f * s
    withTransform({ translate(dx, dy); scale(s, s, Offset.Zero) }, block)
}

fun lum(c: Color) = .299f * c.red + .587f * c.green + .114f * c.blue

/** What the Gloom leaves of a colour: a pale grey of the same lightness. */
fun grey(c: Color): Color {
    val v = (160 + lum(c) * 78) / 255f
    return Color(v, v - 3 / 255f, v - 9 / 255f)
}

private fun darker(c: Color) = Color(c.red * .68f, c.green * .68f, c.blue * .68f)

private fun DrawScope.drawBase(art: SceneArt) {
    for (t in art.things) for (sh in t.shapes) {
        if (sh.only) continue
        drawPath(sh.path, grey(sh.color))
        if (sh.ink && lum(sh.color) < .55f) clipPath(sh.path) { hatch(sh.path.getBounds()) }
    }
}

private fun DrawScope.hatch(b: Rect) {
    var x = b.left - b.height
    while (x < b.right) {
        drawLine(HATCH, Offset(x, b.bottom), Offset(x + b.height, b.top), .7f, alpha = .45f)
        x += 5f
    }
}

/** Ink outlines; each thing first clears the ink of whatever it stands in front of. */
private fun DrawScope.drawInk(art: SceneArt) {
    val outline = Stroke(1.3f, join = StrokeJoin.Round)
    for (t in art.things) {
        for (sh in t.shapes) if (!sh.only) drawPath(sh.path, Color.Black, blendMode = BlendMode.Clear)
        for (sh in t.shapes) if (sh.ink && !sh.only) drawPath(sh.path, INK, style = outline)
        for (l in t.lines) drawPath(l.path, INK, alpha = l.alpha, style = Stroke(l.width, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** The wash spreads from the thing's origin with an uneven, watery edge. */
private fun blob(t: Thing, r: Float): Path {
    val rnd = Random(t.id.hashCode())
    val p1 = rnd.nextFloat() * 6.28f
    val p2 = rnd.nextFloat() * 6.28f
    val p3 = rnd.nextFloat() * 6.28f
    val out = Path()
    for (i in 0..72) {
        val a = (i / 72f * 2 * PI).toFloat()
        val rr = r * (1 + .16f * sin(3 * a + p1) + .09f * sin(7 * a + p2) + .05f * sin(13 * a + p3))
        val x = t.origin.x + rr * cos(a)
        val y = t.origin.y + rr * sin(a)
        if (i == 0) out.moveTo(x, y) else out.lineTo(x, y)
    }
    out.close()
    return out
}

private fun DrawScope.drawColour(art: SceneArt, look: Look) {
    val filter = if (look.fixed) saturate else null
    val alpha = if (look.fixed) 1f else .86f
    for (t in art.things) {
        val c = look.cover[t.id] ?: 0f
        if (c <= .002f) continue
        val vis = art.visible[t.id] ?: continue
        clipPath(vis) {
            val front = if (c < .999f) blob(t, c * t.reach) else null
            val body: DrawScope.() -> Unit = {
                for (sh in t.shapes) {
                    if (sh.brush != null) drawPath(sh.path, sh.brush, alpha = alpha * sh.alpha, colorFilter = filter)
                    else drawPath(sh.path, sh.color, alpha = alpha * sh.alpha, colorFilter = filter)
                    if (sh.wet && sh.brush == null) drawPath(sh.path, darker(sh.color), alpha = .45f * alpha, style = Stroke(2.4f), colorFilter = filter)
                }
                val m = look.mud[t.id] ?: 0f
                if (m > 0f) for (sh in t.shapes) if (!sh.only) drawPath(sh.path, MUD, alpha = m)
                if (look.rainbow && t.id == "sky") rainbow()
            }
            if (front == null) body() else {
                clipPath(front) { body() }
                // The wet edge where the paint stops for now.
                val tint = t.shapes.firstOrNull { !it.only }?.color ?: INK
                clipPath(t.solid) { drawPath(front, darker(tint), alpha = .28f, style = Stroke(3f)) }
            }
        }
    }
}

private fun DrawScope.rainbow() {
    val colors = listOf(0xFFC4472F, 0xFFEF8424, 0xFFE7AD1C, 0xFF5F8A37, 0xFF4F7FC0, 0xFF6B3FA0)
    colors.forEachIndexed { i, c ->
        val r = 270f - i * 7f
        drawArc(Color(c), 180f, 180f, false, Offset(200f - r, 380f - r), Size(2 * r, 2 * r), alpha = .3f, style = Stroke(7f))
    }
}

private fun DrawScope.drawDetails(art: SceneArt, done: Float) {
    if (done <= 0f) return
    val pm = PathMeasure()
    art.details.forEachIndexed { i, d ->
        val p = (done - i).coerceIn(0f, 1f)
        if (p <= 0f) return@forEachIndexed
        val wash = ((p - .8f) / .2f).coerceIn(0f, 1f)
        if (wash > 0f) {
            for (sh in d.fills) drawPath(sh.path, sh.color, alpha = wash)
            d.strokes.forEachIndexed { k, l -> drawPath(l.path, d.strokeColors.getOrElse(k) { INK }, alpha = wash, style = Stroke(l.width, cap = StrokeCap.Round)) }
        }
        for (line in d.lines) {
            pm.setPath(line, false)
            val seg = Path()
            pm.getSegment(0f, pm.length * (p / .8f).coerceAtMost(1f), seg, true)
            drawPath(seg, INK, style = Stroke(1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

private val flagColors = listOf(0xFFC4472F, 0xFFE7AD1C, 0xFF4F9C9A, 0xFF8B1E3F, 0xFF5F8A37, 0xFFEF8424, 0xFF6B3FA0).map { Color(it) }

private fun DrawScope.drawGarlands(art: SceneArt, f: Float) {
    art.garlands.forEachIndexed { k, g ->
        val (a, c, b) = g
        val line = Path().apply { moveTo(a.x, a.y); quadraticTo(c.x, c.y, b.x, b.y) }
        drawPath(line, INK, alpha = f, style = Stroke(1f))
        for (i in 1 until 10) {
            val t = i / 10f
            val x = (1 - t) * (1 - t) * a.x + 2 * (1 - t) * t * c.x + t * t * b.x
            val y = (1 - t) * (1 - t) * a.y + 2 * (1 - t) * t * c.y + t * t * b.y
            val flag = Path().apply { moveTo(x - 5, y); lineTo(x + 5, y); lineTo(x, y + 11); close() }
            drawPath(flag, flagColors[(i + k) % flagColors.size], alpha = f)
            drawPath(flag, INK, alpha = f, style = Stroke(.7f))
        }
    }
}

/** Paper grain over everything. */
private val grain: ImageBitmap by lazy {
    val n = 160
    val bmp = ImageBitmap(n, n)
    val r = Random(7)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bmp), Size(n.toFloat(), n.toFloat())) {
        repeat(900) {
            drawCircle(Color(0xFF5A4E40), .4f + r.nextFloat() * .9f, Offset(r.nextFloat() * n, r.nextFloat() * n), alpha = .05f + r.nextFloat() * .09f)
        }
        repeat(160) {
            drawCircle(Color.White, .5f + r.nextFloat() * .8f, Offset(r.nextFloat() * n, r.nextFloat() * n), alpha = .08f + r.nextFloat() * .1f)
        }
    }
    bmp
}
