package database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.journal.Entry

@Database(entities = [ Entry::class ], version=5)
@TypeConverters(EntryTypeConverters::class)
abstract class EntryDatabase : RoomDatabase() {

    object migration_4_5 : Migration(4,5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Entry ADD COLUMN good3 TEXT NOT NULL DEFAULT''"

            )
        }
    }



    abstract fun entryDao(): EntryDao


}
