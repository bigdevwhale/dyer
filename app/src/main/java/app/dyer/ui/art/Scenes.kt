package app.dyer.ui.art

import androidx.compose.ui.graphics.Color
import app.dyer.domain.Quarter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** The four quarters, drawn in 400 × 600 units with the horizon a little above the middle. */
object Scenes {
    fun of(q: Quarter): SceneArt = when (q) {
        Quarter.MARKET -> market
        Quarter.MILL -> mill
        Quarter.HARBOUR -> harbour
        Quarter.CASTLE -> castle
    }

    private const val GLOW = 0xFFFFD77A
    private const val WIN = 0xFFF5CF62

    private fun cloud(x: Float, y: Float, k: Float = 1f) =
        "M$x ${y}q${8 * k}-${16 * k} ${26 * k}-${10 * k}q${10 * k}-${14 * k} ${28 * k}-${4 * k}q${16 * k}-${2 * k} ${16 * k} ${12 * k}q${8 * k} ${10 * k}-${6 * k} ${14 * k}h-${60 * k}q-${14 * k}-${2 * k}-${4 * k}-${12 * k}z"

    /** Stones on a square: little ellipses scattered over [rows] (y from, y to), skipping [gap] (x from, x to). */
    private fun stones(seed: Int, n: Int, x0: Float, x1: Float, y0: Float, y1: Float, gap: Pair<Float, Float>? = null): String {
        val r = Random(seed)
        val sb = StringBuilder()
        repeat(n) {
            val x = x0 + r.nextFloat() * (x1 - x0)
            val y = y0 + r.nextFloat() * (y1 - y0)
            if (gap != null && x in gap.first..gap.second) return@repeat
            val rx = 4 + r.nextFloat() * 5
            val ry = 2 + r.nextFloat() * 2
            sb.append("M${fmt(x - rx)} ${fmt(y)}a${fmt(rx)} ${fmt(ry)} 0 1 0 ${fmt(2 * rx)} 0a${fmt(rx)} ${fmt(ry)} 0 1 0 ${fmt(-2 * rx)} 0")
        }
        return sb.toString()
    }

    private fun window(x: Float, y: Float, w: Float, h: Float) = listOf(glow(x + w / 2, y + h / 2, 19, GLOW), R(x, y, w, h, WIN))

    // ———————————————————————————— Market Square ————————————————————————————

