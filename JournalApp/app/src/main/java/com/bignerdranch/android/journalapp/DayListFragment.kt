package com.bignerdranch.android.journalapp

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.*

class DayListFragment : Fragment() {

    /**
     * Required interface for hosting activities
     */
    interface Callbacks {
        fun onDaySelected(dayId: UUID)
    }

    private var callbacks: Callbacks? = null

    private lateinit var dayRecyclerView: RecyclerView
    private var adapter: DayAdapter? = DayAdapter(emptyList())

    private val dayListViewModel: DayListViewModel by lazy {
        ViewModelProviders.of(this).get(DayListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    companion object {
        fun newInstance(): DayListFragment {
            return DayListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_day_list,
            container, false
        )
        dayRecyclerView =
            view.findViewById(R.id.day_recycler_view) as RecyclerView
        dayRecyclerView.layoutManager = LinearLayoutManager(context)
        dayRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        dayListViewModel.dayListLiveData.observe(
            viewLifecycleOwner,
            Observer { days ->
                days?.let {
                    Log.i(TAG, "Got days ${days.size}")
                    updateUI(days)
                }
            })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_day_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_day -> {
                val day = Day()
                dayListViewModel.addDay(day)
                callbacks?.onDaySelected(day.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(days: List<Day>) {
        adapter = DayAdapter(days)
        dayRecyclerView.adapter = adapter
    }

    private inner class DayHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener{

        private lateinit var day: Day

        private val titleTextView: TextView =
            itemView.findViewById(R.id.day_title)

        private val dateTextView: TextView = itemView.findViewById(R.id.day_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.day_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(day: Day) {
            this.day = day
            titleTextView.text = this.day.title
            val dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US)
            dateTextView.text = dateFormat.format(this.day.date)
            solvedImageView.visibility = if (day.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
            callbacks?.onDaySelected(day.id)
        }
    }

    private inner class DayAdapter(var days: List<Day>)
        : RecyclerView.Adapter<DayHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType:
        Int)
                : DayHolder {
            val view = layoutInflater.inflate(R.layout.list_item_day,
                parent, false)
            return DayHolder(view)
        }
        override fun onBindViewHolder(holder: DayHolder, position:
        Int) {
            val day = days[position]
            holder.bind(day)
        }
        override  fun getItemCount() = days.size
    }
}

