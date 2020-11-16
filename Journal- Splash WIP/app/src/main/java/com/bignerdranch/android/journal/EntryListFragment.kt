package com.bignerdranch.android.journal

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.*

class EntryListFragment : Fragment() {

    /**
     * Required interface for hosting activities
     */
    interface Callbacks {
        fun onEntrySelected(entryId: UUID)
    }

    private var callbacks: Callbacks? = null

    private lateinit var entryRecyclerView: RecyclerView
    private var adapter: EntryAdapter? = EntryAdapter(emptyList())

    private val entryListViewModel: EntryListViewModel by lazy {
        ViewModelProviders.of(this).get(EntryListViewModel::class.java)
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
        fun newInstance(): EntryListFragment {
            return EntryListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_entry_list,
            container, false
        )
        entryRecyclerView =
            view.findViewById(R.id.entry_recycler_view) as RecyclerView
        entryRecyclerView.layoutManager = LinearLayoutManager(context)
        entryRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        entryListViewModel.entryListLiveData.observe(
            viewLifecycleOwner,
            Observer { entries ->
                entries?.let {
                    Log.i(TAG, "Got entries ${entries.size}")
                    updateUI(entries)
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
        inflater.inflate(R.menu.fragment_entry_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_entry -> {
                val entry = Entry()
                entryListViewModel.addEntry(entry)
                callbacks?.onEntrySelected(entry.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(entries: List<Entry>) {
        adapter = EntryAdapter(entries)
        entryRecyclerView.adapter = adapter
    }

    private inner class EntryHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener{

        private lateinit var entry: Entry

        private val titleTextView: TextView =
            itemView.findViewById(R.id.entry_title)

        private val dateTextView: TextView = itemView.findViewById(R.id.entry_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.entry_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(entry: Entry) {
            this.entry = entry
            titleTextView.text = this.entry.title
            val dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.US)
            dateTextView.text = dateFormat.format(this.entry.date)
            solvedImageView.visibility = if (entry.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
            callbacks?.onEntrySelected(entry.id)
        }
    }

    private inner class EntryAdapter(var entries: List<Entry>)
        : RecyclerView.Adapter<EntryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType:
        Int)
                : EntryHolder {
            val view = layoutInflater.inflate(R.layout.list_item_entry,
                parent, false)
            return EntryHolder(view)
        }
        override fun onBindViewHolder(holder: EntryHolder, position:
        Int) {
            val entry = entries[position]
            holder.bind(entry)
        }
        override  fun getItemCount() = entries.size
    }
}