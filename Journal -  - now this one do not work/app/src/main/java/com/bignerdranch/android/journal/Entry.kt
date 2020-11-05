package com.bignerdranch.android.journal

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Entry(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = ""){
    val photoFileName
        get() = "IMG_$id.jpg"
}