    private val market: SceneArt by lazy {
        val rose = (0 until 8).map { i ->
            val a0 = i * PI / 4
            val a1 = a0 + PI / 4
            val c = longArrayOf(0xFF6B3FA0, 0xFF2F5FAE, 0xFFB03A7A, 0xFF3B7BBF)[i % 4]
            P("M280 198L${fmt(280 + 20 * cos(a0).toFloat())} ${fmt(198 + 20 * sin(a0).toFloat())}A20 20 0 0 1 ${fmt(280 + 20 * cos(a1).toFloat())} ${fmt(198 + 20 * sin(a1).toFloat())}Z", c)
        } + O(280, 198, 6, 0xFFE2B13C)
        val wins = listOf(listOf(32, 222, 20, 24), listOf(84, 222, 20, 24), listOf(32, 270, 20, 24), listOf(140, 245, 20, 22), listOf(190, 245, 20, 22), listOf(142, 290, 18, 22))
        val winShapes = wins.flatMap { (x, y, w, h) -> window(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat()) } +
            listOf(glow(252, 280, 20, GLOW), glow(308, 280, 20, GLOW), P("M246 300V262Q252 250 258 262V300Z", WIN), P("M302 300V262Q308 250 314 262V300Z", WIN))
        val winInk = wins.joinToString("") { (x, y, w, h) -> "M${x + w / 2} ${y}V${y + h}M$x ${y + h / 2}H${x + w}" }
        val lanterns = listOf(listOf(149, 384, 14, 16), listOf(237, 384, 14, 16), listOf(174, 354, 8, 10), listOf(218, 354, 8, 10)).flatMap { (x, y, w, h) ->
            listOf(glow(x + w / 2f, y + h / 2f, w * 1.4f, 0xFFFFB35C, .4f), R(x, y, w, h, 0xFFF08A2A))
        }
        val awning = (0 until 6).flatMap { i ->
            val t0 = 150 + i * 100 / 6f
            val t1 = t0 + 100 / 6f
            val b0 = 142 + i * 116 / 6f
            val b1 = b0 + 116 / 6f
            val c = if (i % 2 == 1) 0xFFF7EAD0 else 0xFFEF8424
            listOf(P("M${fmt(t0)} 470L${fmt(t1)} 470L${fmt(b1)} 492L${fmt(b0)} 492Z", c), P("M${fmt(b0)} 492A${fmt((b1 - b0) / 2)} 8 0 0 0 ${fmt(b1)} 492Z", c))
        }
        val apples = listOf(42 to 460, 70 to 444, 86 to 474, 56 to 486, 34 to 480, 330 to 470, 352 to 456, 372 to 482, 344 to 494).map { (x, y) -> O(x, y, 4, 0xFFD03B2A) }
        SceneArt(
            listOf(
                T("sky", 200, 30, Sky(340, 0xFF2E356F, 0xFF6A5AA0, 0xFFD99AA6, 0xFFF2C49A),
                    *listOf(60 to 30, 130 to 55, 190 to 22, 340 to 40, 370 to 100, 100 to 80, 320 to 120).map { (x, y) -> Shape(oval(x.toFloat(), y.toFloat(), 1.4f), Color(0xFFFFF6D8), ink = false, wet = false, only = true) }.toTypedArray(),
                    lines = listOf(L("M150 150l6 4 6-4M172 138l5 3 5-3"))),
                T("clouds", 70, 110, P(cloud(40f, 110f), 0xFFF4E1E6, wet = false), P(cloud(250f, 74f, .93f), 0xFFF4E1E6, wet = false)),
                T("hills", 360, 230, P("M0 236C50 214 110 220 160 232C220 246 300 206 400 222V300H0Z", 0xFF7287BA)),
                T("meadow", 40, 260, P("M0 250C60 228 120 236 170 248C220 260 300 226 400 240V335H0Z", 0xFF7AA043), P("M0 274C80 258 140 266 200 276C260 286 330 264 400 272V335H0Z", 0xFF5F8A37),
                    lines = listOf(L("M14 300l2-6 2 6M330 296l2-6 2 6M364 305l2-6 2 6M344 252l2-5 2 5"))),
                T("walls", 180, 260, R(20, 207, 96, 123, 0xFFE2C08A), R(124, 227, 102, 103, 0xFFD9A872), R(236, 152, 88, 178, 0xFFCBB38E),
                    R(82, 288, 18, 42, 0xFF7A4F2C), R(196, 290, 18, 40, 0xFF6E4A2C), P("M268 330V306Q280 292 292 306V330Z", 0xFF5E3F28),
                    lines = listOf(L("M236 230H324M236 316H324M28 318h12M54 306h10M108 252h-10M132 316h12M150 236h10", .8f, .55f))),
                T("roofs", 120, 170, R(92, 160, 11, 30, 0xFF9C4A35), P("M10 208L68 148L126 208Z", 0xFFC4472F), P("M116 228L175 176L234 228Z", 0xFFB8432D), P("M254 153L280 72L306 153Z", 0xFFA8392C),
                    lines = listOf(L("M30 190H106M48 172H88M136 212H214M154 196H196M264 128H296M271 106H289", .8f, .6f))),
                T("banners", 280, 175, P("M240 162H254V206L247 198L240 206Z", 0xFF8B1E3F), P("M306 162H320V206L313 198L306 206Z", 0xFF8B1E3F)),
                T("rose", 280, 198, *rose.toTypedArray()),
                T("windows", 20, 230, *winShapes.toTypedArray(), lines = listOf(L(winInk))),
                T("sun", 280, 34, P(starD(280f, 34f, 14f, 7f, 12), 0xFFE7AD1C), O(280, 34, 6, 0xFFF4C94A),
                    lines = listOf(L("M280 72V48M266 54H294M290 50L294 54L290 58", 1.2f))),
                T("river", 0, 410, P("M0 378C90 372 160 386 230 380C300 374 350 384 400 378V444C300 450 120 438 0 446Z", 0xFF4F9C9A),
                    P("M40 400q30-4 60 0q-30 5-60 0z", 0xFF9FD0C6, .8f, ink = false, wet = false), P("M270 420q34-4 70 0q-34 5-70 0z", 0xFF9FD0C6, .8f, ink = false, wet = false),
                    lines = listOf(L("M30 410q10-4 20 0q10 4 20 0M300 398q10-4 20 0q10 4 20 0M80 430q10-4 20 0q10 4 20 0M330 432q8-3 16 0", .9f))),
                T("cobbles", 200, 600, R(0, 330, 400, 50, 0xFFA98458), P("M0 446C120 438 300 450 400 444V600H0Z", 0xFF9C7A52), P("M176 380H224L248 446H152Z", 0xFFB89466),
                    lines = listOf(L(stones(11, 22, 0f, 400f, 336f, 376f, 170f to 230f), .7f, .45f), L(stones(12, 48, 0f, 400f, 452f, 596f), .7f, .45f), L("M180 392H220M168 412H232M160 432H240", .7f, .5f))),
                T("lanterns", 200, 370, *lanterns.toTypedArray(),
                    lines = listOf(L("M156 436V400M244 436V400M178 380V364M222 380V364M147 384L156 377L165 384M235 384L244 377L253 384", 1.2f))),
                T("trees", 60, 460, R(52, 494, 12, 46, 0xFF6F4A2C), R(340, 505, 12, 42, 0xFF6F4A2C),
                    P("M20 480C10 450 30 420 58 422C80 410 105 430 100 455C112 475 95 500 72 496C55 508 28 502 20 480Z", 0xFF5B8C3A),
                    P("M310 488C300 462 318 436 344 438C366 428 390 448 386 470C396 490 380 512 358 508C342 518 316 512 310 488Z", 0xFF4F7F34)),
                T("apples", 200, 470, *apples.toTypedArray()),
                T("awning", 200, 470, *awning.toTypedArray(), lines = listOf(L("M150 492V540M250 492V540", 1.4f))),
                T("produce", 200, 515, R(146, 512, 108, 28, 0xFF8A5A34), O(166, 508, 6, 0xFFE0792A), O(180, 509, 5, 0xFFE7AD1C), O(194, 508, 6, 0xFFF1C232),
                    O(214, 508, 6, 0xFF5F8A37), O(230, 509, 5, 0xFFE7AD1C), O(242, 508, 4, 0xFFC4472F)),
            ),
            listOf(
                DetailArt(
                    "cat", listOf(P("M35 246C34 240 35 235 37 232L36 226L39 229L45 229L48 226L47 232C49 235 50 240 49 246Z", 0xFFD98A3A, ink = false)),
                    listOf(L("M49 244C56 243 57 237 53 235", 3f)), listOf(Color(0xFFD98A3A)),
                    listOf(path("M35 246C34 240 35 235 37 232L36 226L39 229L45 229L48 226L47 232C49 235 50 240 49 246"), path("M49 244C56 243 57 237 53 235"), path("M40 233h1.5"), path("M44 233h1.5")),
                ),
                DetailArt(
                    "geranium", listOf(P("M88 246L90 238H98L100 246Z", 0xFFB8563A, ink = false), O(86.5f, 226, 2.6f, 0xFFD03B6A, ink = false), O(101.5f, 225, 2.6f, 0xFFD03B6A, ink = false), O(94, 227, 2.6f, 0xFFD03B6A, ink = false)),
                    listOf(L("M94 238C91 232 89 229 86.5 227M94 238C96 232 99 229 101.5 226M94 238V229.5", 2f)), listOf(Color(0xFF4F7F34)),
                    listOf(path("M88 246L90 238H98L100 246Z"), path("M94 238C91 232 89 229 86.5 227"), path("M94 238C96 232 99 229 101.5 226"), path("M94 238V229.5")),
                ),
                DetailArt(
                    "birdhouse", listOf(R(46, 446, 14, 16, 0xFF8A5A34, ink = false), P("M43 447L53 438L63 447Z", 0xFFB8432D, ink = false), O(53, 452, 2.5f, 0xFF2B241D, ink = false), P("M64 444q6-6 12-2q-2 5-8 5z", 0xFF3B3F6E, ink = false)),
                    listOf(), listOf(),
                    listOf(path("M46 446H60V462H46Z"), path("M43 447L53 438L63 447"), path("M53 462V472"), path("M64 444q6-6 12-2q-2 5-8 5z"), path("M75 442l4-1")),
                ),
            ),
            listOf(G(96, 214, 170, 250, 238, 186), G(324, 196, 362, 226, 400, 214), G(0, 318, 60, 350, 128, 320)),
        )
    }

