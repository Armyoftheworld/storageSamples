package com.example.storagesamples.contentproviderpaging

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import com.example.storagesamples.R
import java.io.File
import kotlin.math.min

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-29
 * @description
 */
class ImageProvider : ContentProvider() {

    private lateinit var baseDir: File

    companion object {
        private val TAG = ImageProvider::class.java.simpleName
        private const val REPEAT_COUNT_WRITE_FILES = 10
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private val IMAGES = 1

        private val IMAGE_ID = 2

        init {
            uriMatcher.addURI(ImageContract.AUTHORITY, "images", IMAGES)
            uriMatcher.addURI(ImageContract.AUTHORITY, "images/#", IMAGE_ID)
        }

        private fun resolveDocumentProjection(projection: Array<String>?): Array<String> {
            return projection ?: ImageContract.PROJECTION_ALL
        }
    }


    override fun onCreate(): Boolean {
        baseDir = context!!.filesDir
        writeDummyFilesToStorage(context!!)
        return true
    }

    private fun writeDummyFilesToStorage(context: Context) {
        if (baseDir.list()?.isNotEmpty() == true) {
            return
        }
        val imageResIds = getResourceIdArray(context, R.array.image_res_ids)
        for (i in 0 until REPEAT_COUNT_WRITE_FILES) {
            for (resId in imageResIds) {
                writeFileToInternalStorage(context, resId, "-$i.jpeg")
            }
        }

    }

    private fun writeFileToInternalStorage(context: Context, resId: Int, extension: String) {
        val ins = context.resources.openRawResource(resId)
        val buffer = ByteArray(1024)
        try {
            val filename = context.resources.getResourceEntryName(resId) + extension
            val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
            var len = ins.read(buffer)
            while (len >= 0) {
                fos.write(buffer, 0, len)
                len = ins.read(buffer)
            }
            ins.close()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getResourceIdArray(context: Context, resId: Int): IntArray {
        val ar = context.resources.obtainTypedArray(resId)
        val len = ar.length()
        val resIds = IntArray(len)
        for (index in 0 until len) {
            resIds[index] = ar.getResourceId(index, 0)
        }
        ar.recycle()
        return resIds
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException()
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        throw UnsupportedOperationException()
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        queryArgs: Bundle?,
        cancellationSignal: CancellationSignal?
    ): Cursor? {
        val match = uriMatcher.match(uri)
        if (match != IMAGES) {
            return null
        }
        val result = MatrixCursor(resolveDocumentProjection(projection))
        val files = baseDir.listFiles()!!
        val offset = queryArgs?.getInt(ContentResolver.QUERY_ARG_OFFSET, 0)?: 0
        val limit = queryArgs?.getInt(ContentResolver.QUERY_ARG_LIMIT, Int.MAX_VALUE)?: Int.MAX_VALUE
        Log.d(TAG, "queryChildDocuments with Bundle, Uri: " +
                uri + ", offset: " + offset + ", limit: " + limit)
        if (offset < 0) {
            throw IllegalArgumentException("Offset must not be less than 0")
        }
        if (limit < 0) {
            throw IllegalArgumentException("Limit must not be less than 0")
        }
        if (offset >= files.size) {
            return result
        }
        val maxIndex = min(offset + limit, files.size)
        for (i in offset until maxIndex) {
            includeFile(result, files[i])
        }
        val bundle = constructExtras(queryArgs, files)
        result.extras = bundle
        return result
    }

    private fun constructExtras(queryArgs: Bundle?, files: Array<File>): Bundle {
        val bundle = Bundle()
        bundle.putInt(ContentResolver.EXTRA_TOTAL_COUNT, files.size)
        var size = 0
        if (queryArgs?.containsKey(ContentResolver.QUERY_ARG_OFFSET) == true) {
            size++
        }
        if (queryArgs?.containsKey(ContentResolver.QUERY_ARG_LIMIT) == true) {
            size++
        }
        if (size > 0) {
            val honoredArgs = arrayOfNulls<String>(size)
            var index = 0
            if (queryArgs?.containsKey(ContentResolver.QUERY_ARG_OFFSET) == true) {
                honoredArgs[index++] = ContentResolver.QUERY_ARG_OFFSET
            }
            if (queryArgs?.containsKey(ContentResolver.QUERY_ARG_LIMIT) == true) {
                honoredArgs[index] = ContentResolver.QUERY_ARG_LIMIT
            }
            bundle.putStringArray(ContentResolver.EXTRA_HONORED_ARGS, honoredArgs)
        }
        return bundle
    }

    private fun includeFile(result: MatrixCursor, file: File) {
        val row = result.newRow()
        row.add(ImageContract.Columns.DISPLAY_NAME, file.name)
        row.add(ImageContract.Columns.SIZE, file.length())
        row.add(ImageContract.Columns.ABSOLUTE_PATH, file.absolutePath)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException()
    }

    override fun getType(uri: Uri): String? {
        return when(uriMatcher.match(uri)) {
            IMAGES -> "vnd.android.cursor.dir/images"
            IMAGE_ID -> "vnd.android.cursor.item/images"
            else -> throw IllegalArgumentException(String.format("Unknown URI: %s", uri))
        }
    }
}