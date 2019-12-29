package com.example.storagesamples.contentproviderpaging

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.storagesamples.R

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-29
 * @description
 */
internal class ImageAdapter(private val context: Context): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>(){

    private val mImageDocuments = ArrayList<ImageDocument>()

    private var totalSize = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount() = totalSize

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (mImageDocuments.size > position) {
            holder.imageView.setImageURI(mImageDocuments[position].uri)
            holder.textView.text = (position + 1).toString()
        } else {
            holder.imageView.setImageResource(R.drawable.cat_placeholder)
        }
    }

    fun add(imageDocument: ImageDocument) {
        mImageDocuments.add(imageDocument)
    }

    fun setTotalSize(size: Int) {
        totalSize = size
    }

    fun fetchedItemCount() = mImageDocuments.size

    class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var imageView = itemView.findViewById<ImageView>(R.id.imageview)
        var textView = itemView.findViewById<TextView>(R.id.textview_image_label)
    }

    internal data class ImageDocument(val absolutePath: String, val displayName: String, val uri: Uri)

}