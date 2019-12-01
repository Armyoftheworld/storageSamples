package com.example.storagesamples.documenttree

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storagesamples.R

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-01
 * @description
 */
private const val ARG_DIRECTORY_URI = "com.example.storagesamples.documenttree.ARG_DIRECTORY_URI"

class DirectoryFragment : Fragment() {

    companion object {
        fun newInstance(directoryUri: Uri): DirectoryFragment {
            println("directoryUri = ${directoryUri.toString()}")
            return DirectoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DIRECTORY_URI, directoryUri.toString())
                }
            }
        }
    }

    private lateinit var directoryUri: Uri
    private lateinit var adapter: DirectoryEntryAdapter
    private lateinit var viewModel: DirectoryFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        directoryUri = arguments?.getString(ARG_DIRECTORY_URI)?.toUri()
            ?: throw IllegalArgumentException("Must pass URI of directory to open")
        viewModel = ViewModelProviders.of(this).get(DirectoryFragmentViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_directory, container, false)
        val list = view.findViewById<RecyclerView>(R.id.list)
        list.layoutManager = LinearLayoutManager(list.context)
        adapter = DirectoryEntryAdapter(object: OnDocClickListener {
            override fun onDocumentClicked(clickDocument: CachingDocumentFile) {
                viewModel.documentClicked(clickDocument)
            }

            override fun onDocumentLongClicked(clickeDocument: CachingDocumentFile) {
                renameDocument(clickeDocument)
            }

        })
        list.adapter = adapter
        viewModel.documents.observe(this, Observer {
            it.let { adapter.setEntries(it) }
        })
        viewModel.openDirectory.observe(this, Observer {
            it.getContentIfNotHandled()?.let { directory ->
                println("directory.uri = ${directory.uri}")
                (activity as OpenDocumentTreeActivity).showDirectoryContents(directory.uri)
            }
        })
        viewModel.openDocument.observe(this, Observer {
            it.getContentIfNotHandled()?.let { document ->
                openDocument(document)
            }
        })
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        println("onActivityCreated directoryUri = $directoryUri")
        viewModel.loadDirectory(directoryUri)
    }

    private fun openDocument(document: CachingDocumentFile) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            data = document.uri
        }
        if (intent.resolveActivity(requireContext().packageManager)!= null) {
            startActivity(intent)
        } else {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.error_no_activity, document.name),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun renameDocument(document: CachingDocumentFile) {
        val dialogView = layoutInflater.inflate(R.layout.rename_layout, null)
        val editText = dialogView.findViewById<EditText>(R.id.file_name)
        editText.setText(document.name)
        // Use a lambda so that we have access to the [EditText] with the new name.
        val buttonCallback: (DialogInterface, Int) -> Unit = { _, buttonId ->
            when(buttonId) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val newName = editText.text.toString()
                    if (!TextUtils.isEmpty(newName)) {
                        document.rename(newName)
                        // The easiest way to refresh the UI is to load the directory again.
                        viewModel.loadDirectory(directoryUri)
                    }
                }
            }
        }

        val renameDialog = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.rename_title)
            .setView(dialogView)
            .setPositiveButton(R.string.rename_okay, buttonCallback)
            .setNegativeButton(R.string.rename_cancel, buttonCallback)
            .create()

        // When the dialog is shown, select the name so it can be easily changed.
        renameDialog.setOnShowListener {
            editText.requestFocus()
            editText.selectAll()
        }

        renameDialog.show()
    }
}