package app.dyer.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * One filled piece of a thing in a quarter, in the scene's 400 × 600 units.
 * [ink] outlines it; [wet] gives it a darker watercolour edge; [only] pieces (glows, stars) exist only once painted.
 */
class Shape(
    val path: Path,
    val color: Color,
    val brush: Brush? = null,
    val alpha: Float = 1f,
    val ink: Boolean = true,
    val wet: Boolean = true,
    val only: Boolean = false,
)

/** An ink line drawn over a thing: tiles, planks, waves. */
class Line(val path: Path, val width: Float, val alpha: Float = 1f)

/** A thing in a quarter: the paint spreads over it from [origin]. */
class Thing(val id: String, val origin: Offset, val shapes: List<Shape>, val lines: List<Line> = emptyList()) {
    /** Everything the thing covers, glows included. */
    val area: Path = union(shapes.map { it.path })
    /** What the thing hides of things behind it. */
    val solid: Path = union(shapes.filter { !it.only }.map { it.path })
    /** How far the paint must spread from the origin to cover it all. */
    val reach: Float = area.getBounds().let { b ->
        listOf(b.topLeft, b.topRight, b.bottomLeft, b.bottomRight).maxOf { hypot(it.x - origin.x, it.y - origin.y) } * 1.12f + 12f
    }
}

/** A quiet-meal detail: ink [lines] drawn bit by bit, then [fills] washed in. Each line is a single stroke. */
class DetailArt(val id: String, val fills: List<Shape>, val strokes: List<Line>, val strokeColors: List<Color>, val lines: List<Path>)

/** A quarter's whole drawing, back to front. */
class SceneArt(val things: List<Thing>, val details: List<DetailArt>, val garlands: List<List<Offset>>) {
    /** What of each thing is still visible once everything in front of it is drawn. */
    val visible: Map<String, Path> by lazy {
        val out = HashMap<String, Path>()
        var front = Path()
        for (t in things.asReversed()) {
            out[t.id] = Path().apply { op(t.area, front, PathOperation.Difference) }
            front = Path().apply { op(front, t.solid, PathOperation.Union) }
        }
        out
    }

    fun thing(id: String) = things.first { it.id == id }
}

fun union(paths: List<Path>): Path {
    var acc = Path()
    for (p in paths) acc = Path().apply { op(acc, p, PathOperation.Union) }
    return acc
}

private val parser = PathParser()
fun path(d: String): Path = parser.parsePathString(d).toPath()
fun rect(x: Float, y: Float, w: Float, h: Float): Path = Path().apply { addRect(Rect(x, y, x + w, y + h)) }
fun oval(cx: Float, cy: Float, rx: Float, ry: Float = rx): Path = Path().apply { addOval(Rect(cx - rx, cy - ry, cx + rx, cy + ry)) }

fun starD(cx: Float, cy: Float, r1: Float, r2: Float, n: Int): String {
    val sb = StringBuilder()
    for (i in 0 until n * 2) {
        val a = PI * i / n - PI / 2
        val r = if (i % 2 == 1) r2 else r1
        sb.append(if (i == 0) 'M' else 'L').append(fmt(cx + r * cos(a).toFloat())).append(' ').append(fmt(cy + r * sin(a).toFloat()))
    }
    return sb.append('Z').toString()
}

fun fmt(f: Float) = "%.1f".format(java.util.Locale.ROOT, f)

/** A filled piece from SVG path data. */
fun P(d: String, c: Long, a: Float = 1f, ink: Boolean = true, wet: Boolean = true, only: Boolean = false) = Shape(path(d), Color(c), null, a, ink, wet, only)
fun R(x: Number, y: Number, w: Number, h: Number, c: Long, ink: Boolean = true, wet: Boolean = true) =
    Shape(rect(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat()), Color(c), null, 1f, ink, wet)
fun O(cx: Number, cy: Number, r: Number, c: Long, ink: Boolean = true, wet: Boolean = true) = Shape(oval(cx.toFloat(), cy.toFloat(), r.toFloat()), Color(c), null, 1f, ink, wet)
fun E(cx: Number, cy: Number, rx: Number, ry: Number, c: Long, ink: Boolean = true) = Shape(oval(cx.toFloat(), cy.toFloat(), rx.toFloat(), ry.toFloat()), Color(c), ink = ink)
fun glow(cx: Number, cy: Number, r: Number, c: Long, a: Float = .32f) = Shape(oval(cx.toFloat(), cy.toFloat(), r.toFloat()), Color(c), null, a, ink = false, wet = false, only = true)
fun Sky(h: Number, vararg stops: Long) = Shape(rect(0f, 0f, 400f, h.toFloat()), Color(stops[stops.size / 2]), Brush.verticalGradient(stops.map { Color(it) }, 0f, h.toFloat()), ink = false, wet = false)
fun L(d: String, w: Float = 1f, a: Float = 1f) = Line(path(d), w, a)
fun T(id: String, x: Number, y: Number, vararg s: Shape, lines: List<Line> = emptyList()) = Thing(id, Offset(x.toFloat(), y.toFloat()), s.toList(), lines)
fun G(vararg pts: Number) = pts.map { it.toFloat() }.chunked(2).map { Offset(it[0], it[1]) }
