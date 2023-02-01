package com.example.mypdf.utils

import com.example.mypdf.data.PdfModel
import com.example.mypdf.ui.adapters.PdfAdapter


interface RvListenerPdf {

    fun onPdfClick(pdfModel: PdfModel, position: Int)
    fun onPdfMoreClick(pdfModel: PdfModel, position: Int, holder: PdfAdapter.PdfHolder)
}