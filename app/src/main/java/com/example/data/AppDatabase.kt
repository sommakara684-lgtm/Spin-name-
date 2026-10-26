package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.SpinHistoryEntity
import com.example.model.WheelEntity
import com.example.model.WheelItemEntity
import com.example.model.WheelPaletteTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WheelEntity::class,
        WheelItemEntity::class,
        SpinHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wheelDao(): WheelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spin_name_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialPresets(database.wheelDao())
                    }
                }
            }
        }

        suspend fun populateInitialPresets(dao: WheelDao) {
            // Preset 1: Classroom / Team Names
            val wheel1Id = dao.insertWheel(
                WheelEntity(
                    title = "Classroom & Team",
                    description = "Pick students, speakers, or teammates randomly",
                    themePalette = WheelPaletteTheme.RAINBOW.name,
                    spinDurationSeconds = 4.5f,
                    autoRemoveWinner = false,
                    isPreset = true
                )
            )
            val students = listOf(
                "Alice", "Bob", "Charlie", "David", "Emma",
                "Frank", "Grace", "Henry", "Isabella", "Jack"
            )
            dao.insertItems(students.mapIndexed { index, name ->
                WheelItemEntity(wheelId = wheel1Id, name = name, orderIndex = index)
            })

            // Preset 2: Lunch Chooser
            val wheel2Id = dao.insertWheel(
                WheelEntity(
                    title = "What to Eat?",
                    description = "End the food debate with a quick spin",
                    themePalette = WheelPaletteTheme.SUNSET.name,
                    spinDurationSeconds = 4.0f,
                    autoRemoveWinner = false,
                    isPreset = true
                )
            )
            val foods = listOf(
                "Pizza", "Burgers", "Sushi", "Tacos", "Ramen", "Pasta", "Burritos", "Salad"
            )
            dao.insertItems(foods.mapIndexed { index, name ->
                WheelItemEntity(wheelId = wheel2Id, name = name, orderIndex = index)
            })

            // Preset 3: Yes or No
            val wheel3Id = dao.insertWheel(
                WheelEntity(
                    title = "Yes or No",
                    description = "Quick binary choice maker",
                    themePalette = WheelPaletteTheme.NEON.name,
                    spinDurationSeconds = 3.5f,
                    autoRemoveWinner = false,
                    isPreset = true
                )
            )
            val yesNo = listOf("YES!", "NO", "MAYBE", "SPIN AGAIN")
            dao.insertItems(yesNo.mapIndexed { index, name ->
                WheelItemEntity(wheelId = wheel3Id, name = name, orderIndex = index)
            })

            // Preset 4: Truth or Dare
            val wheel4Id = dao.insertWheel(
                WheelEntity(
                    title = "Truth or Dare",
                    description = "Party game decision wheel",
                    themePalette = WheelPaletteTheme.CANDY.name,
                    spinDurationSeconds = 4.0f,
                    autoRemoveWinner = false,
                    isPreset = true
                )
            )
            val truthOrDare = listOf("Truth", "Dare", "Double Dare", "Pass", "Choose Someone")
            dao.insertItems(truthOrDare.mapIndexed { index, name ->
                WheelItemEntity(wheelId = wheel4Id, name = name, orderIndex = index)
            })
        }
    }
}
