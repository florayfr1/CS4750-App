package database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.journal.Entry

@Database(entities = [ Entry::class ], version=8)
@TypeConverters(EntryTypeConverters::class)
abstract class EntryDatabase : RoomDatabase() {

    object migration_7_8 : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Entry ADD COLUMN link TEXT NOT NULL DEFAULT ''"

            )
        }
    }



    abstract fun entryDao(): EntryDao


}
