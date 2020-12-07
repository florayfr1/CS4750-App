package com.bignerdranch.android.journal

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import database.EntryDatabase
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "entry-database"


class EntryRepository private constructor(context: Context) {

    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    private val database : EntryDatabase = Room.databaseBuilder(
        context.applicationContext,
        EntryDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(EntryDatabase.migration_7_8)
        .build()

    private val entryDao = database.entryDao()

    fun getEntries(): LiveData<List<Entry>> = entryDao.getEntries()

    fun getEntry(id: UUID): LiveData<Entry?> = entryDao.getEntry(id)

    fun updateEntry(entry: Entry) {
        executor.execute {
            entryDao.updateEntry(entry)
        }
    }
    fun addEntry(entry: Entry) {
        executor.execute {
            entryDao.addEntry(entry)
        }
    }

    fun getPhotoFile(entry: Entry): File = File(filesDir,
        entry.photoFileName)

    companion object {
        private var INSTANCE: EntryRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = EntryRepository(context)
            }
        }

        fun get(): EntryRepository {
            return INSTANCE ?: throw IllegalStateException("EntryRepository must be initialized")
        }
    }
}