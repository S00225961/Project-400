package com.example.project400.data

import android.graphics.RectF
import com.example.project400.data.KeyPoint

data class Person (
    var id: Int = -1, // default id is -1
    val keyPoints: List<KeyPoint>,
    val boundingBox: RectF? = null, // Only MoveNet MultiPose return bounding box.
    val score: Float
)