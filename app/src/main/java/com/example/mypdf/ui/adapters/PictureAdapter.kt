package com.example.mypdf.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mypdf.R
import com.example.mypdf.data.PictureModel
import com.example.mypdf.ui.activities.ImageViewActivity


class PictureAdapter(
    private val context: Context,
    private val pictureArrayList: ArrayList<PictureModel>
): RecyclerView.Adapter<PictureAdapter.PictureHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_card, parent, false)
        return PictureHolder(view)
    }

    override fun onBindViewHolder(holder: PictureHolder, position: Int) {
        val pictureModel = pictureArrayList[position]
        val pictureUri = pictureModel.pictureUri

        Glide.with(context)
            .load(pictureUri)
            .placeholder(R.drawable.baseline_image_black)
            .into(holder.pictureTv)

        holder.itemView.setOnClickListener{
            val intent = Intent(context, ImageViewActivity::class.java)
            intent.putExtra("pictureUri", "$pictureUri")
            context.startActivity(intent)
        }
        holder.checkBox.setOnCheckedChangeListener {view, isChecked ->
            pictureModel.checked = isChecked
        }
    }

    override fun getItemCount(): Int {
        return pictureArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    inner class PictureHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var pictureTv = itemView.findViewById<ImageView>(R.id.imageTv)
        var checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)

    }

}