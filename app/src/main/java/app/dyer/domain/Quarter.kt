package app.dyer.domain

import app.dyer.domain.Pigment.BLUE
import app.dyer.domain.Pigment.EARTH
import app.dyer.domain.Pigment.GREEN
import app.dyer.domain.Pigment.ORANGE
import app.dyer.domain.Pigment.RED
import app.dyer.domain.Pigment.WHITE
import app.dyer.domain.Pigment.YELLOW

/**
 * A thing in a quarter that waits for paint, and who lives by it. [need] is how many full washes it takes;
 * the first line is said when paint first touches it, the second when it is done.
 */
class Slot(
    val id: String,
    val pigment: Pigment,
    val need: Float,
    val whoRu: String, val whoEn: String,
    val firstRu: String, val firstEn: String,
    val fullRu: String, val fullEn: String,
) {
    fun who(ru: Boolean) = if (ru) whoRu else whoEn
    fun first(ru: Boolean) = if (ru) firstRu else firstEn
    fun full(ru: Boolean) = if (ru) fullRu else fullEn
}

/** A small thing the brush draws during quiet meals. */
class Detail(val id: String, val ru: String, val en: String, val doneRu: String, val doneEn: String) {
    fun name(ru: Boolean) = if (ru) this.ru else en
    fun done(ru: Boolean) = if (ru) doneRu else doneEn
}

private fun s(id: String, p: Pigment, need: Float, who: String, whoEn: String, a: String, aEn: String, b: String, bEn: String) =
    Slot(id, p, need, who, whoEn, a, aEn, b, bEn)

