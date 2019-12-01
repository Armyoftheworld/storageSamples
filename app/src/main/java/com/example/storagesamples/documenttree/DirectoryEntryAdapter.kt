package com.example.storagesamples.documenttree

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.storagesamples.R

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-01
 * @description
 */
class DirectoryEntryAdapter(private val docClickListener: OnDocClickListener) :
    RecyclerView.Adapter<DirectoryEntryAdapter.ViewHolder>() {

    private val directoryEntries = mutableListOf<CachingDocumentFile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.directory_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = directoryEntries.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val item = directoryEntries[position]
            val itemDrawables = if (item.isDirectory) {
                R.drawable.ic_folder_black_24dp
            } else {
                R.drawable.ic_file_black_24dp
            }
            fileName.text = item.name
            fileType.text = item.type
            imageView.setImageResource(itemDrawables)
            root.setOnClickListener {
                docClickListener.onDocumentClicked(item)
            }
            root.setOnLongClickListener {
                docClickListener.onDocumentLongClicked(item)
                true
            }
        }
    }

    fun setEntries(newList: List<CachingDocumentFile>) {
        synchronized(directoryEntries) {
            directoryEntries.clear()
            directoryEntries.addAll(newList)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val fileName = view.findViewById<AppCompatTextView>(R.id.file_name)
        val fileType = view.findViewById<AppCompatTextView>(R.id.mime_type)
        val imageView = view.findViewById<AppCompatImageView>(R.id.entry_image)
    }
}

interface OnDocClickListener {
    fun onDocumentClicked(clickDocument: CachingDocumentFile)
    fun onDocumentLongClicked(clickeDocument: CachingDocumentFile)
}