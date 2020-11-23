package database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.journal.Entry

@Database(entities = [ Entry::class ], version=6)
@TypeConverters(EntryTypeConverters::class)
abstract class EntryDatabase : RoomDatabase() {

    object migration_5_6 : Migration(5,6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Entry ADD COLUMN rating REAL NOT NULL DEFAULT '0.0'"

            )
        }
    }



    abstract fun entryDao(): EntryDao


}
