package uk.nhs.nhsx.sonar.android.app.debug

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.Seconds
import uk.nhs.nhsx.sonar.android.app.MainActivity
import uk.nhs.nhsx.sonar.android.app.ble.BleEvents
import uk.nhs.nhsx.sonar.android.app.ble.BluetoothService
import uk.nhs.nhsx.sonar.android.app.contactevents.ContactEventDao
import uk.nhs.nhsx.sonar.android.app.util.toUtcIsoFormat
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class TestViewModel @Inject constructor(
    private val context: Context,
    private val contactEventDao: ContactEventDao,
    private val eventTracker: BleEvents
) : ViewModel() {

    fun clear() {
        viewModelScope.launch {

            context.stopService(Intent(context, BluetoothService::class.java))

            contactEventDao.clearEvents()
            eventTracker.clear()
            withContext(Dispatchers.IO) {
                FirebaseInstanceId.getInstance().deleteInstanceId()
            }

            MainActivity.start(context)
        }
    }

    fun storeEvents() {
        viewModelScope.launch {
            val events = contactEventDao.getAll()
            val text = events.joinToString("\n") {
                val eventTime = DateTime(it.timestamp)
                "${it.idAsString()},${eventTime.toUtcIsoFormat()},${it.duration},${it.rssiValues.joinToString(":")},${it.rssiTimestamps.map{ Seconds.secondsBetween(eventTime, DateTime(it)).seconds}.joinToString(":")}}"
            }

            val zipFile = "contact-events-exports.zip"

            context.openFileOutput(zipFile, Context.MODE_PRIVATE).use {
                ZipOutputStream(it).use { zip ->
                    zip.putNextEntry(ZipEntry("contact-events.csv"))
                    zip.write(text.toByteArray())
                }
            }

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(
                        context,
                        "uk.nhs.nhsx.sonar.android.app.exports",
                        File(context.filesDir, zipFile)
                    )
                )
                type = "application/zip"
            }

            context.startActivity(Intent.createChooser(sendIntent, "Export events"))
        }
    }

    fun observeConnectionEvents() = eventTracker.observeConnectionEvents()
}
