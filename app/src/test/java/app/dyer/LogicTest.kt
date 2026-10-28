package app.dyer

import app.dyer.domain.Brush
import app.dyer.domain.Catalogue
import app.dyer.domain.Kind
import app.dyer.domain.Meal
import app.dyer.domain.Pigment
import app.dyer.domain.Presets
import app.dyer.domain.Quarter
import app.dyer.domain.Recipes
import app.dyer.domain.WeekPaint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LogicTest {
    private val cat = Catalogue.load { File("src/main/assets/$it").takeIf { f -> f.exists() }?.inputStream() }

    @Test fun catalogueIsWhole() {
        assertTrue(cat.plants.size > 150)
        assertEquals(cat.plants.size, cat.plants.map { it.id }.toSet().size)
        for (p in cat.plants) {
            assertNotNull("family of ${p.id}", cat.family(p.family))
            assertTrue("fact of ${p.id}", p.factRu.isNotBlank() && p.factEn.isNotBlank())
            if (p.ferment) p.asPlant?.let { assertNotNull("ferment ${p.id} counts as $it", cat[it]) }
            else assertNotNull("pigment of ${p.id}", p.pigment)
        }
        for (pr in Presets.all) for (id in pr.plants) assertNotNull("preset ${pr.en}: $id", cat[id])
        for (r in Recipes.all) assertNotNull("recipe ${r.en}: ${r.plant}", cat[r.plant])
    }

    @Test fun everyQuarterTakesEveryPaint() {
        for (q in Quarter.entries) {
            for (p in Pigment.entries) assertTrue("$q lacks $p", q.slots.any { it.pigment == p })
            assertEquals(3, q.details.size)
            assertEquals(q.slots.size, q.slots.map { it.id }.toSet().size)
        }
    }

    @Test fun seasonsWrapTheNewYear() {
        assertEquals(setOf(12, 1, 2), Catalogue.months("12-2"))
        assertEquals(setOf(7, 8, 9), Catalogue.months("7-9"))
        assertTrue(Catalogue.months("").isEmpty())
    }

    private fun meal(id: Long, at: Long, plate: Float, vararg plants: String) = Meal(id, at, plants.joinToString(" "), plate)

    @Test fun weekCountsEachPlantOnceAndSpicesAsDrops() {
        val meals = listOf(
            meal(1, 1, 1f, "tomato", "cucumber", "dill"),
            meal(2, 2, 1f, "tomato", "saffron", "whiterice"),
            meal(3, 3, .25f, "sauerkraut", "cabbage", "kefir"),
        )
        val p = WeekPaint.compute(0, Quarter.MARKET, meals, emptyList(), cat, 30)
        // tomato, cucumber, dill ¼, saffron ¼, white rice ½, cabbage (sauerkraut counts as cabbage once).
        assertEquals(1f + 1f + .25f + .25f + .5f + 1f, p.total, .001f)
        assertTrue(p.fixed)
        assertEquals(listOf("tomato", "cucumber", "dill", "saffron", "whiterice", "cabbage"), p.plants)
        assertFalse(p.festival)
    }

    @Test fun paintSpreadsToTheEmptiestThingOfItsColour() {
        val p = WeekPaint.compute(0, Quarter.MARKET, listOf(meal(1, 1, 1f, "tomato", "apple", "beet", "cranberry")), emptyList(), cat, 30)
        val reds = Quarter.MARKET.slots.filter { it.pigment == Pigment.RED }.map { it.id }
        val touched = p.washes.map { it.slot }
        assertTrue(touched.all { it in reds })
        // Four reds over three red things: each gets paint before any gets a second wash.
        assertEquals(reds.toSet(), touched.toSet())
    }

    @Test fun aThinBrushPaintsLess() {
        val wide = WeekPaint.compute(0, Quarter.MILL, listOf(meal(1, 1, 1f, "carrot")), emptyList(), cat, 30)
        val thin = WeekPaint.compute(0, Quarter.MILL, listOf(meal(1, 1, .25f, "carrot")), emptyList(), cat, 30)
        val slot = wide.washes.single().slot
        assertTrue(wide.cover(slot) > thin.cover(slot))
        assertEquals(.5f, Brush.strength(.25f), .001f)
    }

    @Test fun thirtyPlantsMakeAFestival() {
        val ids = cat.plants.filter { it.kind == Kind.VEG || it.kind == Kind.FRUIT }.take(30).map { it.id }
        val p = WeekPaint.compute(0, Quarter.CASTLE, listOf(meal(1, 1, 1f, *ids.toTypedArray())), emptyList(), cat, 30)
        assertTrue(p.festival)
    }
}
