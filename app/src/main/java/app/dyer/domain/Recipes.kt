package app.dyer.domain

import java.time.LocalDate
import kotlin.random.Random

/** A real kitchen experiment from the dyer's book; it opens once its plant is on the shelf. */
class Recipe(val plant: String, val ru: String, val en: String, val stepsRu: String, val stepsEn: String) {
    fun title(ru: Boolean) = if (ru) this.ru else en
    fun steps(ru: Boolean) = if (ru) stepsRu else stepsEn
}

object Recipes {
    val all = listOf(
        Recipe(
            "onion", "Яйца в луковой шелухе", "Eggs dyed with onion skins",
            "Собери шелуху от десятка луковиц, залей водой, проваривай полчаса и остуди. Опусти в отвар сырые яйца и вари минут десять. Чем дольше яйца лежат в отваре, тем темнее цвет: от золотистого до красно-бурого. Для узора прижми к яйцу листик петрушки и туго обмотай капроном.",
            "Collect the skins of ten onions, cover with water, simmer for half an hour and let cool. Put raw eggs in the liquid and boil for ten minutes. The longer they sit in it, the darker they get, from gold to red-brown. For a pattern, press a parsley leaf to the egg and wrap it tight in an old stocking.",
        ),
        Recipe(
            "redcabbage", "Капустный индикатор", "Red cabbage indicator",
            "Мелко нашинкуй четверть кочана краснокочанной капусты, залей кипятком и дай настояться полчаса. Процеди фиолетовый настой в три стакана. В первый капни уксуса — он покраснеет. Во второй насыпь щепотку соды — посинеет и позеленеет. Третий оставь как есть. Это настоящая химия: антоцианы меняют цвет от кислотности.",
            "Shred a quarter of a red cabbage, pour boiling water over it and leave for half an hour. Strain the purple liquid into three glasses. Add a drop of vinegar to the first: it turns red. Stir a pinch of baking soda into the second: it turns blue, then green. Leave the third as it is. Real chemistry: anthocyanins change colour with acidity.",
        ),
        Recipe(
            "turmeric", "Золотой рис", "Golden rice",
            "Добавь в воду для риса чайную ложку куркумы и щепотку соли — рис станет ярко-жёлтым. Той же куркумой можно покрасить хлопковый платок: подержи его в горячем отваре минут двадцать. Только помни, что на солнце такой цвет со временем бледнеет.",
            "Add a teaspoon of turmeric and a pinch of salt to the rice water, and the rice turns bright yellow. Turmeric dyes a cotton handkerchief too: steep it in the hot liquid for twenty minutes. The colour fades in sunlight over time.",
        ),
        Recipe(
            "beet", "Розовые блины", "Pink pancakes",
            "Натри сырую свёклу на мелкой тёрке и отожми сок. Добавь пару ложек сока в тесто для блинов или оладий — они выйдут розовыми, а вкуса свёклы почти не будет.",
            "Grate a raw beetroot finely and squeeze out the juice. Stir two spoonfuls into pancake batter: the pancakes come out pink, and you can hardly taste the beet.",
        ),
        Recipe(
            "spinach", "Зелёные блины", "Green pancakes",
            "Пробей горсть шпината в блендере с молоком и замеси на этом молоке тесто. Блины выйдут ярко-зелёными. Так же красят домашнюю пасту.",
            "Blend a handful of spinach with the milk and make the batter with it. The pancakes come out bright green. Homemade pasta is dyed the same way.",
        ),
        Recipe(
            "blueberry", "Черничные чернила", "Bilberry ink",
            "Раздави горсть черники через сито, добавь щепотку соли и каплю уксуса, чтобы цвет держался. Пиши пером или кистью — получится фиолетово-синий. На бумаге он со временем станет серо-лиловым.",
            "Press a handful of bilberries through a sieve, add a pinch of salt and a drop of vinegar to fix the colour. Write with a nib or a brush: a violet blue. On paper it slowly turns greyish lilac.",
        ),
        Recipe(
            "avocado", "Розовый платок", "A pink handkerchief",
            "Отмой косточки и кожуру трёх-четырёх авокадо, залей водой и держи на слабом огне час, не давая кипеть. Вода станет розово-бурой. Опусти в неё мокрый хлопковый или шёлковый платок и оставь на ночь. Цвет выйдет нежно-розовым — никто не догадается, что это авокадо.",
            "Wash the stones and skins of three or four avocados, cover with water and keep on a low heat for an hour without boiling. The water turns pinkish brown. Put a wet cotton or silk handkerchief in it overnight. It comes out a soft pink: nobody will guess avocado.",
        ),
        Recipe(
            "tea", "Старинная бумага", "Old paper",
            "Завари крепкий чёрный чай и остуди. Пройдись по листу кистью или опусти лист в чай на минуту и высуши. Бумага станет как из старого письма. Края можно аккуратно потрепать.",
            "Brew strong black tea and let it cool. Brush it over a sheet of paper, or dip the sheet for a minute and dry it. It looks like an old letter. Rough up the edges a little.",
        ),
        Recipe(
            "coffee", "Кофейная акварель", "Coffee watercolour",
            "Свари крепкий кофе и разлей по трём чашкам: крепкий, разбавленный вдвое и вчетверо. Рисуй кистью, как акварелью, — получится сепия в три тона.",
            "Make strong coffee and pour it into three cups: strong, half-and-half with water, and a quarter strength. Paint with it like watercolour: sepia in three shades.",
        ),
        Recipe(
            "walnut", "Ореховые чернила", "Walnut ink",
            "Залей горсть скорлупы грецких орехов водой и вари около часа, пока отвар не станет тёмно-коричневым. Процеди и добавь щепотку соли. Такими чернилами писали ещё в старину.",
            "Cover a handful of walnut shells with water and boil for about an hour until the liquid is dark brown. Strain and add a pinch of salt. People wrote with ink like this long ago.",
        ),
        Recipe(
            "saffron", "Шафрановая сдоба", "Saffron buns",
            "Растолки несколько нитей шафрана с ложкой сахара и залей двумя ложками тёплого молока. Добавь в тесто — булки станут золотыми и душистыми.",
            "Crush a few saffron threads with a spoonful of sugar and pour over two spoonfuls of warm milk. Add it to the dough: the buns turn golden and fragrant.",
        ),
    )

