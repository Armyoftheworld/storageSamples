package com.example.storagesamples.document

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.net.toUri
import com.example.storagesamples.R
import kotlinx.android.synthetic.main.activity_open_document.*

private const val OPEN_DOCUMENT_REQUEST_CODE = 0x33
private const val TAG = "OpenDocumentActivity"
private const val LAST_OPENED_URI_KEY =
    "com.example.storagesamples.document.pref.LAST_OPENED_URI_KEY"

class OpenDocumentActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_document)
        getSharedPreferences(TAG, Context.MODE_PRIVATE).let {
            if (it.contains(LAST_OPENED_URI_KEY)) {
                val documentUri = it.getString(LAST_OPENED_URI_KEY, null)?.toUri() ?: return@let
                openDocument(documentUri)
            }
        }
    }

    private fun openDocument(documentUri: Uri) {
        /**
         * Save the document to [SharedPreferences]. We're able to do this, and use the
         * uri saved indefinitely, because we called [ContentResolver.takePersistableUriPermission]
         * up in [onActivityResult].
         */
        getSharedPreferences(TAG, Context.MODE_PRIVATE).edit {
            putString(LAST_OPENED_URI_KEY, documentUri.toString())
        }
        val fragment = ActionOpenDocumentFragment.newInstance(documentUri)
        supportFragmentManager.beginTransaction().apply {
            add(R.id.container, fragment)
            commitAllowingStateLoss()
        }
        no_document_view.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { documentUri ->
                /**
                 * Upon getting a document uri returned, we can use
                 * [ContentResolver.takePersistableUriPermission] in order to persist the
                 * permission across restarts.
                 *
                 * This may not be necessary for your app. If it is not requested, access
                 * to the uri would persist until the device is restarted.
                 *
                 * This app requests it to demonstrate how, and to allow us to reopen the last
                 * opened document when the app starts.
                 */
                contentResolver.takePersistableUriPermission(documentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                openDocument(documentUri)
            }
        }
    }

    fun openDocumentPicker(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            /**
             * Because we'll want to use [ContentResolver.openFileDescriptor] to read
             * the data of whatever file is picked, we set [Intent.CATEGORY_OPENABLE]
             * to ensure this will succeed.
             */
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            /**
             * In this app we'll only display PDF documents, but if it were capable
             * of editing a document, we may want to also request
             * [Intent.FLAG_GRANT_WRITE_URI_PERMISSION].
             */
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                AlertDialog.Builder(this)
                    .setMessage(R.string.intro_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                return true
            }
            R.id.action_open -> {
                openDocumentPicker(View(this))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