    // ———————————————————————————— The Mill ————————————————————————————

    private fun blade(cx: Float, cy: Float, deg: Float): Pair<String, String> {
        val a = deg * PI.toFloat() / 180f
        val dx = cos(a)
        val dy = sin(a)
        val nx = -dy
        val ny = dx
        fun pt(r: Float, w: Float) = "${fmt(cx + dx * r + nx * w)} ${fmt(cy + dy * r + ny * w)}"
        val shape = "M${pt(10f, 0f)}L${pt(92f, 0f)}L${pt(92f, 18f)}L${pt(10f, 18f)}Z"
        val lattice = listOf(30f, 50f, 70f).joinToString("") { "M${pt(it, 0f)}L${pt(it, 18f)}" } + "M${pt(10f, 9f)}L${pt(92f, 9f)}"
        return shape to lattice
    }

    private val mill: SceneArt by lazy {
        val blades = listOf(28f, 118f, 208f, 298f).map { blade(295f, 272f, it) }
        val wheat = Random(5).let { r -> (0 until 46).joinToString("") { val x = r.nextFloat() * 396; val y = 370 + r.nextFloat() * 56; "M${fmt(x)} ${fmt(y)}l2-6 2 6" } }
        val leaves = listOf(470f to 18f, 500f to 24f, 530f to 30f).flatMap { (y, x0) -> (0 until 7).map { i -> O(x0 + 14 + i * 21, y + 6 - i * 1.7f, 5, 0xFF6FA043, wet = false) } }
        val poppies = Random(9).let { r -> (0 until 16).map { O(292 + r.nextFloat() * 100, if (it % 2 == 0) 466 + r.nextFloat() * 28 else 548 + r.nextFloat() * 44, 4, 0xFFD6342A) } }
        val sunflowers = listOf(196 to 394, 212 to 384, 228 to 396).flatMap { (x, y) -> listOf(P(starD(x.toFloat(), y.toFloat(), 10f, 6f, 12), 0xFFF1B81F), O(x, y, 4, 0xFF6E4A2C)) }
        val fence = (0 until 10).map { R(182 + it * 22, 436, 5, 26, 0xFF8A6A48) } + listOf(R(178, 442, 222, 3, 0xFF8A6A48), R(178, 453, 222, 3, 0xFF8A6A48))
        SceneArt(
            listOf(
                T("sky", 200, 40, Sky(372, 0xFF4F7FC0, 0xFF9CC3E6, 0xFFF3E3BD)),
                T("sun", 318, 92, P(starD(318f, 92f, 30f, 20f, 14), 0xFFE7AD1C), O(318, 92, 16, 0xFFF4C94A)),
                T("clouds", 60, 140, P(cloud(30f, 150f, 1.2f), 0xFFFBF3E6, wet = false), P(cloud(180f, 70f), 0xFFFBF3E6, wet = false)),
                T("hills", 380, 280, P("M0 300C60 270 130 276 190 292C250 306 320 262 400 278V372H0Z", 0xFF6F86B8)),
                T("meadow", 30, 340, P("M0 332C90 312 180 320 260 328C320 334 360 318 400 322V600H0Z", 0xFF7AA043),
                    lines = listOf(L("M20 470l2-6 2 6M230 520l2-6 2 6M380 470l2-6 2 6M210 590l2-6 2 6", 1f))),
                T("field", 10, 400, P("M0 362C120 352 260 354 400 364V432C260 422 120 426 0 434Z", 0xFFE3BE4F), lines = listOf(L(wheat, .8f, .55f))),
                T("mill", 295, 420, P("M262 432L274 280H316L328 432Z", 0xFFA58A6A), P("M286 432V412Q295 402 304 412V432Z", 0xFF6E4A2C), R(291, 312, 8, 10, 0xFF5E3F28),
                    lines = listOf(L("M268 340H322M265 384H325", .8f, .6f))),
                T("cap", 295, 270, P("M268 284Q295 240 322 284Z", 0xFFB8432D)),
                T("sails", 295, 272, *blades.map { P(it.first, 0xFFF4EDE0) }.toTypedArray(), O(295, 272, 6, 0xFF6E4A2C),
                    lines = listOf(L(blades.joinToString("") { it.second }, .7f, .6f))),
                T("hut", 115, 400, R(56, 372, 118, 64, 0xFFEFE6D2), R(74, 390, 18, 16, 0xFFF0D98C), R(136, 390, 18, 16, 0xFFF0D98C), R(104, 398, 18, 38, 0xFF8A5A34),
                    lines = listOf(L("M83 390V406M74 398H92M145 390V406M136 398H154", .8f))),
                T("thatch", 115, 330, P("M44 374L115 318L186 374Z", 0xFFD9B24C), lines = listOf(L("M62 362L115 326M84 368L115 334M146 368L115 334M168 362L115 326", .8f, .5f))),
                T("sunflowers", 212, 392, *sunflowers.toTypedArray(), lines = listOf(L("M196 432V404M212 432V394M228 432V406", 1.3f))),
                T("fence", 290, 450, *fence.toTypedArray()),
                T("path", 260, 600, P("M282 432H300C302 450 294 470 282 488C262 520 246 556 242 600H188C196 548 226 506 252 478C268 460 278 448 282 432Z", 0xFFB89466),
                    lines = listOf(L(stones(21, 14, 200f, 280f, 470f, 590f), .6f, .4f))),
                T("beds", 90, 500, P("M18 470L168 458L174 474L22 488Z", 0xFF4F7F34), P("M24 500L178 488L184 504L28 518Z", 0xFF4F7F34), P("M30 530L188 518L194 534L34 548Z", 0xFF4F7F34), *leaves.toTypedArray()),
                T("pumpkins", 80, 570, E(46, 570, 16, 11, 0xFFE0792A), E(86, 580, 14, 10, 0xFFE58A2E), E(124, 568, 15, 11, 0xFFE0792A),
                    lines = listOf(L("M46 559V581M86 570V590M124 557V579M46 559l3-5M86 570l3-5M124 557l3-5", .9f))),
                T("scarecrow", 150, 490, P("M138 490H162L158 528H142Z", 0xFFD9822B), O(150, 478, 8, 0xFFE8C06A), P("M138 472L150 456L162 472Z", 0xFF6E4A2C),
                    lines = listOf(L("M150 528V560M130 494H170", 1.6f))),
                T("pond", 330, 520, E(330, 520, 52, 18, 0xFF4F8FB0), lines = listOf(L("M300 518q8-3 16 0M340 526q8-3 16 0M326 512q6-2 12 0", .8f))),
                T("poppies", 340, 520, *poppies.toTypedArray()),
            ),
            listOf(
                DetailArt(
                    "geese", listOf(P("M262 504q6-8 18-4q6 2 4 8q-10 4-22-4z", 0xFFFBF8F0, ink = false), O(276, 488, 3, 0xFFFBF8F0, ink = false), P("M244 512q6-8 18-4q6 2 4 8q-10 4-22-4z", 0xFFFBF8F0, ink = false), O(258, 496, 3, 0xFFFBF8F0, ink = false)),
                    listOf(L("M278 486l5 1M260 494l5 1", 2f)), listOf(Color(0xFFEF8424)),
                    listOf(path("M262 504q6-8 18-4q6 2 4 8q-10 4-22-4z"), path("M278 502q2-10-2-14"), path("M244 512q6-8 18-4q6 2 4 8q-10 4-22-4z"), path("M260 510q2-10-2-14")),
                ),
                DetailArt(
                    "stork", listOf(P("M98 322q17 7 34 0q-17-5-34 0z", 0xFF8A6A48, ink = false), P("M106 316q8-14 20-6q2 6-6 8z", 0xFFFBF8F0, ink = false), P("M108 314q4 4 10 4q-4-6-10-4z", 0xFF2B241D, ink = false)),
                    listOf(L("M126 298l9 3", 1.8f)), listOf(Color(0xFFD6342A)),
                    listOf(path("M98 322q17 7 34 0q-17-5-34 0z"), path("M106 316q8-14 20-6q2 6-6 8z"), path("M124 310q4-6 2-12"), path("M126 298a2 2 0 1 0 0.1 0")),
                ),
                DetailArt(
                    "dog", listOf(P("M18 432q10-8 24-4q6 2 6 8q-14 4-30-4z", 0xFFB07A44, ink = false), O(46, 426, 5, 0xFFB07A44, ink = false), P("M44 422l-2-6 5 4z", 0xFF6E4A2C, ink = false)),
                    listOf(), listOf(),
                    listOf(path("M18 432q10-8 24-4q6 2 6 8q-14 4-30-4z"), path("M41 426a5 5 0 1 0 10 0a5 5 0 1 0 -10 0"), path("M18 430q-6-4-4-10"), path("M24 436V440M40 437V441")),
                ),
            ),
            listOf(G(56, 374, 115, 394, 174, 374), G(186, 372, 228, 352, 266, 344)),
        )
    }

