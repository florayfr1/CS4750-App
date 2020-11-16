package com.bignerdranch.android.journal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*
private  const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(),
    EntryListFragment.Callbacks{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = EntryListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onEntrySelected(entryId: UUID) {
        val fragment = EntryFragment.newInstance(entryId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}