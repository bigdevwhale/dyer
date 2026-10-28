package app.dyer.ui.shelf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.dyer.domain.Guild
import app.dyer.domain.Pigment
import app.dyer.domain.Plant
import app.dyer.domain.Recipes
import app.dyer.ui.Home
import app.dyer.ui.Nav
import app.dyer.ui.Overlay
import app.dyer.ui.art.Jar
import app.dyer.ui.components.Card
import app.dyer.ui.components.Caps
import app.dyer.ui.components.Dot
import app.dyer.ui.components.Gap
import app.dyer.ui.components.TopBar
import app.dyer.ui.components.color
import app.dyer.ui.theme.C
import app.dyer.ui.theme.T

/** Every plant ever eaten stands here as a jar of its paint, shelf by shelf. */
@Composable
fun ShelfScreen(h: Home, nav: Nav) {
    val w = h.w
    val s = h.s
    val jars = s.jars
    val plants = jars.keys.mapNotNull { s.cat[it] }
    val total = s.cat.plants.count { !it.own && !it.ferment }
    val families = plants.map { it.family }.filter { it != "own" && it != "dairy" }.toSet().size
    Column(Modifier.fillMaxSize()) {
        TopBar(
            w.t("Полка", "Shelf"),
            w.t("${plants.count { !it.ferment }} ${w.plural(plants.count { !it.ferment }, "банка", "банки", "банок")} из $total · семейств: $families",
                "${plants.count { !it.ferment }} jars of $total · families: $families"),
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp)) {
            GuildOrder(h, nav)
            Gap(8)
            val open = Recipes.all.count { it.plant in jars }
            Card(onClick = { nav.push(Overlay.Recipes) }) {
                Text(w.t("Книга красильщика", "The dyer’s book"), style = T.Heading)
                Text(
                    w.t("Настоящие опыты на кухне: яйца в луковой шелухе, капустный индикатор, черничные чернила. Открыто $open из ${Recipes.all.size} ›",
                        "Real kitchen experiments: onion-skin eggs, red cabbage indicator, bilberry ink. $open of ${Recipes.all.size} open ›"),
                    style = T.Small,
                )
            }
            if (plants.isEmpty()) Text(
                w.t("Полка пока пустая. Каждое новое растение ставит сюда свою банку.", "The shelf is empty for now. Every new plant puts its jar here."),
                style = T.Italic, modifier = Modifier.padding(vertical = 24.dp),
            )
            for (pig in Pigment.entries) {
                val row = plants.filter { it.pigment == pig && !it.ferment }.sortedBy { it.name(h.ru) }
                if (row.isEmpty()) continue
                Shelf(h, w.pigment(pig).replaceFirstChar(Char::uppercase) + " · " + row.size, row, nav)
            }
            val ferm = plants.filter { it.ferment }.sortedBy { it.name(h.ru) }
            if (ferm.isNotEmpty()) Shelf(h, w.t("Квашеное · протрава", "Fermented · fixatives"), ferm, nav)
            Gap(20)
        }
    }
}

@Composable
private fun Shelf(h: Home, title: String, row: List<Plant>, nav: Nav) {
    Caps(title)
    Box {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (p in row) {
                Column(Modifier.width(66.dp).clickable { nav.push(Overlay.Plant(p.id)) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Jar(p, h.s.jars[p.id]?.times ?: 0, Modifier.size(42.dp, 50.dp))
                    Box(Modifier.width(62.dp).height(4.dp).background(C.shelf))
                    Text(p.name(h.ru), style = T.Label.copy(fontSize = 15.sp, lineHeight = 15.sp, textAlign = TextAlign.Center), maxLines = 2, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

/** Three plants the quarter would like, with how to eat them without fuss. */
@Composable
fun GuildOrder(h: Home, nav: Nav) {
    val w = h.w
    val picks = Guild.order(h.s, h.paint, h.today)
    val missing = Pigment.entries.filter { it !in h.paint.pigments }
    Card {
        Text(w.t("Заказ гильдии", "The guild’s order"), style = T.Heading)
        Text(
            if (missing.isEmpty()) w.t("На этой неделе есть все семь красок. Гильдия просит просто чего-нибудь нового.", "All seven paints are in this week. The guild just asks for something new.")
            else w.t("Кварталу «${h.paint.quarter.ru}» не хватает ", "${h.paint.quarter.en} is short of ") + missing.take(3).joinToString(w.t(" и ", " and ")) { w.pigmentOf(it) } + ".",
            style = T.Small,
        )
        for (pk in picks) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { nav.push(Overlay.Plant(pk.plant.id)) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Dot(pk.plant.pigment.color(), 20.dp)
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Text(pk.plant.name(h.ru), style = T.BodyB)
                    val why = listOfNotNull(
                        if (pk.fresh) w.t("ещё ни разу", "never tried") else null,
                        if (pk.missing) w.t("даст ", "gives ") + w.pigment(pk.plant.pigment!!) else null,
                        if (pk.plant.seasonal) w.t("сейчас сезон", "in season now") else null,
                    ).joinToString(" · ")
                    Text((if (why.isNotEmpty()) "$why — " else "") + w.tip(pk.plant.kind), style = T.Small)
                }
                Text("›", style = T.Heading.copy(color = C.wine))
            }
        }
    }
}

private val Int.sp get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
