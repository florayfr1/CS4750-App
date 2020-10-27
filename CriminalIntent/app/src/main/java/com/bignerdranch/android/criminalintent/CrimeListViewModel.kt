package com.bignerdranch.android.criminalintent

import androidx.lifecycle.ViewModel
import kotlinx.android.synthetic.main.fragment_crime.view.*

class CrimeListViewModel : ViewModel()
{
    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

}