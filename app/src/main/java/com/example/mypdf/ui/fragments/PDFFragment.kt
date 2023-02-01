package com.example.mypdf.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.mypdf.R
import com.example.mypdf.data.PdfModel
import com.example.mypdf.databinding.FragmentPDFBinding
import com.example.mypdf.ui.activities.PdfViewActivity
import com.example.mypdf.ui.adapters.PdfAdapter
import com.example.mypdf.utils.Constants
import com.example.mypdf.utils.RvListenerPdf
import org.w3c.dom.Text
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
        binding = FragmentPDFBinding.inflate(inflater,container,false)
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
                showMoreOptionDialog(pdfModel,holder)
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

    private fun showMoreOptionDialog(pdfModel: PdfModel, holder: PdfAdapter.PdfHolder) {
        val popupMenu = PopupMenu(mContext, holder.moreBtn)
        popupMenu.menu.add(Menu.NONE,0,0,"Rename")
        popupMenu.menu.add(Menu.NONE,1,1,"Delete")
        popupMenu.menu.add(Menu.NONE,2,2,"Share")
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                0 -> {
                    pdfRename(pdfModel)
                }
                1 -> {
                    pdfDelete(pdfModel)
                }
                2 -> {
                    pdfShare(pdfModel)
                }
            }

            true
        }

    }

    private fun pdfShare(pdfModel: PdfModel) {
        val file = pdfModel.file
        val fileUri = FileProvider.getUriForFile(mContext,"com.example.mypdf.fileprovider",file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        startActivity(Intent.createChooser(intent,"Share PDF"))
    }

    private fun pdfDelete(pdfModel: PdfModel) {
        val dialog = AlertDialog.Builder(mContext)
        dialog.setTitle("Delete File")
            .setMessage("Delete ${pdfModel.file.name} file?")
            .setPositiveButton("Delete") { dialog, _ ->
                try{
                    pdfModel.file.delete()
                    Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show()
                    loadPdfDocuments()
                }
                catch(e:Exception) {
                    Toast.makeText(mContext, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
                    loadPdfDocuments()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun pdfRename(pdfModel: PdfModel) {
        val view = LayoutInflater.from(mContext).inflate(R.layout.rename,null)
        val pdfRenameEt = view.findViewById<EditText>(R.id.pdfRenameEt)
        val renameBtn = view.findViewById<Button>(R.id.renamrBtn)
        val prevName = pdfModel.file.nameWithoutExtension

        pdfRenameEt.setText(prevName)

        val builder = AlertDialog.Builder(mContext)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.show()

        renameBtn.setOnClickListener {
            val newName = pdfRenameEt.text.toString().trim()
            if(newName.isEmpty()) {
                Toast.makeText(mContext, "Please Enter Name..!", Toast.LENGTH_SHORT).show()
            }
            else {
                try {
                    val newFile = File(mContext.getExternalFilesDir(null), "${Constants.PDF_FOLDER}/$newName.pdf")
                    pdfModel.file.renameTo(newFile)
                    Toast.makeText(mContext, "Successful", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                    loadPdfDocuments()
                }
                catch (_:Exception){}
                alertDialog.dismiss()


            }
        }

    }

}