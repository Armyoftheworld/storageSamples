package com.example.storagesamples.contentproviderpaging

import android.net.Uri
import android.provider.BaseColumns

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-29
 * @description
 */
internal object ImageContract {
    val AUTHORITY = "com.example.android.contentproviderpaging.documents"
    val CONTENT_URI = Uri.parse("content://$AUTHORITY/images")

    internal interface Columns: BaseColumns {
        companion object{
            val DISPLAY_NAME = "display_name"
            val ABSOLUTE_PATH = "absolute_path"
            val SIZE = "size"
        }
    }

    val PROJECTION_ALL = arrayOf(BaseColumns._ID, Columns.DISPLAY_NAME, Columns.ABSOLUTE_PATH, Columns.SIZE)
}