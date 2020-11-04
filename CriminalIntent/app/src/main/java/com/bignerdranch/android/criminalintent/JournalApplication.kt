package com.bignerdranch.android.criminalintent

import android.app.Application

class JournalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DayRepository.initialize(this)
    }
}