package com.example.project400.body_tracking

import android.graphics.Bitmap
import com.example.project400.data.Person

interface PoseDetector : AutoCloseable {
    fun estimatePoses(bitmap: Bitmap): List<Person>

    fun lastInferenceTimeNanos(): Long
}