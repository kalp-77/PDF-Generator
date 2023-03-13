package com.example.mypdf.ui.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.mypdf.R
import com.example.mypdf.databinding.ActivityImageViewBinding
import com.example.mypdf.databinding.ActivityMainBinding

class ImageViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewBinding
    private var pictureUri = ""

    companion object {
        private const val TAG = "IMAGE_VIEW_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
        window.statusBarColor = Color.BLACK


        binding = ActivityImageViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val imageTv = binding.imageTv

        supportActionBar?.title = "Image View"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        pictureUri = intent.getStringExtra("pictureUri").toString()
        Glide.with(this)
            .load(pictureUri)
            .placeholder(R.drawable.baseline_image_black)
            .into(imageTv)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}