package com.example.myapplication

import android.view.View
import android.view.ViewGroup
import com.kizitonwose.calendar.view.ViewContainer

class MonthViewContainer(view: View) : ViewContainer(view) {
    // Alternatively, you can add an ID to the container layout and use findViewById()
    val titlesContainer = view as ViewGroup
}
