package com.bignerdranch.android.journal

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Entry(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = "",
                 var good1: String = "",
                 var good2: String = "",
                 var good3: String = "",
                 var rating: Float = 0.0F,
                 var reflect: String = "",
                 var link: String = ""


){
    val photoFileName
        get() = "IMG_$id.jpg"
}