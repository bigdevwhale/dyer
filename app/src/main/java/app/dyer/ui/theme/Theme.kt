package app.dyer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.dyer.R

/** A dyer's workshop: linen paper, ink lines, and the paints themselves. */
@Immutable
object C {
    val bg = Color(0xFFE6DFCF)
    val paper = Color(0xFFF5F0E4)
    val card = Color(0xFFFAF6EE)
    val white = Color(0xFFFFFDF8)
    val line = Color(0xFFD6CBB6)
    val lineDark = Color(0xFFB5A993)
    val ink = Color(0xFF2B241D)
    val ink2 = Color(0xFF5A4E40)
    val muted = Color(0xFF7B705F)
    val wine = Color(0xFF8B1E3F)
    val gold = Color(0xFFE7AD1C)
    val green = Color(0xFF5F8A37)
    val shelf = Color(0xFF8A6A48)
    val shelfDark = Color(0xFF6E5236)
    val wall = Color(0xFFEDE5D3)
    val scrim = Color(0x8C1E1812)
    val night = Color(0xFF1C1712)
    val nightText = Color(0xFFF3ECDC)
    val nightMuted = Color(0xFF9D907B)
}

/** Ruslan Display: lettering from old Russian signboards, for titles only. */
val Sign = FontFamily(Font(R.font.ruslan, FontWeight.Normal))
val Serif = FontFamily(
    Font(R.font.pt_serif_regular, FontWeight.Normal),
    Font(R.font.pt_serif_bold, FontWeight.Bold),
    Font(R.font.pt_serif_italic, FontWeight.Normal, FontStyle.Italic),
)
/** The labels on the jars, in the dyer's hand. */
val Hand = FontFamily(Font(R.font.caveat, FontWeight.Normal))

object T {
    val Title = TextStyle(fontFamily = Sign, fontSize = 30.sp, lineHeight = 34.sp, color = C.ink)
    val Heading = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 24.sp, color = C.ink)
    val Body = TextStyle(fontFamily = Serif, fontSize = 16.sp, lineHeight = 22.sp, color = C.ink)
    val BodyB = Body.copy(fontWeight = FontWeight.Bold)
    val Italic = Body.copy(fontStyle = FontStyle.Italic)
    val Small = TextStyle(fontFamily = Serif, fontSize = 13.5.sp, lineHeight = 18.sp, color = C.ink2)
    val Tiny = TextStyle(fontFamily = Serif, fontSize = 11.5.sp, lineHeight = 14.sp, color = C.muted)
    val Caps = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.sp, color = C.muted)
    val Button = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 16.5.sp, lineHeight = 20.sp)
    val Label = TextStyle(fontFamily = Hand, fontSize = 19.sp, lineHeight = 20.sp, color = C.ink)
    /** What the townsfolk say. */
    val Voice = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 21.sp, color = C.ink)
}

@Composable
fun DyerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = C.ink, onPrimary = C.paper, background = C.bg, surface = C.paper, onSurface = C.ink,
            onBackground = C.ink, outline = C.line, error = C.wine,
        ),
        typography = MaterialTheme.typography.copy(bodyLarge = T.Body, bodyMedium = T.Body, bodySmall = T.Small, labelLarge = T.Button, titleLarge = T.Title),
        content = content,
    )
}
