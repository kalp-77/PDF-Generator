package com.example.mypdf.ui.activities

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.mypdf.R
import com.example.mypdf.data.PdfViewModel
import com.example.mypdf.databinding.ActivityMainBinding
import com.example.mypdf.databinding.ActivityPdfViewBinding
import com.example.mypdf.ui.adapters.PDFViewAdapter
import java.io.File
import java.util.concurrent.Executors

class PdfViewActivity : AppCompatActivity() {


    private lateinit var binding: ActivityPdfViewBinding
    private var pdfUri = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)
        window.statusBarColor = Color.BLACK

        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        supportActionBar?.title = "PDF Viewer"
        supportActionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        pdfUri = intent.getStringExtra("pdfUri").toString()
        loadPdfPages()
    }
    private var currPage:PdfRenderer.Page? = null

    private fun loadPdfPages(){
        val pdfViewArrayList = ArrayList<PdfViewModel>()
        val pdfViewAdapter = PDFViewAdapter(this,pdfViewArrayList)
        binding.pdfViewRv.adapter = pdfViewAdapter

        val file = Uri.parse(pdfUri).path?.let { File(it) }
        try {
            supportActionBar?.setSubtitle(file?.name)
        }
        catch (e:Exception) { }
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executorService.execute {
            try{
                val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)
                val pageCount = pdfRenderer.pageCount
                if(pageCount <=0) {
                    Log.d("TAG", "loadThumbnailFromPdf: No pages")
                }
                else {
                    Log.d("TAG", "loadThumbnailFromPdffff: $pageCount")
                    for(i in 0 until pageCount) {
                        // close current page before opening new page
                        if(currPage!=null){
                            currPage?.close()
                        }
                        currPage = pdfRenderer.openPage(i)
                        val bitmap = Bitmap.createBitmap(
                            currPage!!.width,
                            currPage!!.height,
                            Bitmap.Config.ARGB_8888
                        )

                        currPage!!.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        val pdfViewModel = PdfViewModel(Uri.parse(pdfUri), (i+1), pageCount, bitmap)
                        pdfViewArrayList.add(pdfViewModel)
                    }
                }
            }
            catch(e:Exception) {

            }
            handler.post{
                pdfViewAdapter.notifyDataSetChanged()
            }
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}