package app.dyer.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** One plate: everything on it at once, and how much of it was plants. */
@Entity(tableName = "meal")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long,
    /** Space-separated plant ids. */
    val plants: String,
    /** The share of the plate that was plants: .25, .5 or 1. */
    val plate: Float,
) {
    val ids: List<String> get() = plants.split(' ').filter { it.isNotBlank() }
}

/** A plant someone added because the catalogue didn't have it. Its id is «own<id>». */
@Entity(tableName = "own")
data class Own(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pigment: String,
    val kind: String,
    val at: Long,
) {
    fun plant(): Plant = Plant(
        id = "own$id", ru = name, en = name, latin = "", family = "own",
        pigment = pigment.firstOrNull()?.let(Pigment::of) ?: Pigment.GREEN,
        kind = Kind.of(kind) ?: Kind.VEG, season = emptySet(), asPlant = null, factRu = "", factEn = "", own = true,
    )
}

/** A dish kept for one tap: borscht is six paints at once. */
@Entity(tableName = "dish")
data class Dish(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val plants: String,
    val plate: Float,
) {
    val ids: List<String> get() = plants.split(' ').filter { it.isNotBlank() }
}

/** A quiet meal: minutes with the phone face down; every twenty draws a small detail into the week. */
@Entity(tableName = "quiet")
data class Quiet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long,
    val minutes: Float,
)

fun Long.day(zone: ZoneId): Long = Instant.ofEpochMilli(this).atZone(zone).toLocalDate().toEpochDay()
fun Long.date(zone: ZoneId): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
fun Long.hour(zone: ZoneId): Int = Instant.ofEpochMilli(this).atZone(zone).hour

/** The Monday a moment's week starts on, as an epoch day. Weeks are Monday to Sunday. */
fun Long.week(zone: ZoneId): Long = date(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()

fun weekStart(week: Long, zone: ZoneId): Long = LocalDate.ofEpochDay(week).atStartOfDay(zone).toInstant().toEpochMilli()
