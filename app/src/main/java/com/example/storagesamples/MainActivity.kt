package com.example.storagesamples

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.storagesamples.document.OpenDocumentActivity
import com.example.storagesamples.documenttree.OpenDocumentTreeActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openDocument(view: View) {
        startActivity(Intent(this, OpenDocumentActivity::class.java))
    }

    fun openDocumentTree(view: View) {
        startActivity(Intent(this, OpenDocumentTreeActivity::class.java))
    }


}
