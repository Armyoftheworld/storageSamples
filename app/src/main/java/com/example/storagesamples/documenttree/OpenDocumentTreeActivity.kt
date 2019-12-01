package com.example.storagesamples.documenttree

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.storagesamples.R
import kotlinx.android.synthetic.main.activity_open_document_tree.*

private const val OPEN_DIRECTORY_REQUEST_CODE = 0xf11e

class OpenDocumentTreeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_document_tree)
        setSupportActionBar(toolbar)

        fab_open_directory.setOnClickListener {
            openDirectory()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val directoryOpen = supportFragmentManager.backStackEntryCount > 0
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(directoryOpen)
                it.setDisplayShowHomeEnabled(directoryOpen)
            }

            if (directoryOpen) {
                fab_open_directory.hide()
            } else {
                fab_open_directory.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == OPEN_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val directoryUri = data?.data ?: return

            contentResolver.takePersistableUriPermission(directoryUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            showDirectoryContents(directoryUri)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun showDirectoryContents(directoryUri: Uri) {
        supportFragmentManager.beginTransaction().apply {
            val directoryTag = directoryUri.toString()
            val directoryFragment = DirectoryFragment.newInstance(directoryUri)
            replace(R.id.fragment_container, directoryFragment, directoryTag)
            addToBackStack(directoryTag)
            commitAllowingStateLoss()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun openDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_DIRECTORY_REQUEST_CODE)
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStack()
        return false
    }


}
