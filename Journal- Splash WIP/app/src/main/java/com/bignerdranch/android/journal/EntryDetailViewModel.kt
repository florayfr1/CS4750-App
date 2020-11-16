package com.bignerdranch.android.journal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*

class EntryDetailViewModel() : ViewModel() {
    private val entryRepository = EntryRepository.get()
    private val entryIdLiveData = MutableLiveData<UUID>()
    var entryLiveData: LiveData<Entry?> =
        Transformations.switchMap(entryIdLiveData) { entryId ->
            entryRepository.getEntry(entryId)
        }

    fun loadEntry(entryId: UUID) {
        entryIdLiveData.value = entryId
    }

    fun saveEntry(entry: Entry) {
        entryRepository.updateEntry(entry)
    }

    fun getPhotoFile(entry: Entry): File {
        return entryRepository.getPhotoFile(entry)
    }
}