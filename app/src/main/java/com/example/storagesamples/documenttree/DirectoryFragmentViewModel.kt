package com.example.storagesamples.documenttree

import android.app.Application
import android.net.Uri
import androidx.constraintlayout.solver.Cache
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-01
 * @description
 */
class DirectoryFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val _documents = MutableLiveData<List<CachingDocumentFile>>()
    val documents = _documents

    private val _openDirectory = MutableLiveData<Event<CachingDocumentFile>>()
    val openDirectory = _openDirectory

    private val _openDocument = MutableLiveData<Event<CachingDocumentFile>>()
    val openDocument = _openDocument

    fun loadDirectory(directoryUri: Uri) {
        val documentsTree = DocumentFile.fromTreeUri(getApplication(), directoryUri) ?: return
        println("loadDirectory directoryUri = $directoryUri")
        val childDocuments = documentsTree.listFiles().toCachingList()
        // It's much nicer when the documents are sorted by something, so we'll sort the documents
        // we got by name. Unfortunate there may be quite a few documents, and sorting can take
        // some time, so we'll take advantage of coroutines to take this work off the main thread.
        viewModelScope.launch {
            val sortedDocuments = withContext(Dispatchers.IO) {
                childDocuments.sortedWith(Comparator { o1, o2 ->
                    if (o1.isDirectory && o2.isDirectory ||
                        !o1.isDirectory && !o2.isDirectory
                    ) {
                        o1.name?.compareTo(o2.name ?: "") ?: 0
                    } else if (o1.isDirectory) {
                        -1
                    } else {
                        1
                    }
                })
            }
            _documents.postValue(sortedDocuments)
        }
    }

    fun documentClicked(clickDocument: CachingDocumentFile) {
        if (clickDocument.isDirectory) {
            openDirectory.postValue(Event(clickDocument))
        } else {
            openDocument.postValue(Event(clickDocument))
        }
    }

}