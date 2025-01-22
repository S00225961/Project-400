package com.example.project400

import android.graphics.Bitmap

interface PoseDetector : AutoCloseable {
    fun estimatePoses(bitmap: Bitmap): List<Person>

    fun lastInferenceTimeNanos(): Long
}