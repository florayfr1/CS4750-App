package com.bignerdranch.android.journalapp

import androidx.lifecycle.ViewModel
import kotlinx.android.synthetic.main.fragment_day.view.*

class DayListViewModel : ViewModel()
{
    private val dayRepository = DayRepository.get()
    val dayListLiveData = dayRepository.getDays()

    fun addDay(day: Day) {
        dayRepository.addDay(day)
    }

}
