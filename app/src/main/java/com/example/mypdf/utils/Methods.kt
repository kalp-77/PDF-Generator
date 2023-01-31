package com.example.mypdf.utils

import android.text.format.DateFormat
import java.util.*

object Methods {


    fun formatTimestamp(timestamp: Long) : String {
        val calander = Calendar.getInstance(Locale.ENGLISH)
        calander.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy",calander).toString()
    }
}