package com.example.mypdf.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mypdf.R
import com.example.mypdf.databinding.FragmentPDFBinding

class PDFFragment : Fragment() {

    private lateinit var mContext:Context
    private lateinit var binding: FragmentPDFBinding

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

}