/** The kingdom's quarters. Each week the Gloom settles on the next one, and the week's plants paint it back. */
enum class Quarter(val ru: String, val en: String, val slots: List<Slot>, val details: List<Detail>) {
    MARKET(
        "Рыночная площадь", "Market Square",
        listOf(
            s("sky", BLUE, 2f, "Звездочёт", "The stargazer", "Вечер вернулся. Опять видно, где кончается день.", "Evening is back. You can see where the day ends again.", "Небо синее до самого верха — ночью будут звёзды.", "Blue right to the top. There’ll be stars tonight."),
            s("clouds", WHITE, 1f, "Прачка", "The washerwoman", "Облака снова белые, как простыни после стирки.", "The clouds are white again, like sheets off the line.", "Ни пятнышка. Хоть бельё на них развешивай.", "Not a spot on them. You could hang washing on them."),
            s("hills", BLUE, 1f, "Почтальон", "The postman", "Дальние холмы проступили — теперь видно, куда нести письма.", "The far hills are showing. Now I can see where the letters go.", "Холмы синие-синие, как чернила у меня в сумке.", "The hills are as blue as the ink in my bag."),
            s("meadow", GREEN, 2f, "Пастушка", "The shepherdess", "Овцы снова отличают траву от камней.", "The sheep can tell grass from stones again.", "Луг зелёный до самой реки. Овцы довольны, и я тоже.", "Green all the way to the river. The sheep are happy, and so am I."),
            s("walls", EARTH, 2f, "Каменщик", "The mason", "Стены потеплели. На вид — и как будто на ощупь.", "The walls look warmer. They almost feel warmer too.", "Каждый камень на месте и своего цвета.", "Every stone in place and its own colour."),
            s("roofs", RED, 2f, "Кровельщик", "The roofer", "Черепица снова рыжая. А я уж думал, крыши из пепла.", "The tiles are red again. I thought the roofs were made of ash.", "Все крыши горят. С колокольни глядеть одно удовольствие.", "Every roof aglow. What a view from the bell tower."),
            s("banners", RED, 1f, "Герольд", "The herald", "Знамёна снова бордовые, как при старом короле.", "The banners are crimson again, as in the old king’s day.", "Знамёна полощутся на ветру во всей красе.", "The banners fly in all their glory."),
            s("rose", BLUE, 1f, "Стеклодув", "The glassblower", "Витраж горит! Синий, лиловый, малиновый.", "The rose window glows! Blue, violet, crimson.", "Солнце пройдёт сквозь витраж — и на полу собора будет радуга.", "When the sun comes through, there’ll be a rainbow on the cathedral floor."),
            s("windows", WHITE, 1.5f, "Ночной сторож", "The night watchman", "В окнах свет. Значит, дома кто-то есть.", "Lights in the windows. Someone’s home.", "Все окна светятся. Обход сегодня будет весёлый.", "Every window lit. A cheerful round tonight."),
            s("sun", YELLOW, 1f, "Звонарь", "The bell-ringer", "Солнце на шпиле! Как блестит!", "The sun on the spire! How it shines!", "Флюгер сияет — его видно с любой улицы.", "The weathervane blazes. You can see it from every street."),
            s("river", GREEN, 1.5f, "Лодочник", "The boatman", "Вода опять похожа на воду.", "The water looks like water again.", "Река зелёная и прозрачная — видно рыбу.", "Green and clear. I can see the fish."),
            s("cobbles", EARTH, 2f, "Мостовщик", "The paver", "Под ногами снова дорога, а не туман.", "There’s a road underfoot again, not fog.", "Вся площадь в цвете, камешек к камешку.", "The whole square in colour, stone by stone."),
            s("lanterns", ORANGE, 1f, "Фонарщик", "The lamplighter", "Фонари на мосту горят. Можно возвращаться домой затемно.", "The bridge lanterns are lit. You can walk home after dark.", "Все четыре фонаря горят — мост как в праздник.", "All four lanterns lit. The bridge looks like a holiday."),
            s("trees", GREEN, 1f, "Садовница", "The gardener", "Яблони зазеленели. Я все глаза проглядела, пока ждала.", "The apple trees are green again. I watched and waited so long.", "Кроны густые, как в мае.", "The crowns are as thick as in May."),
            s("apples", RED, 1f, "Ребятня", "The children", "Яблоки покраснели! Чур, моё вон то, самое высокое.", "The apples are red! Bags I the top one.", "Все яблоки красные. Мы только посмотреть, честно!", "Every apple red. We’re only looking, honest!"),
            s("awning", ORANGE, 1f, "Торговка", "The stallholder", "Полосатый навес! Теперь меня с того берега видно.", "A striped awning! They’ll see me from across the river.", "Навес как новенький. Подходите, не стесняйтесь!", "Good as new. Step up, don’t be shy!"),
            s("produce", YELLOW, 1f, "Зеленщица", "The greengrocer", "На прилавке снова видно, где лимон, а где картошка.", "You can tell the lemons from the potatoes again.", "Прилавок пестрит, как клумба.", "The stall is as bright as a flower bed."),
        ),
        listOf(
            Detail("cat", "Кошка в окне", "A cat in the window", "В окне теперь сидит рыжая кошка. Говорят, она приходит только к тем, кто ест не спеша.", "A ginger cat now sits in the window. They say she only visits people who eat slowly."),
            Detail("geranium", "Герань на окне", "A geranium on the sill", "На соседнем окне зацвела герань. Хозяйка уверяет, что поливала её молча.", "A geranium has flowered on the next sill. Its owner swears she watered it in silence."),
            Detail("birdhouse", "Скворечник на яблоне", "A birdhouse in the apple tree", "На яблоню повесили скворечник. Скворцы селятся там, где никто не суетится.", "Someone has hung a birdhouse in the apple tree. Starlings settle where nobody fusses."),
        ),
    ),
    MILL(
        "Мельница", "The Mill",
        listOf(
            s("sky", BLUE, 2f, "Пасечник", "The beekeeper", "Небо поголубело — пчёлы полетели за мёдом.", "The sky is blue again. The bees are off for nectar.", "Чисто до самого горизонта. Лётная погода для пчёл.", "Clear to the horizon. Flying weather for bees."),
            s("sun", YELLOW, 1f, "Петух", "The rooster", "Ку-ка-ре-ку! Солнце вернулось — есть о чём покричать.", "Cock-a-doodle-doo! The sun’s back. Now there’s something to crow about.", "Солнце во всё небо! Буду кричать с утра до вечера.", "Sun all over the sky! I’ll crow from dawn to dusk."),
            s("clouds", WHITE, 1f, "Ветер", "The wind", "Облака снова белые — гонять их одно удовольствие.", "The clouds are white again. Chasing them is a joy.", "Белые, пушистые — как овцы на небе.", "White and fluffy, like sheep in the sky."),
            s("hills", BLUE, 1f, "Странник", "The wanderer", "Дальние холмы посинели — значит, дорога ещё длинная.", "The far hills have turned blue. The road is long yet.", "Холмы синие, как в песне. Пора идти дальше.", "Blue as in a song. Time to walk on."),
            s("meadow", GREEN, 2f, "Пастушок", "The cowherd", "Трава позеленела, коровы перестали вздыхать.", "The grass is green. The cows have stopped sighing.", "Луг зелёный-презелёный, коровы жуют и не глядят на меня.", "Green as can be. The cows chew and don’t even look at me."),
            s("field", YELLOW, 2f, "Жнея", "The reaper", "Пшеница опять золотая! А то жали серое, как старую солому.", "The wheat is golden again! We were reaping grey stuff like old straw.", "Поле золотое до самой мельницы. Хлеб будет.", "Gold right up to the mill. There’ll be bread."),
            s("mill", EARTH, 2f, "Мельник", "The miller", "Мельница потемнела, как настоящее старое дерево. Красота!", "The mill has darkened like real old timber. Lovely!", "Каждая доска своего цвета. Мельница довольна, и я тоже.", "Every plank its own colour. The mill is pleased, and so am I."),
            s("cap", RED, 1f, "Плотник", "The carpenter", "Шапка у мельницы красная, как у мухомора. Видно за версту!", "The mill’s cap is red as a toadstool. Seen from a mile off!", "Покрашено на совесть, ни одной серой дранки.", "Painted properly, not one grey shingle."),
            s("sails", WHITE, 1f, "Подмастерье", "The apprentice", "Крылья побелели — и будто легче стали крутиться.", "The sails are white again, and they seem to turn more easily.", "Крылья белые-белые, как паруса.", "White as a ship’s sails."),
            s("hut", WHITE, 1f, "Хозяйка", "The farmwife", "Хата снова белёная. Будто весной побелили.", "The cottage is whitewashed again, as if done this spring.", "Хата сияет. Хоть гостей зови.", "The cottage shines. Time to invite guests."),
            s("thatch", YELLOW, 1f, "Дед Матвей", "Grandad Matvey", "Крыша соломенная, как в моей молодости.", "A straw roof, like when I was young.", "Солома золотая — ни дождь, ни Хмарь ей не страшны.", "Golden straw. Neither rain nor Gloom can harm it."),
            s("sunflowers", YELLOW, 1f, "Ребятишки", "The children", "Подсолнухи повернулись к солнцу! И стали жёлтыми!", "The sunflowers turned to the sun! And they’re yellow!", "Семечек будет — на всю зиму!", "Seeds enough for all winter!"),
            s("fence", EARTH, 1f, "Плетельщик", "The fence-weaver", "Плетень потемнел, как ему и положено.", "The wattle fence has darkened, as it should.", "Каждый прутик на месте и в цвете.", "Every twig in place, and in colour."),
            s("path", EARTH, 1.5f, "Возчик", "The carter", "Дорога снова дорога — колёса не вязнут в сером.", "The road’s a road again. The wheels don’t sink in the grey.", "До самой мельницы гладко, как по скатерти.", "Smooth as a tablecloth all the way to the mill."),
            s("beds", GREEN, 1.5f, "Огородница", "The vegetable gardener", "Грядки ожили — видно, где морковь, а где сорняк.", "The beds are alive. I can tell carrots from weeds.", "Все грядки зелёные. Теперь только поливать.", "Every bed green. Now just water."),
            s("pumpkins", ORANGE, 1f, "Огородник", "The grower", "Тыквы порыжели. К осени будут с бочку.", "The pumpkins have turned orange. By autumn they’ll be barrel-sized.", "Тыквы горят, как фонари. Одна точно станет каретой.", "Glowing like lanterns. One of them will surely become a coach."),
            s("scarecrow", ORANGE, 1f, "Пугало", "The scarecrow", "Ой, у меня снова рыжий кафтан! Вороны, бойтесь.", "Oh, my orange coat is back! Beware, crows.", "Я самое нарядное пугало в округе.", "I’m the smartest scarecrow around."),
            s("pond", BLUE, 1f, "Гусятница", "The goose girl", "Пруд посинел — гуси сами в воду пошли.", "The pond is blue. The geese went in by themselves.", "Пруд как небо, только мокрое.", "The pond is like the sky, only wet."),
            s("poppies", RED, 1f, "Девчата", "The girls", "Маки в поле! Сплетём венки.", "Poppies in the field! Let’s make wreaths.", "Маков столько, что поле как в веснушках.", "So many poppies the field looks freckled."),
        ),
        listOf(
            Detail("geese", "Гуси у пруда", "Geese by the pond", "К пруду вышли гуси. Гусятница говорит, что они чуют спокойных людей.", "Geese have come down to the pond. The goose girl says they can sense calm people."),
            Detail("stork", "Аист на крыше", "A stork on the roof", "На хату прилетел аист и свил гнездо. Это к добру, говорит хозяйка.", "A stork has landed on the cottage and built a nest. A good sign, says the farmwife."),
            Detail("dog", "Пёс у плетня", "A dog by the fence", "У плетня улёгся пёс. Он никуда не торопится, как и ты сегодня.", "A dog has lain down by the fence. He’s in no hurry, just like you today."),
        ),
    ),
    HARBOUR(
        "Гавань", "The Harbour",
        listOf(
            s("sky", BLUE, 2f, "Штурман", "The navigator", "Небо вернулось — снова можно идти по звёздам.", "The sky is back. We can steer by the stars again.", "Закат во всё небо. Завтра ветер будет попутный.", "Sunset across the whole sky. A fair wind tomorrow."),
            s("gulls", WHITE, 1f, "Чайки", "The gulls", "Кри-и! Перья снова белые!", "Kree! Our feathers are white again!", "Кри-и-и! Белее нас только пена!", "Kree-ee! Only the foam is whiter than us!"),
            s("sea", BLUE, 2f, "Рыбак", "The fisherman", "Море синее. Рыба вернулась к берегу.", "The sea is blue. The fish have come back inshore.", "Синее до горизонта. Улов будет хороший.", "Blue to the horizon. It’ll be a good catch."),
            s("cliffs", GREEN, 1.5f, "Козопас", "The goatherd", "Мыс зазеленел, козы полезли наверх.", "The headland is green. The goats are climbing up.", "Весь мыс зелёный — козы добрались до самого маяка.", "All green. The goats have reached the lighthouse."),
            s("tower", WHITE, 1f, "Смотритель маяка", "The lighthouse keeper", "Маяк снова белый, с моря его видно.", "The lighthouse is white again. Ships can see it.", "Белый, как сахар. Хоть сейчас на открытку.", "White as sugar. Fit for a postcard."),
            s("stripes", RED, 1f, "Дочка смотрителя", "The keeper’s daughter", "Полоски красные! Теперь наш маяк ни с каким не спутаешь.", "Red stripes! No one will mix up our lighthouse now.", "Полоски как на леденце.", "Stripes like a candy cane."),
            s("lamp", YELLOW, 1f, "Лоцман", "The pilot", "Огонь маяка снова жёлтый — суда пойдут в гавань.", "The light is yellow again. Ships will come into harbour.", "Маяк светит на десять миль.", "The light reaches ten miles out."),
            s("hulls", EARTH, 1f, "Корабел", "The shipwright", "Лодки снова дубовые, а не пепельные.", "The boats look like oak again, not ash.", "Борта просмолены и блестят.", "Tarred hulls, gleaming."),
            s("sails", WHITE, 1.5f, "Парусный мастер", "The sailmaker", "Паруса белые — не стыдно выйти в море.", "White sails. No shame in putting to sea.", "Паруса как облака на привязи.", "Sails like clouds on a leash."),
            s("redsail", RED, 1f, "Капитан", "The captain", "Алый парус! Где-то на берегу его ждут.", "A scarlet sail! Someone ashore is waiting for it.", "Парус горит — его видно с любого причала.", "The sail blazes. You can see it from every pier."),
            s("buoys", ORANGE, 1f, "Бакенщик", "The buoy keeper", "Бакены рыжие — фарватер снова видно.", "The buoys are orange. The channel shows again.", "Все бакены на месте и в цвете.", "Every buoy in place and in colour."),
            s("quay", EARTH, 2f, "Грузчик", "The docker", "Причал снова деревянный, по нему приятно ходить.", "The pier is timber again. It’s nice to walk on.", "Причал как новый, каждая доска своего цвета.", "Like new, every plank its own colour."),
            s("warehouse", ORANGE, 1.5f, "Купец", "The merchant", "Склады охристые, как в старые добрые времена.", "The warehouses are ochre, like the good old days.", "Склады полны цвета — торговля пойдёт.", "Full of colour. Trade will pick up."),
            s("roofs", RED, 1f, "Трубочист", "The chimney sweep", "Крыши снова красные — по ним и ходить веселее.", "Red roofs again. Even walking on them is more fun.", "Все крыши в цвете, ни одной серой черепички.", "Every roof coloured, not one grey tile."),
            s("windows", WHITE, 1f, "Трактирщица", "The innkeeper", "В окнах трактира свет — заходите погреться.", "Lights in the inn. Come in and get warm.", "Все окна горят, в трактире полно гостей.", "Every window lit. The inn is full."),
            s("flags", YELLOW, 1f, "Таможенник", "The customs officer", "Флаги жёлтые — гавань открыта.", "Yellow flags. The harbour is open.", "Флаги вьются, гавань празднует.", "The flags fly. The harbour celebrates."),
            s("nets", GREEN, 1f, "Рыбачка", "The fisherwoman", "Сети позеленели, как водоросли. Рыба их не заметит.", "The nets are green as seaweed. The fish won’t notice them.", "Цветные сети чинить одно удовольствие.", "Mending nets is a joy when they’re in colour."),
            s("barrels", EARTH, 1f, "Бондарь", "The cooper", "Бочки снова дубовые, с обручами.", "The barrels are oak again, hoops and all.", "Все бочки в цвете — хоть селёдку соли.", "Every barrel coloured. Ready for herring."),
        ),
        listOf(
            Detail("cat", "Кот на бочке", "A cat on a barrel", "На бочке устроился портовый кот. Он ждёт улова и никуда не спешит.", "A harbour cat has settled on a barrel. He waits for the catch and is in no hurry."),
            Detail("fish", "Рыба на верёвке", "Fish on a line", "Рыбачка вывесила рыбу вялиться. Значит, улов был.", "The fisherwoman has hung fish out to dry. So the catch was good."),
            Detail("crab", "Краб на причале", "A crab on the quay", "На причал выбрался краб и ходит боком. Ему тоже некуда торопиться.", "A crab has climbed onto the quay and walks sideways. He’s in no hurry either."),
        ),
    ),
    CASTLE(
        "Замок", "The Castle",
        listOf(
            s("sky", BLUE, 2f, "Астролог", "The astrologer", "Ночное небо вернулось. Звёзды снова на своих местах.", "The night sky is back. The stars are in their places.", "Небо тёмно-синее, бархатное. Королева будет довольна.", "Deep velvet blue. The queen will be pleased."),
            s("stars", WHITE, 1f, "Фрейлина", "The lady-in-waiting", "Звёзды засветились — можно загадывать желания.", "The stars are shining. Time to make wishes.", "Все звёзды на месте. Я пересчитала.", "Every star in place. I counted."),
            s("moon", YELLOW, 1f, "Сова", "The owl", "У-у! Луна снова жёлтая. Охота будет хорошей.", "Hoo! The moon is yellow again. Good hunting tonight.", "Луна как сыр. У-у-у.", "A moon like cheese. Hoo-oo."),
            s("forest", GREEN, 1.5f, "Лесничий", "The forester", "Ельник потемнел, как настоящий бор.", "The fir wood has darkened like a real forest.", "Лес стоит стеной — зелёный, густой.", "A wall of trees, green and dense."),
            s("hills", GREEN, 2f, "Егерь", "The gamekeeper", "Холмы позеленели. Олени вышли пастись.", "The hills are green. The deer have come out to graze.", "Зелено до самого горизонта.", "Green to the horizon."),
            s("walls", EARTH, 2f, "Зодчий", "The architect", "Камень снова тёплый, медовый.", "The stone is warm again, like honey.", "Замок весь в цвете — как на старой гравюре, только ярче.", "The whole castle in colour, like an old print but brighter."),
            s("roofs", BLUE, 1f, "Кровельщик", "The slater", "Шпили синие — сланец как сланец.", "The spires are blue. Slate looks like slate.", "Синие шпили видно из-за холмов.", "You can see the blue spires from beyond the hills."),
            s("flags", RED, 1f, "Знаменосец", "The standard-bearer", "Флаги алые! Королевство на месте.", "Scarlet flags! The kingdom is still here.", "Все флаги реют, как в день коронации.", "Every flag flying, as on coronation day."),
            s("banner", YELLOW, 1f, "Геральдист", "The herald", "Золотой лев на знамени снова золотой.", "The golden lion on the banner is golden again.", "Лев сияет — гордый, как и положено льву.", "The lion shines, proud as a lion should be."),
            s("windows", WHITE, 1.5f, "Камергер", "The chamberlain", "В окнах свечи. Во дворце кто-то не спит.", "Candles in the windows. Someone in the palace is awake.", "Все окна горят — будет бал.", "Every window lit. There will be a ball."),
            s("moat", GREEN, 1f, "Водяной", "The water sprite", "Ров позеленел. Я тут живу, мне нравится.", "The moat is green. I live here, and I like it.", "Ров как изумруд. Теперь бы ещё кувшинки.", "An emerald moat. Water lilies next."),
            s("gate", EARTH, 1f, "Привратник", "The gatekeeper", "Ворота снова дубовые. Открывать не стыдно.", "The gate is oak again. No shame opening it.", "Ворота блестят — хоть самого короля встречай.", "Gleaming. Fit to greet the king himself."),
            s("path", EARTH, 1.5f, "Гонец", "The messenger", "Дорога к замку видна — донесу весть быстрее.", "The road to the castle shows. I’ll get the news there faster.", "Дорога как лента — бегом до ворот.", "Like a ribbon. I’ll run to the gate."),
            s("torches", ORANGE, 1f, "Факельщик", "The torchbearer", "Факелы горят у ворот. Хмари сюда хода нет.", "Torches burn at the gate. The Gloom can’t get in.", "Оба факела пылают.", "Both torches blazing."),
            s("maple", ORANGE, 1f, "Дровосек", "The woodcutter", "Клён рыжий, как лиса. Рубить жалко.", "The maple is red as a fox. Too fine to fell.", "Клён горит, как костёр, только не жжётся.", "The maple flames like a bonfire, but doesn’t burn."),
            s("roses", RED, 1f, "Королевская садовница", "The royal gardener", "Розы раскрылись! Королева велела срезать к завтраку.", "The roses have opened! The queen wants some for breakfast.", "Весь розарий в цвету.", "The whole rose garden in bloom."),
        ),
        listOf(
            Detail("owl", "Сова на клёне", "An owl in the maple", "На клёне устроилась сова. Она смотрит на замок и молчит, как ты за едой.", "An owl has settled in the maple. She watches the castle in silence, as you ate in silence."),
            Detail("swan", "Лебедь во рву", "A swan in the moat", "По рву поплыл лебедь. Водяной говорит, что лебеди любят тишину.", "A swan is gliding along the moat. The water sprite says swans love quiet."),
            Detail("fox", "Лиса у дороги", "A fox by the road", "У дороги присела лиса. Ей тоже некуда торопиться.", "A fox sits by the road. She’s in no hurry either."),
        ),
    );

    fun name(ru: Boolean) = if (ru) this.ru else en
    fun slot(id: String) = slots.first { it.id == id }

    companion object {
        /** The quarter a week paints: they come round in turn, from the week someone started. */
        fun of(week: Long, firstWeek: Long): Quarter = entries[Math.floorMod((week - firstWeek) / 7, entries.size.toLong()).toInt()]
    }
}
