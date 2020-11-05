package com.bignerdranch.android.journal

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*
import androidx.lifecycle.Observer
import java.io.File


private const val TAG = "EntryFragment"
private const val ARG_ENTRY_ID = "entry_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0

private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2

class EntryFragment : Fragment(), DatePickerFragment.Callbacks{
    private lateinit var entry: Entry
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val entryDetailViewModel: EntryDetailViewModel by lazy {
        ViewModelProviders.of(this).get(EntryDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entry = Entry()
        val entryId: UUID = arguments?.getSerializable(ARG_ENTRY_ID) as
                UUID
        entryDetailViewModel.loadEntry(entryId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_entry,container,false)

        titleField = view.findViewById(R.id.entry_title) as EditText
        dateButton = view.findViewById(R.id.entry_date) as Button
        solvedCheckBox = view.findViewById(R.id.entry_solved) as CheckBox
        reportButton = view.findViewById(R.id.entry_report) as Button
        suspectButton = view.findViewById(R.id.entry_suspect) as Button

        photoButton = view.findViewById(R.id.entry_camera) as ImageButton
        photoView = view.findViewById(R.id.entry_photo) as ImageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        entryDetailViewModel.entryLiveData.observe(
            viewLifecycleOwner,
            Observer { entry ->
                entry?.let {
                    this.entry = entry
                    photoFile = entryDetailViewModel.getPhotoFile(entry)
                    photoUri =
                        FileProvider.getUriForFile(requireActivity(),
                            "com.bignerdranch.android.journal.fileprovider",
                            photoFile)

                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                entry.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener{ _, isChecked ->
                entry.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(entry.date).apply {
                setTargetFragment(this@EntryFragment, REQUEST_DATE)
                show(this@EntryFragment.requireFragmentManager(),
                    DIALOG_DATE)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getEntryReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.entry_report_subject))
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent,
                        getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {


            val pickContactIntent =
                Intent(Intent.ACTION_PICK,
                    ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent,
                    REQUEST_CONTACT)
            }


            val packageManager: PackageManager =
                requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        photoButton.apply {
            val packageManager: PackageManager =
                requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        entryDetailViewModel.saveEntry(entry)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onDateSelected(date: Date) {
        entry.date = date
        updateUI()
    }

    private fun updateUI() {
        titleField.setText(entry.title)
        dateButton.text = entry.date.toString()
        solvedCheckBox.apply {
            isChecked = entry.isSolved
            jumpDrawablesToCurrentState()
        }
        if (entry.suspect.isNotEmpty()) {
            suspectButton.text = entry.suspect
        }
        updatePhotoView()
    }


    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path,
                requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
// Specify which fields you want your query to return values for
                val queryFields =
                    arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
// Perform your query - the contactUri is like a "where" clause here
                val cursor = requireActivity().contentResolver
                    .query(contactUri?: Uri.EMPTY, queryFields, null, null, null)
                cursor?.use {
// Verify cursor contains at least one result
                    if (it.count == 0) {
                        return
                    }
// Pull out the first column of the first row of data -
// that is your suspect's name
                            it.moveToFirst()
                    val suspect = it.getString(0)
                    entry.suspect = suspect
                    entryDetailViewModel.saveEntry(entry)
                    suspectButton.text = suspect
                }
            }
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }

    private fun getEntryReport(): String {
        val solvedString = if (entry.isSolved) {
            getString(R.string.entry_report_solved)
        } else {
            getString(R.string.entry_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT,
            entry.date).toString()
        var suspect = if (entry.suspect.isBlank()) {
            getString(R.string.entry_report_no_suspect)
        } else {
            getString(R.string.entry_report_suspect, entry.suspect)
        }
        return getString(R.string.entry_report,
            entry.title, dateString, solvedString, suspect)
    }

    companion object{
        fun newInstance(entryId: UUID): EntryFragment
        {
            val args = Bundle().apply{
                putSerializable(ARG_ENTRY_ID, entryId)
            }
            return  EntryFragment().apply {
                arguments = args
            }
        }
    }
}