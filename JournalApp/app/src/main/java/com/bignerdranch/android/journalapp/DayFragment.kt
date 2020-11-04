package com.bignerdranch.android.journalapp

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
import android.util.Log
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
import java.lang.String.format


private const val TAG = "DayFragment"
private const val ARG_CRIME_ID = "day_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0

private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2

class DayFragment : Fragment(), DatePickerFragment.Callbacks{
    private lateinit var day: Day
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val dayDetailViewModel: DayDetailViewModel by lazy {
        ViewModelProviders.of(this).get(DayDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        day = Day()
        val dayId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as
                UUID
        dayDetailViewModel.loadDay(dayId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_day,container,false)

        titleField = view.findViewById(R.id.day_title) as EditText
        dateButton = view.findViewById(R.id.day_date) as Button
        solvedCheckBox = view.findViewById(R.id.day_solved) as CheckBox
        reportButton = view.findViewById(R.id.day_report) as Button
        suspectButton = view.findViewById(R.id.day_suspect) as Button

        photoButton = view.findViewById(R.id.day_camera) as ImageButton
        photoView = view.findViewById(R.id.day_photo) as ImageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        dayDetailViewModel.dayLiveData.observe(
            viewLifecycleOwner,
            Observer { day ->
                day?.let {
                    this.day = day
                    photoFile = dayDetailViewModel.getPhotoFile(day)
                    photoUri =
                        FileProvider.getUriForFile(requireActivity(),
                            "com.bignerdranch.android.journalapp.fileprovider",
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
                day.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener{ _, isChecked ->
                day.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(day.date).apply {
                setTargetFragment(this@DayFragment, REQUEST_DATE)
                show(this@DayFragment.requireFragmentManager(),
                    DIALOG_DATE)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getDayReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.day_report_subject))
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
        dayDetailViewModel.saveDay(day)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onDateSelected(date: Date) {
        day.date = date
        updateUI()
    }

    private fun updateUI() {
        titleField.setText(day.title)
        dateButton.text = day.date.toString()
        solvedCheckBox.apply {
            isChecked = day.isSolved
            jumpDrawablesToCurrentState()
        }
        if (day.suspect.isNotEmpty()) {
            suspectButton.text = day.suspect
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
                    day.suspect = suspect
                    dayDetailViewModel.saveDay(day)
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

    private fun getDayReport(): String {
        val solvedString = if (day.isSolved) {
            getString(R.string.day_report_solved)
        } else {
            getString(R.string.day_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT,
            day.date).toString()
        var suspect = if (day.suspect.isBlank()) {
            getString(R.string.day_report_no_suspect)
        } else {
            getString(R.string.day_report_suspect, day.suspect)
        }
        return getString(R.string.day_report,
            day.title, dateString, solvedString, suspect)
    }

    companion object{
        fun newInstance(dayId: UUID): DayFragment
        {
            val args = Bundle().apply{
                putSerializable(ARG_CRIME_ID, dayId)
            }
            return  DayFragment().apply {
                arguments = args
            }
        }
    }
}