    fun of(plant: String) = all.firstOrNull { it.plant == plant }
}

/** A dish that comes with the app, so the first plates are one tap. */
class Preset(val ru: String, val en: String, val plants: List<String>, val plate: Float) {
    fun name(ru: Boolean) = if (ru) this.ru else en
}

object Presets {
    val all = listOf(
        Preset("Борщ", "Borscht", listOf("beet", "cabbage", "carrot", "potato", "onion", "tomato", "dill", "garlic"), 1f),
        Preset("Щи из квашеной капусты", "Sauerkraut soup", listOf("sauerkraut", "potato", "carrot", "onion", "bay", "dill"), 1f),
        Preset("Винегрет", "Vinegret salad", listOf("beet", "potato", "carrot", "pickles", "peas", "onion"), 1f),
        Preset("Овсянка с ягодами", "Porridge with berries", listOf("oats", "strawberry", "blueberry", "flax", "walnut"), .5f),
        Preset("Гречка с грибами", "Buckwheat with mushrooms", listOf("buckwheat", "champignon", "onion", "dill"), 1f),
        Preset("Греческий салат", "Greek salad", listOf("tomato", "cucumber", "pepper", "onion", "olive", "oregano"), 1f),
        Preset("Хумус с овощами", "Hummus and vegetables", listOf("chickpea", "sesame", "lemon", "garlic", "carrot", "cucumber"), 1f),
        Preset("Овощное рагу", "Vegetable stew", listOf("zucchini", "eggplant", "pepper", "tomato", "onion", "carrot", "basil"), 1f),
        Preset("Плов", "Plov", listOf("whiterice", "carrot", "onion", "garlic", "cumin"), .5f),
    )
}

/** The guild's order: a few plants the quarter would like this week. */
class Pick(val plant: Plant, val fresh: Boolean, val missing: Boolean)

object Guild {
    private val kinds = setOf(Kind.VEG, Kind.GREEN, Kind.FRUIT, Kind.BERRY, Kind.GRAIN, Kind.LEGUME, Kind.NUT, Kind.SEED, Kind.MUSHROOM)

    /** Three plants in season and not on this week's plates; never-eaten ones and missing colours first. */
    fun order(s: State, w: WeekPaint, today: LocalDate, n: Int = 3): List<Pick> {
        val r = Random(today.toEpochDay())
        val month = today.monthValue
        val scored = s.cat.plants.filter { !it.own && it.kind in kinds && it.inSeason(month) && it.id !in w.plants }.map { p ->
            val fresh = p.id !in s.jars
            val missing = p.pigment != null && p.pigment !in w.pigments
            Triple(p, (if (fresh) 3 else 0) + (if (missing) 2 else 0) + (if (p.seasonal) 1 else 0) + r.nextFloat(), fresh to missing)
        }.sortedByDescending { it.second }
        val out = mutableListOf<Pick>()
        val used = mutableSetOf<Pigment?>()
        for ((p, _, f) in scored) {
            if (out.size == n) break
            if (p.pigment in used && scored.size > n * 3) continue
            used += p.pigment
            out += Pick(p, f.first, f.second)
        }
        return out
    }
}
