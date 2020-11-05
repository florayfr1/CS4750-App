package database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.journal.Entry

@Database(entities = [ Entry::class ], version=2)
@TypeConverters(EntryTypeConverters::class)
abstract class EntryDatabase : RoomDatabase() {

    object migration_1_2 : Migration(1,2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Entry ADD COLUMN suspect TEXT NOT NULL DEFAULT''"
            )
        }
    }

    abstract fun entryDao(): EntryDao


}