    // ———————————————————————————— The Harbour ————————————————————————————

    private val harbour: SceneArt by lazy {
        val gull = { x: Int, y: Int, k: Float -> "M$x ${y}q${8 * k}-${8 * k} ${16 * k} 0q${8 * k}-${8 * k} ${16 * k} 0q-${8 * k}-${3 * k}-${16 * k} ${2 * k}q-${8 * k}-${5 * k}-${16 * k}-${2 * k}z" }
        val net = (0 until 9).joinToString("") { i -> "M${154 + i * 12} 520L${170 + i * 12} 494" } + (0 until 9).joinToString("") { i -> "M${154 + i * 12} 494L${170 + i * 12} 520" }
        val windows = listOf(12 to 404, 40 to 404, 74 to 404, 100 to 404).flatMap { (x, y) -> window(x.toFloat(), y.toFloat(), 14f, 16f) }
        SceneArt(
            listOf(
                T("sky", 200, 30, Sky(312, 0xFF39477E, 0xFF8A6FA6, 0xFFF0B48A)),
                T("gulls", 120, 120, P(gull(90, 118, 1f), 0xFFFBF8F0, wet = false), P(gull(150, 88, .8f), 0xFFFBF8F0, wet = false), P(gull(48, 160, .7f), 0xFFFBF8F0, wet = false), P(gull(230, 140, .6f), 0xFFFBF8F0, wet = false)),
                T("sea", 100, 300, R(0, 300, 400, 160, 0xFF3F6F9E),
                    lines = listOf(L("M20 330q10-4 20 0q10 4 20 0M120 318q10-4 20 0q10 4 20 0M150 420q10-4 20 0q10 4 20 0M60 380q8-3 16 0M200 360q8-3 16 0", .9f, .8f))),
                T("cliffs", 330, 300, P("M232 316C276 266 340 246 400 252V445H226Z", 0xFF6F9A4A), lines = listOf(L("M260 330l2-6 2 6M300 300l2-6 2 6M380 280l2-6 2 6M330 380l2-6 2 6", 1f))),
                T("tower", 350, 200, P("M332 252L340 150H360L368 252Z", 0xFFF6F1E6), lines = listOf(L("M336 150H364", 1.2f))),
                T("stripes", 350, 210, P("M337.8 178H362.2L363.8 198H336.2Z", 0xFFC4472F), P("M334.7 218H365.3L366.7 236H333.3Z", 0xFFC4472F)),
                T("lamp", 350, 139, glow(350, 139, 26, GLOW, .4f), R(342, 128, 16, 22, WIN), P("M338 128L350 116L362 128Z", 0xFF3B3F6E)),
                T("hulls", 160, 390, P("M130 372H226L214 392H142Z", 0xFF7A5233), P("M40 346H96L90 356H46Z", 0xFF6E4A2C)),
                T("sails", 180, 330, P("M180 370V292L222 370Z", 0xFFF6F1E6), P("M176 370V302L148 370Z", 0xFFF6F1E6), P("M66 344V308L46 344Z", 0xFFF6F1E6),
                    lines = listOf(L("M178 372V286M67 346V298", 1.3f))),
                T("redsail", 78, 320, P("M68 344V300L92 344Z", 0xFFC4472F)),
                T("buoys", 120, 400, P("M160 394L165 378L170 394Z", 0xFFEF7A24), O(165, 396, 6, 0xFFEF7A24), P("M196 432L200 420L204 432Z", 0xFFEF7A24), O(200, 434, 5, 0xFFEF7A24)),
                T("quay", 200, 600, R(0, 440, 260, 16, 0xFFA8845A), R(0, 456, 400, 144, 0xFF9C7A52),
                    lines = listOf(L("M40 440V456M80 440V456M120 440V456M160 440V456M200 440V456M240 440V456", .8f, .6f), L(stones(31, 50, 0f, 400f, 462f, 596f), .7f, .45f))),
                T("warehouse", 60, 420, R(0, 384, 124, 72, 0xFFD39A4F), R(48, 430, 16, 26, 0xFF6E4A2C), lines = listOf(L("M0 420H124", .7f, .5f))),
                T("roofs", 60, 360, P("M-4 386L30 346L64 386Z", 0xFFB8432D), P("M58 386L92 350L128 386Z", 0xFFA8392C)),
                T("windows", 60, 410, *windows.toTypedArray(), lines = listOf(L("M19 404V420M47 404V420M81 404V420M107 404V420", .8f))),
                T("flags", 252, 400, P("M252 394L280 400L252 408Z", 0xFFE7AD1C), P("M30 320L48 325L30 330Z", 0xFFE7AD1C), lines = listOf(L("M252 440V392M30 346V318", 1.3f))),
                T("nets", 206, 510, P("M150 520C170 486 240 486 262 520Z", 0xFF5F8A5A), lines = listOf(L(net, .6f, .5f))),
                T("barrels", 340, 500, R(300, 490, 24, 32, 0xFF8A5A34), R(328, 494, 24, 28, 0xFF7E5230), R(356, 490, 24, 32, 0xFF8A5A34),
                    lines = listOf(L("M300 498H324M300 514H324M328 502H352M328 514H352M356 498H380M356 514H380", .9f))),
            ),
            listOf(
                DetailArt(
                    "cat", listOf(P("M304 490C303 484 304 479 306 476L305 470L308 473L314 473L317 470L316 476C318 479 319 484 318 490Z", 0xFFD98A3A, ink = false)),
                    listOf(L("M318 488C325 487 326 481 322 479", 3f)), listOf(Color(0xFFD98A3A)),
                    listOf(path("M304 490C303 484 304 479 306 476L305 470L308 473L314 473L317 470L316 476C318 479 319 484 318 490"), path("M318 488C325 487 326 481 322 479"), path("M309 477h1.5"), path("M313 477h1.5")),
                ),
                DetailArt(
                    "fish", listOf(P("M214 470q-4 8 0 14q4-6 0-14z", 0xFFB9C4C8, ink = false), P("M234 471q-4 8 0 14q4-6 0-14z", 0xFFB9C4C8, ink = false), P("M254 472q-4 8 0 14q4-6 0-14z", 0xFFB9C4C8, ink = false), P("M274 473q-4 8 0 14q4-6 0-14z", 0xFFB9C4C8, ink = false)),
                    listOf(), listOf(),
                    listOf(path("M200 468L290 472"), path("M214 470q-4 8 0 14q4-6 0-14"), path("M234 471q-4 8 0 14q4-6 0-14"), path("M254 472q-4 8 0 14q4-6 0-14"), path("M274 473q-4 8 0 14q4-6 0-14")),
                ),
                DetailArt(
                    "crab", listOf(E(96, 552, 12, 7, 0xFFD6342A, ink = false), O(80, 542, 4, 0xFFD6342A, ink = false), O(112, 542, 4, 0xFFD6342A, ink = false)),
                    listOf(), listOf(),
                    listOf(path("M84 552a12 7 0 1 0 24 0a12 7 0 1 0 -24 0"), path("M86 548L80 542"), path("M106 548L112 542"), path("M86 556l-8 6M90 558l-6 8"), path("M106 556l8 6M102 558l6 8")),
                ),
            ),
            listOf(G(-4, 386, 60, 404, 128, 386), G(128, 386, 190, 414, 252, 396)),
        )
    }

