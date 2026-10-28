package app.dyer.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyer.domain.Pigment
import app.dyer.ui.theme.C
import app.dyer.ui.theme.T
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Linen paper: a few specks. */
fun Modifier.paper(color: Color = C.paper) = this.background(color).drawBehind {
    val r = Random(7)
    val n = (size.width * size.height / 9000f).toInt().coerceAtMost(500)
    repeat(n) {
        drawCircle(C.ink2.copy(alpha = .04f + r.nextFloat() * .05f), .6f + r.nextFloat() * 1.3f, Offset(r.nextFloat() * size.width, r.nextFloat() * size.height))
    }
}

enum class Kind { INK, GHOST, PAPER, WINE }

@Composable
fun Btn(text: String, modifier: Modifier = Modifier, kind: Kind = Kind.INK, enabled: Boolean = true, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val (bg, fg, edge) = when (kind) {
        Kind.INK -> Triple(C.ink, C.paper, C.ink)
        Kind.GHOST -> Triple(C.card, C.ink, C.line)
        Kind.PAPER -> Triple(C.paper, C.ink, C.paper)
        Kind.WINE -> Triple(C.wine, C.paper, C.wine)
    }
    Box(
        modifier.clip(shape).background(if (enabled) bg else C.lineDark).border(1.5.dp, if (enabled) edge else C.lineDark, shape)
            .clickable(enabled = enabled, onClick = onClick).height(52.dp).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Every button is one line and one height: a long label shrinks a little instead of wrapping.
        BasicText(
            text, style = T.Button.copy(color = if (enabled) fg else C.paper, textAlign = TextAlign.Center), maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = T.Button.fontSize, stepSize = .5.sp),
        )
    }
}

@Composable
fun Chip(text: String, on: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier.clip(shape).background(if (on) C.ink else C.card).border(1.5.dp, if (on) C.ink else C.line, shape)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, style = T.Body.copy(color = if (on) C.paper else C.ink, fontSize = 14.sp, lineHeight = 18.sp))
    }
}

/** A paper card. */
@Composable
fun Card(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier.fillMaxWidth().clip(shape).paper(C.card).border(1.dp, C.line, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 16.dp, vertical = 14.dp),
        content = content,
    )
}

@Composable
fun Caps(text: String, modifier: Modifier = Modifier) = Text(text.uppercase(), style = T.Caps, modifier = modifier.padding(top = 14.dp, bottom = 6.dp))

@Composable
fun Gap(h: Int = 10) = Spacer(Modifier.height(h.dp))

@Composable
fun Flow(modifier: Modifier = Modifier, gap: Dp = 6.dp, content: @Composable () -> Unit) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(gap), verticalArrangement = Arrangement.spacedBy(gap)) { content() }
}

@Composable
fun Field(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, style: TextStyle = T.Body, max: Int = 60, onDone: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(10.dp)
    Box(modifier.fillMaxWidth().clip(shape).background(C.white).border(1.5.dp, C.line, shape).padding(horizontal = 12.dp, vertical = 11.dp)) {
        if (value.isEmpty()) Text(hint, style = style.copy(color = C.muted))
        BasicTextField(
            value, { onChange(it.take(max)) }, Modifier.fillMaxWidth(), textStyle = style, singleLine = true, cursorBrush = SolidColor(C.wine),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default),
            keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        )
    }
}

/** The title of a tab. */
@Composable
fun TopBar(title: String, sub: String? = null, end: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 18.dp, end = 12.dp, top = 12.dp, bottom = 6.dp), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(title, style = T.Title)
            if (sub != null) Text(sub, style = T.Small.copy(color = C.muted))
        }
        end()
    }
}

/** A sheet of paper over the screen, with a title and a way back. */
@Composable
fun Sheet(title: String, onClose: () -> Unit, closeLabel: String, scroll: Boolean = true, foot: (@Composable ColumnScope.() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(C.scrim).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(top = 18.dp).clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)).paper()) {
            Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp).size(40.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(C.lineDark))
            Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = T.Heading.copy(fontSize = 21.sp), modifier = Modifier.weight(1f))
                Text(closeLabel, style = T.Small, modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onClose).padding(10.dp))
            }
            val inner = Modifier.fillMaxWidth().weight(1f).then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(horizontal = 18.dp, vertical = 6.dp)
            Column(inner, content = content)
            if (foot != null) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(C.line))
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), content = foot)
            }
        }
    }
}

/** A blob of paint. */
@Composable
fun Dot(color: Color, size: Dp = 14.dp, modifier: Modifier = Modifier, empty: Boolean = false) {
    Box(
        modifier.size(size).clip(CircleShape).background(if (empty) Color.Transparent else color)
            .border(1.2.dp, if (empty) C.lineDark else C.ink.copy(alpha = .8f), CircleShape),
    )
}

fun Pigment?.color(): Color = if (this == null) Color(0xFFF4F1EA) else Color(argb)

/** A plate seen from above, with [share] of it green. */
@Composable
fun PlateIcon(share: Float, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val r = this.size.minDimension / 2f
        drawCircle(Color.White, r)
        drawArc(C.green, -90f, 360f * share.coerceAtMost(.86f), true, Offset(center.x - r * .82f, center.y - r * .82f), Size(r * 1.64f, r * 1.64f))
        drawCircle(C.ink, r - .6.dp.toPx(), style = Stroke(1.2.dp.toPx()))
    }
}

/** Short messages at the bottom of the screen. */
class Toaster {
    var text by mutableStateOf<String?>(null)
    var seq by mutableIntStateOf(0)
    fun show(s: String) { text = s; seq++ }
}

val LocalToast = compositionLocalOf { Toaster() }

@Composable
fun BoxScope.ToastHost(t: Toaster) {
    LaunchedEffect(t.seq) { if (t.text != null) { delay(3200); t.text = null } }
    val s = t.text ?: return
    Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 18.dp, end = 18.dp, bottom = 84.dp)) {
        Text(s, style = T.Body.copy(color = C.paper, textAlign = TextAlign.Center), modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(C.ink).clickable { t.text = null }.padding(horizontal = 16.dp, vertical = 11.dp))
    }
}
