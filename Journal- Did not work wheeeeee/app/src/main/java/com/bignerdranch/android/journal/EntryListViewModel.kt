package com.bignerdranch.android.journal

import androidx.lifecycle.ViewModel

class EntryListViewModel : ViewModel()
{
    private val entryRepository = EntryRepository.get()
    val entryListLiveData = entryRepository.getEntries()

    fun addEntry(entry: Entry) {
        entryRepository.addEntry(entry)
    }

}