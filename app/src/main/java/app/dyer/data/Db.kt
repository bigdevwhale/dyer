package app.dyer.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import app.dyer.domain.Dish
import app.dyer.domain.Meal
import app.dyer.domain.Own
import app.dyer.domain.Quiet

@Dao
interface DyerDao {
    @Query("SELECT * FROM meal ORDER BY at, id") suspend fun meals(): List<Meal>
    @Query("SELECT * FROM own ORDER BY id") suspend fun own(): List<Own>
    @Query("SELECT * FROM dish ORDER BY id") suspend fun dishes(): List<Dish>
    @Query("SELECT * FROM quiet ORDER BY at, id") suspend fun quiet(): List<Quiet>

    @Insert suspend fun insert(m: Meal): Long
    @Insert suspend fun insert(o: Own): Long
    @Insert suspend fun insert(d: Dish): Long
    @Insert suspend fun insert(q: Quiet): Long

    @Query("DELETE FROM meal WHERE id = :id") suspend fun deleteMeal(id: Long)
    @Query("DELETE FROM dish WHERE id = :id") suspend fun deleteDish(id: Long)
}

@Database(entities = [Meal::class, Own::class, Dish::class, Quiet::class], version = 1, exportSchema = true)
abstract class DyerDb : RoomDatabase() {
    abstract fun dao(): DyerDao

    companion object {
        fun create(c: Context): DyerDb = Room.databaseBuilder(c, DyerDb::class.java, "dyer.db").build()
    }
}
