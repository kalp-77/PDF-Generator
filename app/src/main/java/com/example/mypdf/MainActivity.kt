package com.example.mypdf

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mypdf.databinding.ActivityMainBinding
import com.example.mypdf.ui.fragments.PDFFragment
import com.example.mypdf.ui.fragments.PictureFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        window.statusBarColor = Color.BLACK

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadPicturesfragment()

        binding.bottomNavigationMenu.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_menu_pictures -> {
                    loadPicturesfragment()
                }
                R.id.bottom_menu_pdfs -> {
                    loadPdfFragment()
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun loadPicturesfragment() {
        title = "Pictures"
        val pictureListFragment = PictureFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.frameLayout, pictureListFragment,"PictureFragment")
        fragmentTransaction.commit()
    }

    private fun loadPdfFragment() {
        title = "PDF List"
        val pdfListFragment = PDFFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.frameLayout, pdfListFragment, "PDFFragment")
        fragmentTransaction.commit()
    }


}