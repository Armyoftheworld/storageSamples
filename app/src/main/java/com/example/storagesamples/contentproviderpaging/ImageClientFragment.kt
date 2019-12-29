package com.example.storagesamples.contentproviderpaging

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storagesamples.R
import kotlinx.android.synthetic.main.fragment_image_client.*
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-29
 * @description
 */
class ImageClientFragment : Fragment() {
    companion object {
        private val TAG = ImageClientFragment::class.java.simpleName
        private val LIMIT = 10

        fun newInstance(): Fragment {
            val fragment = ImageClientFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }

    private lateinit var adapter: ImageAdapter
    private val loaderCallback = LoaderCallback()
    private val offset = AtomicInteger(0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerview.layoutManager = linearLayoutManager
        adapter = ImageAdapter(view.context)
        recyclerview.adapter = adapter
        recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
                if (lastVisibleItemPosition >= adapter.fetchedItemCount()) {
                    val pageId = lastVisibleItemPosition / LIMIT
                    LoaderManager.getInstance(this@ImageClientFragment)
                        .restartLoader(pageId, null, loaderCallback)
                }
            }
        })

        button_show.setOnClickListener {
            LoaderManager.getInstance(this).restartLoader(0, null, loaderCallback)
            button_show.visibility = View.GONE
        }
    }

    private inner class LoaderCallback : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            return object : CursorLoader(activity!!) {
                override fun loadInBackground(): Cursor? {
                    val bundle = Bundle()
                    bundle.putInt(ContentResolver.QUERY_ARG_OFFSET, offset.toInt())
                    bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, LIMIT)
                    return activity!!.contentResolver
                        .query(ImageContract.CONTENT_URI, null, bundle, null)
                }
            }
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
            val extras = data?.extras
            val totalSize = extras?.getInt(ContentResolver.EXTRA_TOTAL_COUNT) ?: Int.MAX_VALUE
            adapter.setTotalSize(totalSize)
            val beforeCount = adapter.fetchedItemCount()
            while (data?.moveToNext() == true) {
                val displayName =
                    data.getString(data.getColumnIndex(ImageContract.Columns.DISPLAY_NAME))
                val absolutePath =
                    data.getString(data.getColumnIndex(ImageContract.Columns.ABSOLUTE_PATH))
                println(absolutePath)
                val imageDocument = ImageAdapter.ImageDocument(
                    absolutePath, displayName, Uri.fromFile(
                        File(absolutePath)
                    )
                )
                adapter.add(imageDocument)
            }
            val count = data?.count ?: 0
            if (count == 0) {
                return
            }
            adapter.notifyItemRangeChanged(beforeCount, count)
            val offsetSnapShot = offset.get()
            val msg = getString(
                R.string.fetched_images_out_of, offsetSnapShot + 1, offsetSnapShot + count,
                totalSize
            )
            offset.addAndGet(count)
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
        }

    }


}