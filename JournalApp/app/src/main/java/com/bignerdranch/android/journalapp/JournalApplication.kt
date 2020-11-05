package com.bignerdranch.android.journalapp

import android.app.Application

class JournalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DayRepository.initialize(this)
    }
}