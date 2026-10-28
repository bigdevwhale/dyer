package app.dyer.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import app.dyer.domain.Kind
import app.dyer.domain.Plant
import app.dyer.ui.components.color

private val INK = Color(0xFF2B241D)
private val LID = Color(0xFF8A6A48)

/** A glass jar of paint; the more often a plant was eaten, the fuller it is. Fermented things bubble. */
@Composable
fun Jar(plant: Plant, times: Int, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val k = w / 54f
        val body = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(4 * k, 12 * k, w - 4 * k, h - 2 * k, CornerRadius(8 * k))) }
        drawPath(body, Color(0xFFFFFDF8))
        val level = if (times <= 0) 0f else (.32f + times * .07f).coerceAtMost(.9f)
        val fill = when {
            plant.kind == Kind.REFINED -> Color(0xFFEDE6D6)
            else -> plant.pigment.color()
        }
        clipPath(body) {
            val top = h - 2 * k - (h - 14 * k) * level
            drawRect(fill, Offset(0f, top), Size(w, h))
            drawLine(INK.copy(alpha = .25f), Offset(0f, top), Offset(w, top), 1.2f * k)
            if (plant.ferment) for (i in 0 until 6) drawCircle(Color.White.copy(alpha = .8f), (1.4f + i % 3) * k, Offset((12 + i * 6) * k, top + (8 + (i * 7) % 18) * k))
            if (plant.kind.drop) for (i in 0 until 3) drawCircle(fill, 2.5f * k, Offset((16 + i * 10) * k, top - 6 * k))
        }
        drawLine(Color.White.copy(alpha = .75f), Offset(10 * k, 18 * k), Offset(10 * k, h - 12 * k), 2.2f * k)
        drawPath(body, INK, style = Stroke(1.6f * k))
        drawRoundRect(LID, Offset(6 * k, 4 * k), Size(w - 12 * k, 9 * k), CornerRadius(2 * k))
        drawRoundRect(INK, Offset(6 * k, 4 * k), Size(w - 12 * k, 9 * k), CornerRadius(2 * k), style = Stroke(1.4f * k))
    }
}
