package com.example.mypdf.data

import android.graphics.Bitmap
import android.net.Uri

class PdfViewModel(
    var pdfUri: Uri,
    var pageNumber: Int,
    var pageCount: Int,
    var bitmap: Bitmap
)