package com.bignerdranch.android.journalapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import database.DayDatabase
import java.io.File
import java.util.*
import java.util.concurrent.Executors


private const val DATABASE_NAME = "day-database"


class DayRepository private constructor(context: Context) {

    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    private val database : DayDatabase = Room.databaseBuilder(
        context.applicationContext,
        DayDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(DayDatabase.migration_1_2)
        .build()

    private val dayDao = database.dayDao()

    fun getDays(): LiveData<List<Day>> = dayDao.getDays()

    fun getDay(id: UUID): LiveData<Day?> = dayDao.getDay(id)

    fun updateDay(day: Day) {
        executor.execute {
            dayDao.updateDay(day)
        }
    }
    fun addDay(day: Day) {
        executor.execute {
            dayDao.addDay(day)
        }
    }

    fun getPhotoFile(day: Day): File = File(filesDir,
        day.photoFileName)

    companion object {
        private var INSTANCE: DayRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = DayRepository(context)
            }
        }

        fun get(): DayRepository {
            return INSTANCE ?: throw IllegalStateException("DayRepository must be initialized")
        }
    }
}