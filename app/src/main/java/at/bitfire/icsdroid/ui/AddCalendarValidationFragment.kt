/*
 * Copyright (c) Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.icsdroid.ui

import android.app.Application
import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import at.bitfire.ical4android.Event
import at.bitfire.icsdroid.CalendarFetcher
import at.bitfire.icsdroid.HttpClient
import at.bitfire.icsdroid.R
import okhttp3.MediaType
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

class AddCalendarValidationFragment: DialogFragment() {

    private lateinit var titleColorModel: TitleColorFragment.TitleColorModel
    private lateinit var credentialsModel: CredentialsFragment.CredentialsModel
    private lateinit var validationModel: ValidationModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        titleColorModel = ViewModelProviders.of(requireActivity()).get(TitleColorFragment.TitleColorModel::class.java)
        credentialsModel = ViewModelProviders.of(requireActivity()).get(CredentialsFragment.CredentialsModel::class.java)

        validationModel = ViewModelProviders.of(requireActivity()).get(ValidationModel::class.java)
        validationModel.result.observe(this, Observer { info ->
            dialog.dismiss()

            val errorMessage = info.exception?.localizedMessage
            if (errorMessage == null) {
                titleColorModel.url.value = info.url.toString()
                titleColorModel.color.value = resources.getColor(R.color.colorPrimary)

                if (titleColorModel.title.value.isNullOrBlank())
                    titleColorModel.title.value = info.calendarName ?: info.url?.file

                requireFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, AddCalendarDetailsFragment())
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            } else
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
        })

        val url = URL(titleColorModel.url.value ?: throw IllegalArgumentException("No URL given"))
        validationModel.initialize(url, credentialsModel.username.value, credentialsModel.password.value)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progress = ProgressDialog(activity)
        progress.setMessage(getString(R.string.add_calendar_validating))
        return progress
    }

    override fun onPause() {
        super.onPause()
        HttpClient.setForeground(false)
    }

    override fun onResume() {
        super.onResume()
        HttpClient.setForeground(true)
    }


    /* model and data source */

    class ValidationModel(
            application: Application
    ): AndroidViewModel(application) {

        val result = MutableLiveData<ResourceInfo>()
        var initialized = false

        fun initialize(originalUrl: URL, username: String?, password: String?) {
            synchronized(initialized) {
                if (initialized)
                    return
                initialized = true
            }

            val info = ResourceInfo()
            val downloader = object: CalendarFetcher(getApplication(), originalUrl) {
                override fun onSuccess(data: InputStream, contentType: MediaType?, eTag: String?, lastModified: Long?) {
                    InputStreamReader(data, contentType?.charset() ?: Charsets.UTF_8).use { reader ->
                        val properties = mutableMapOf<String, String>()
                        val events = Event.fromReader(reader, properties)

                        // URL may have changed because of redirects
                        info.url = url

                        info.calendarName = properties[Event.CALENDAR_NAME]
                        info.eventsFound = events.size
                    }

                    result.postValue(info)
                }

                override fun onError(error: Exception) {
                    info.exception = error
                    result.postValue(info)
                }
            }

            downloader.username = username
            downloader.password = password

            // directly ask for confirmation of custom certificates
            downloader.inForeground = true

            Thread(downloader).start()
        }

    }

}