    // ———————————————————————————— The Castle ————————————————————————————

    private val castle: SceneArt by lazy {
        val stars = listOf(40 to 40, 90 to 90, 150 to 30, 210 to 70, 250 to 20, 380 to 140, 30 to 150, 180 to 130, 300 to 160, 390 to 40).map { (x, y) ->
            P(starD(x.toFloat(), y.toFloat(), 5f, 1.8f, 4), 0xFFFFF3C8, wet = false)
        }
        val moon = Shape(androidx.compose.ui.graphics.Path().apply { op(oval(330f, 80f, 26f), oval(344f, 72f, 23f), androidx.compose.ui.graphics.PathOperation.Difference) }, Color(0xFFF5D46A))
        val firs = Random(3).let { r -> (0 until 15).joinToString("") { i -> val x = i * 28 - 6f; val h = 34 + r.nextFloat() * 18; "M${fmt(x)} 336L${fmt(x + 14)} ${fmt(300 - h + 30)}L${fmt(x + 28)} 336Z" } }
        val wins = listOf("M125 214V200Q130 192 135 200V214Z", "M125 268V254Q130 246 135 254V268Z", "M265 214V200Q270 192 275 200V214Z", "M265 268V254Q270 246 275 254V268Z", "M170 292V278Q176 270 182 278V292Z", "M218 292V278Q224 270 230 278V292Z")
        val winGlows = listOf(130 to 206, 130 to 260, 270 to 206, 270 to 260, 176 to 284, 224 to 284).map { (x, y) -> glow(x, y, 16, GLOW) }
        val roses = listOf(34 to 506, 52 to 498, 70 to 510, 46 to 524, 66 to 532, 100 to 552, 118 to 546, 126 to 562, 106 to 568).map { (x, y) -> O(x, y, 4.5f, 0xFFC4304A) }
        SceneArt(
            listOf(
                T("sky", 200, 20, Sky(342, 0xFF1F2A5A, 0xFF4A4F8F, 0xFFB48AA8)),
                T("stars", 200, 80, *stars.toTypedArray()),
                T("moon", 330, 80, moon),
                T("forest", 20, 320, P(firs, 0xFF3F6B3A)),
                T("hills", 380, 330, P("M0 318C70 296 150 306 210 322C280 340 340 300 400 308V600H0Z", 0xFF5F8A37),
                    lines = listOf(L("M30 420l2-6 2 6M370 400l2-6 2 6M330 560l2-6 2 6", 1f))),
                T("walls", 200, 280, R(108, 170, 44, 190, 0xFFB89C70), R(248, 170, 44, 190, 0xFFB89C70),
                    P("M148 360V198H160V206H172V198H184V206H196V198H208V206H220V198H232V206H244V198H252V360Z", 0xFFC2AA80),
                    lines = listOf(L("M108 230H152M108 300H152M248 230H292M248 300H292M148 250H252M148 330H170M230 330H252", .8f, .5f))),
                T("roofs", 200, 140, P("M102 172L130 104L158 172Z", 0xFF4B5F9A), P("M242 172L270 104L298 172Z", 0xFF4B5F9A)),
                T("flags", 200, 120, P("M130 82L150 88L130 94Z", 0xFFC4472F), P("M270 82L290 88L270 94Z", 0xFFC4472F), P("M200 172L222 178L200 186Z", 0xFFC4472F),
                    lines = listOf(L("M130 104V80M270 104V80M200 198V170", 1.3f))),
                T("banner", 200, 230, P("M184 222H216V262L200 252L184 262Z", 0xFFE7AD1C), lines = listOf(L("M193 238q7-8 14 0q-3 8-7 6q-4 2-7-6z", .9f))),
                T("windows", 130, 230, *winGlows.toTypedArray(), *wins.map { P(it, WIN) }.toTypedArray()),
                T("moat", 60, 370, P("M60 360C140 354 260 354 340 360V382C260 376 140 376 60 382Z", 0xFF4F9C9A), lines = listOf(L("M80 370q8-3 16 0M280 372q8-3 16 0", .8f))),
                T("gate", 200, 340, P("M182 360V328Q200 308 218 328V360Z", 0xFF6E4A2C), lines = listOf(L("M188 330V360M194 322V360M200 320V360M206 322V360M212 330V360M184 340H216M184 350H216", .8f, .8f))),
                T("path", 200, 600, P("M182 358H218L216 380H184Z", 0xFF9C7A52), P("M184 380H216L262 600H138Z", 0xFFB89466), lines = listOf(L(stones(41, 18, 160f, 240f, 400f, 590f), .6f, .4f))),
                T("torches", 200, 330, glow(170, 328, 13, 0xFFFFB35C, .45f), glow(230, 328, 13, 0xFFFFB35C, .45f), P("M170 336q-5-8 0-16q5 8 0 16z", 0xFFF08A2A), P("M230 336q-5-8 0-16q5 8 0 16z", 0xFFF08A2A),
                    lines = listOf(L("M170 336V350M230 336V350", 1.4f))),
                T("maple", 340, 470, R(334, 500, 12, 50, 0xFF6F4A2C), P("M300 480C290 450 312 424 340 426C366 418 390 440 384 466C394 488 372 510 348 504C330 514 304 504 300 480Z", 0xFFE0792A)),
                T("roses", 60, 530, P("M20 520C14 500 34 486 54 492C74 484 92 500 86 518C92 536 70 548 52 542C34 550 18 538 20 520Z", 0xFF3F6B3A),
                    P("M84 560C80 544 96 534 112 538C128 532 142 544 138 558C142 572 126 580 112 576C98 582 84 574 84 560Z", 0xFF3F6B3A), *roses.toTypedArray()),
            ),
            listOf(
                DetailArt(
                    "owl", listOf(P("M334 434q-4-16 6-20q10 4 6 20z", 0xFF8A6A48, ink = false), O(337, 420, 2.2f, 0xFFF5D46A, ink = false), O(343, 420, 2.2f, 0xFFF5D46A, ink = false)),
                    listOf(), listOf(),
                    listOf(path("M334 434q-4-16 6-20q10 4 6 20z"), path("M335 414l-2-5 4 3"), path("M345 414l2-5-4 3"), path("M339 424l1 2 1-2")),
                ),
                DetailArt(
                    "swan", listOf(P("M96 372q12-10 28-2q-8 6-28 2z", 0xFFFBF8F0, ink = false), O(118, 352, 3.2f, 0xFFFBF8F0, ink = false)),
                    listOf(L("M121 351l5 1", 2f)), listOf(Color(0xFFEF8424)),
                    listOf(path("M96 372q12-10 28-2q-8 6-28 2z"), path("M118 368q4-12 0-16")),
                ),
                DetailArt(
                    "fox", listOf(P("M268 556q12-10 28-4q6 2 2 8q-16 4-30-4z", 0xFFD9822B, ink = false), P("M268 554q-12-2-14 8q8 0 14-6z", 0xFFD9822B, ink = false), P("M294 548l4-8 2 9z", 0xFFD9822B, ink = false)),
                    listOf(), listOf(),
                    listOf(path("M268 556q12-10 28-4q6 2 2 8q-16 4-30-4z"), path("M268 554q-12-2-14 8q8 0 14-6z"), path("M294 548l4-8 2 9"), path("M274 560v6M290 561v6")),
                ),
            ),
            listOf(G(152, 226, 200, 248, 248, 226), G(152, 300, 200, 318, 248, 300)),
        )
    }
}
