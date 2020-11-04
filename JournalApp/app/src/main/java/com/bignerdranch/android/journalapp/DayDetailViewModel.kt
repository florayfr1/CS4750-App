package com.bignerdranch.android.journalapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*

class DayDetailViewModel() : ViewModel() {
    private val dayRepository = DayRepository.get()
    private val dayIdLiveData = MutableLiveData<UUID>()
    var dayLiveData: LiveData<Day?> =
        Transformations.switchMap(dayIdLiveData) { dayId ->
            dayRepository.getDay(dayId)
        }

    fun loadDay(dayId: UUID) {
        dayIdLiveData.value = dayId
    }

    fun saveDay(day: Day) {
        dayRepository.updateDay(day)
    }

    fun getPhotoFile(day: Day): File {
        return dayRepository.getPhotoFile(day)
    }
}


