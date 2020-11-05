package database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.criminalintent.Crime
import java.util.*

@Dao
interface DayDao{
    @Query("SELECT * FROM day")
    fun getDays(): LiveData<List<Day>>

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getDay(id: UUID): LiveData<Day?>

    @Update
    fun updateDay(day: Day)

    @Insert
    fun addDay(day: Day)
}

