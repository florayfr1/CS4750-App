package com.bignerdranch.android.journal

import android.app.Application

class JournalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        EntryRepository.initialize(this)
    }
}