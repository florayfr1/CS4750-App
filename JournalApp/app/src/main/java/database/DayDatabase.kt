package database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.journalapp.Day

@Database(entities = [ Day::class ], version=2)
@TypeConverters(database.DayTypeConverters::class)
abstract class DayDatabase : RoomDatabase() {

    object migration_1_2 : Migration(1,2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Day ADD COLUMN suspect TEXT NOT NULL DEFAULT''"
            )
        }
    }

    abstract fun dayDao(): DayDao


}

