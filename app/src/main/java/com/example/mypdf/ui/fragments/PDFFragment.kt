package com.example.mypdf.ui.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mypdf.R
import com.example.mypdf.data.PdfModel
import com.example.mypdf.databinding.FragmentPDFBinding
import com.example.mypdf.ui.activities.PdfViewActivity
import com.example.mypdf.ui.adapters.PdfAdapter
import com.example.mypdf.utils.Constants
import com.example.mypdf.utils.RvListenerPdf
import java.io.File

class PDFFragment : Fragment() {

    private lateinit var mContext:Context
    private lateinit var binding: FragmentPDFBinding
    private lateinit var adapterPdf: PdfAdapter
    private lateinit var pdfArray: ArrayList<PdfModel>
    private lateinit var pdfRv : RecyclerView

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPDFBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pdfRv = view.findViewById(R.id.pdfRv)
        loadPdfDocuments()
    }
    private fun loadPdfDocuments() {
        pdfArray = ArrayList()
        adapterPdf = PdfAdapter(mContext,pdfArray, object : RvListenerPdf{
            override fun onPdfClick(pdfModel: PdfModel, position: Int) {
                val intent = Intent(mContext, PdfViewActivity::class.java)
                intent.putExtra("pdfUri","${pdfModel.uri}")
                startActivity(intent)
            }

            override fun onPdfMoreClick(
                pdfModel: PdfModel,
                position: Int,
                holder: PdfAdapter.PdfHolder
            ) {
                TODO("Not yet implemented")
            }

        })
        pdfRv.adapter = adapterPdf
        val folder = File(mContext.getExternalFilesDir(null),Constants.PDF_FOLDER)
        if(folder.exists()) {
            val files = folder.listFiles()
            for(file in files!!) {
                val uri = Uri.fromFile(file)
                val pdfModel = PdfModel(file, uri)
                pdfArray.add(pdfModel)
                adapterPdf.notifyItemInserted(pdfArray.size)
            }
        }
    }

}