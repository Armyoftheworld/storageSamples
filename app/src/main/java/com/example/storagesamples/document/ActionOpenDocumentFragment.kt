package com.example.storagesamples.document

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.storagesamples.R
import kotlinx.android.synthetic.main.fragment_pdf_renderer_basic.*

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-01
 * @description
 */

private const val CURRENT_PAGE_INDEX_KEY =
    "com.example.storagesamples.document.state.CURRENT_PAGE_INDEX_KEY"

private const val TAG = "ActionOpenDocumentFragment"
private const val INITIAL_PAGE_INDEX = 0

class ActionOpenDocumentFragment : Fragment() {
    companion object {
        private const val DOCUMENT_URI_ARGUMENT =
            "com.example.storagesamples.document.args.DOCUMENT_URI_ARGUMENT"

        fun newInstance(documentUri: Uri): ActionOpenDocumentFragment {
            return ActionOpenDocumentFragment().apply {
                arguments = Bundle().apply {
                    putString(DOCUMENT_URI_ARGUMENT, documentUri.toString())
                }
            }
        }
    }

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private var currentPageNumber = INITIAL_PAGE_INDEX

    private val pageCount @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    get() = pdfRenderer.pageCount


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pdf_renderer_basic, container, false)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previous.setOnClickListener {
            showPage(currentPage.index - 1)
        }
        next.setOnClickListener {
            showPage(currentPage.index + 1)
        }
        currentPageNumber = savedInstanceState?.getInt(CURRENT_PAGE_INDEX_KEY, INITIAL_PAGE_INDEX)
            ?: INITIAL_PAGE_INDEX
    }

    override fun onStart() {
        super.onStart()
        val documentUri = arguments?.getString(DOCUMENT_URI_ARGUMENT)?.toUri() ?: return
        try {
            openRenerer(activity, documentUri)
            showPage(currentPageNumber)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            closeRenderer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(java.lang.Exception::class)
    private fun openRenerer(activity: FragmentActivity?, documentUri: Uri) {
        if (activity == null) {
            return
        }
        val fileDescriptor = activity.contentResolver.openFileDescriptor(documentUri, "r") ?: return
        pdfRenderer = PdfRenderer(fileDescriptor)
        currentPage = pdfRenderer.openPage(currentPageNumber)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showPage(index: Int) {
        if (index < 0 || index >= pageCount) {
            return
        }
        currentPage.close()
        currentPage = pdfRenderer.openPage(index)
        val bitmap = Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        image.setImageBitmap(bitmap)
        previous.isEnabled = index != 0
        next.isEnabled = index + 1 < pageCount
    }


    @Throws(Exception::class)
    private fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_PAGE_INDEX_KEY, currentPage.index)
        super.onSaveInstanceState(outState)
    }
}