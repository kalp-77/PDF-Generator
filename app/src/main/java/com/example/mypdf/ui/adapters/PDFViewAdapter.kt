package com.example.mypdf.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mypdf.R
import com.example.mypdf.data.PdfViewModel
import com.example.mypdf.data.PictureModel
import com.example.mypdf.ui.activities.ImageViewActivity

class PDFViewAdapter(
    private val context: Context,
    private val pdfViewArrayList: ArrayList<PdfViewModel>
): RecyclerView.Adapter<PDFViewAdapter.PdfViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_pdf_view, parent, false)
        return PdfViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val pdfViewModel = pdfViewArrayList[position]
        val pageNumber = position+1
        val bitmap = pdfViewModel.bitmap

        Glide.with(context)
            .load(bitmap)
            .placeholder(R.drawable.baseline_image_black)
            .into(holder.imagePdf)

        holder.pageNumber.text = "$pageNumber"
    }

    override fun getItemCount(): Int {
        return pdfViewArrayList.size
    }

    inner class PdfViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var pageNumber = itemView.findViewById<TextView>(R.id.pageNumberTv)
        var imagePdf = itemView.findViewById<ImageView>(R.id.imagePdf)
    }

}